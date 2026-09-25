package me.xiaozhangup.carbkotlin

import kotlin.Any
import me.xiaozhangup.carbkotlin.command.Commands
import me.xiaozhangup.carbkotlin.chain.PlatformChainScheduler
import java.io.File

/** Supplies native services lazily, including before the native plugin constructor finishes. */
class Crab(anchor: Class<*>, dataFolder: () -> File, private val plugin: () -> Any) :
    CrabContext(anchor, dataFolder, { Commands(plugin()) }, { PlatformChainScheduler(plugin()) }) {
    private val events by lazy { own(me.xiaozhangup.carbkotlin.event.Events(plugin())) }
    fun registerEvents() = events.register(scanner)
    fun <E : Any> fireEvent(event: E) = me.xiaozhangup.crab.CarbKotlin.getServer().eventManager.fire(event)
    fun onlinePlayers() = me.xiaozhangup.crab.CarbKotlin.getServer().allPlayers
    fun console() = me.xiaozhangup.crab.CarbKotlin.getServer().consoleCommandSource
    fun executeConsole(command: String) =
        me.xiaozhangup.crab.CarbKotlin.getServer().commandManager.executeAsync(console(), command)

    /** Bind the native owner; queued tasks start only when start() is called. */
    constructor(plugin: Any, dataFolder: File) : this(plugin.javaClass, { dataFolder }, { plugin }) {
        scanner.bind(plugin)
    }
}
