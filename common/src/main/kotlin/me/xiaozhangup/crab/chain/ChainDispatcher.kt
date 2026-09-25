package me.xiaozhangup.crab.chain

import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext

/** On shutdown, cancelled continuations still run so coroutine cleanup can finish. */
internal class ChainDispatcher(private val scheduler: ChainScheduler, private val type: DispatcherType) : CoroutineDispatcher(), AutoCloseable {
    private val pending = ConcurrentHashMap.newKeySet<Dispatch>()
    @Volatile private var closed = false

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        val dispatch = Dispatch(context, block)
        pending.add(dispatch)
        if (closed) {
            dispatch.reject()
            return
        }
        try {
            dispatch.attach(scheduler.schedule(type, 0, 0, dispatch))
        } catch (ex: Exception) {
            dispatch.reject(ex)
        }
    }

    override fun close() {
        closed = true
        pending.toList().forEach { it.reject() }
    }

    private inner class Dispatch(val context: CoroutineContext, val block: Runnable) : Runnable {
        private val claimed = AtomicBoolean()
        @Volatile private var rejected = false
        private val task = AtomicReference<AutoCloseable?>()
        fun attach(handle: AutoCloseable) {
            task.set(handle)
            if (rejected) handle.close()
        }
        override fun run() {
            if (!claimed.compareAndSet(false, true)) return
            pending.remove(this)
            block.run()
        }
        fun reject(cause: Exception? = null) {
            if (!claimed.compareAndSet(false, true)) return
            pending.remove(this)
            rejected = true
            task.get()?.close()
            context[Job]?.cancel(CancellationException("Chain scheduler closed", cause))
            Dispatchers.IO.dispatch(context, block)
        }
    }
}
