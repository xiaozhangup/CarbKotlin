package me.xiaozhangup.carb.chain

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

/** Paper main-thread and asynchronous scheduler. Delay/period are server ticks. */
internal class PlatformChainScheduler(private val plugin: Plugin) : ChainScheduler {
    override fun schedule(type: DispatcherType, delay: Long, period: Long, action: Runnable): AutoCloseable {
        val scheduler = Bukkit.getScheduler()
        val task = if (type == DispatcherType.ASYNC) {
            if (period > 0) scheduler.runTaskTimerAsynchronously(plugin, action, delay, period)
            else scheduler.runTaskLaterAsynchronously(plugin, action, delay)
        } else {
            if (period > 0) scheduler.runTaskTimer(plugin, action, delay, period)
            else scheduler.runTaskLater(plugin, action, delay)
        }
        return AutoCloseable { task.cancel() }
    }
}
