package me.xiaozhangup.carbkotlin.redis

import java.io.Closeable
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

internal class RedisLifecycle : AutoCloseable {
    val service = Executors.newCachedThreadPool() { runnable ->
        Thread(runnable, "CarbKotlin-redis").apply { isDaemon = true }
    }
    val resources = CopyOnWriteArrayList<Closeable>()

    private val scheduler = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "CarbKotlin-redis-lock").apply { isDaemon = true }
    }

    fun schedule(periodMillis: Long, action: () -> Unit): ScheduledFuture<*> =
        scheduler.scheduleWithFixedDelay(action, periodMillis, periodMillis, TimeUnit.MILLISECONDS)

    override fun close() {
        service.shutdownNow()
        scheduler.shutdownNow()
        var failure: Throwable? = null
        resources.forEach {
            try { it.close() } catch (ex: Exception) {
                if (failure == null) failure = ex else failure!!.addSuppressed(ex)
            }
        }
        resources.clear()
        failure?.let { throw it }
    }

}
