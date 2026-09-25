package me.xiaozhangup.carb.task

import me.xiaozhangup.carb.CrabContext

/** Delays execution until calls have stopped for the requested number of milliseconds. */
class Debounce(private val crab: CrabContext, private val delay: Long, private val async: Boolean, val action: () -> Unit) : AutoCloseable {
    private var task: Task? = null
    private var token: Any? = null

    @Synchronized
    operator fun invoke(delay: Long = this.delay) {
        val current = Any()
        token = current
        task?.cancel()
        task = crab.submitTask(async = async, delay = delay / 50) {
            synchronized(this@Debounce) {
                if (token === current) {
                    task = null
                    token = null
                    action()
                }
            }
        }
    }

    @Synchronized
    override fun close() {
        token = null
        task?.cancel()
        task = null
    }
}
