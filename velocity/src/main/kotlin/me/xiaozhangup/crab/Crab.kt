package me.xiaozhangup.crab

import kotlin.Any
import me.xiaozhangup.crab.command.Commands
import me.xiaozhangup.crab.chain.PlatformChainScheduler
import java.io.File

/** Supplies native services lazily, including before the native plugin constructor finishes. */
class Crab(anchor: Class<*>, dataFolder: () -> File, private val plugin: () -> Any) :
    CrabContext(anchor, dataFolder, { Commands(plugin()) }, { PlatformChainScheduler(plugin()) }) {
    private val events by lazy { own(me.xiaozhangup.crab.event.Events(plugin())) }
    fun registerEvents() = events.register(scanner)
    fun <E : Any> fireEvent(event: E) = me.xiaozhangup.crab.CrabKotlin.getServer().eventManager.fire(event)
    fun onlinePlayers() = me.xiaozhangup.crab.CrabKotlin.getServer().allPlayers
    fun console() = me.xiaozhangup.crab.CrabKotlin.getServer().consoleCommandSource
    fun executeConsole(command: String) =
        me.xiaozhangup.crab.CrabKotlin.getServer().commandManager.executeAsync(console(), command)

    /** Bind the native owner; queued tasks start only when start() is called. */
    constructor(plugin: Any, dataFolder: File) : this(plugin.javaClass, { dataFolder }, { plugin }) {
        scanner.bind(plugin)
    }
}
