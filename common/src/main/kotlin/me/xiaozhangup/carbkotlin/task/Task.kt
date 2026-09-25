package me.xiaozhangup.carbkotlin.task

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/** A cancellable task owned by one Crab instance. */
class Task internal constructor(private val removed: (Task) -> Unit) : AutoCloseable {
    private val disposed = AtomicBoolean()
    private val handle = AtomicReference<AutoCloseable?>()
    internal val active get() = !disposed.get()

    internal fun attach(task: AutoCloseable) {
        handle.set(task)
        if (disposed.get()) handle.getAndSet(null)?.close()
    }

    fun cancel() {
        if (disposed.compareAndSet(false, true)) {
            try { handle.getAndSet(null)?.close() } finally { removed(this) }
        }
    }

    override fun close() = cancel()
}
