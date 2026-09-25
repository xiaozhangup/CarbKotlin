package me.xiaozhangup.carbkotlin.chain

import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** DSL adapted from TabooLib basic-submit-chain 6.3.0-test-6-23-1 (MIT). */
class Chain<R> internal constructor(private val executor: ChainExecutor) {
    suspend fun <T> async(block: () -> T): T = withContext(executor.dispatcher(DispatcherType.ASYNC)) { block() }
    suspend fun <T> sync(block: () -> T): T = withContext(executor.dispatcher(DispatcherType.SYNC)) { block() }
    suspend fun wait(value: Long, type: DurationType) {
        delay(if (type == DurationType.MINECRAFT_TICK) Math.multiplyExact(value, 50) else value)
    }
    suspend fun <T> sync(period: Long, now: Boolean = false, delay: Long = 0, block: Cancellable.() -> T): T =
        withContext(executor.dispatcher(DispatcherType.SYNC)) { repeat(DispatcherType.SYNC, period, now, delay, block) }
    suspend fun <T> async(period: Long, now: Boolean = false, delay: Long = 0, block: Cancellable.() -> T): T =
        withContext(executor.dispatcher(DispatcherType.ASYNC)) { repeat(DispatcherType.ASYNC, period, now, delay, block) }

    private suspend fun <T> repeat(type: DispatcherType, period: Long, now: Boolean, delay: Long, block: Cancellable.() -> T): T {
        require(period > 0) { "Repeat period must be positive" }
        return suspendCancellableCoroutine { continuation ->
            val finished = AtomicBoolean()
            val task = AtomicReference<AutoCloseable?>()
            val cancellable = Cancellable()
            fun stop() { task.get()?.close() }
            continuation.invokeOnCancellation { finished.set(true); stop() }
            val action = Runnable {
                if (!finished.get()) {
                    try {
                        val value = cancellable.call(block)
                        if (cancellable.cancelled && finished.compareAndSet(false, true)) {
                            stop()
                            continuation.resume(value)
                        }
                    } catch (ex: Throwable) {
                        if (finished.compareAndSet(false, true)) {
                            stop()
                            continuation.resumeWithException(ex)
                        }
                    }
                }
            }
            // Match submit(now = true): one immediate execution, ignoring delay/period.
            if (now) action.run()
            else if (!finished.get()) {
                try {
                    val handle = executor.scheduler.schedule(type, delay, period, action)
                    task.set(handle)
                    if (finished.get()) handle.close()
                } catch (ex: Exception) {
                    if (finished.compareAndSet(false, true)) continuation.resumeWithException(ex)
                }
            }
        }
    }
}
