package me.xiaozhangup.crab.task

import me.xiaozhangup.crab.chain.ChainScheduler
import me.xiaozhangup.crab.chain.DispatcherType

/** Queues pre-enable submissions and owns native handles until completion/cancellation. */
class TaskExecutor(private val backend: () -> ChainScheduler) : ChainScheduler, AutoCloseable {
    private val scheduler by lazy(backend)
    private val tasks = linkedSetOf<Task>()
    private val pending = mutableListOf<() -> Unit>()
    private var started = false
    private var closed = false
    @Volatile var runImmediately = false

    fun start() {
        val queued = synchronized(this) {
            check(!closed) { "Crab is closed" }
            started = true
            pending.toList().also { pending.clear() }
        }
        queued.forEach { it() }
    }

    fun submit(now: Boolean, async: Boolean, delay: Long, period: Long, respectImmediate: Boolean = true, schedule: ((Runnable) -> AutoCloseable)? = null, executor: Task.() -> Unit): Task {
        val task = Task { synchronized(this) { tasks.remove(it) } }
        val immediate = now || (respectImmediate && runImmediately)
        val action = {
            if (task.active) {
                try { task.executor() }
                catch (failure: Throwable) { task.cancel(); throw failure }
                finally { if (immediate || period <= 0) task.cancel() }
            }
        }
        val launch: () -> Unit = {
            if (task.active) {
                if (immediate) action()
                else try {
                    task.attach(schedule?.invoke(Runnable { action() }) ?: scheduler.schedule(
                        if (async) DispatcherType.ASYNC else DispatcherType.SYNC,
                        delay.coerceAtLeast(0), period, Runnable { action() }
                    ))
                } catch (failure: Throwable) {
                    task.cancel()
                    throw failure
                }
            }
        }
        val execute = synchronized(this) {
            check(!closed) { "Crab is closed" }
            tasks.add(task)
            if (started) true else { pending.add(launch); false }
        }
        if (execute) launch()
        return task
    }

    override fun schedule(type: DispatcherType, delay: Long, period: Long, action: Runnable): AutoCloseable =
        submit(false, type == DispatcherType.ASYNC, delay, period, respectImmediate = false) { action.run() }

    override fun close() {
        val snapshot = synchronized(this) {
            closed = true
            pending.clear()
            tasks.toList()
        }
        snapshot.forEach { it.cancel() }
    }
}
