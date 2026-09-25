package me.xiaozhangup.carb.chain

import com.velocitypowered.api.proxy.ProxyServer
import java.util.concurrent.TimeUnit

/** Velocity has no Bukkit main thread. Both modes use its native task scheduler. */
internal class PlatformChainScheduler(private val plugin: Any, private val server: ProxyServer = me.xiaozhangup.carb.CarbKotlin.getServer()) : ChainScheduler {
    override fun schedule(type: DispatcherType, delay: Long, period: Long, action: Runnable): AutoCloseable {
        val builder = server.scheduler.buildTask(plugin, action)
        if (delay > 0) builder.delay(Math.multiplyExact(delay, 50), TimeUnit.MILLISECONDS)
        if (period > 0) builder.repeat(Math.multiplyExact(period, 50), TimeUnit.MILLISECONDS)
        val task = builder.schedule()
        return AutoCloseable { task.cancel() }
    }
}
