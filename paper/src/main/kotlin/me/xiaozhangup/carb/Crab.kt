package me.xiaozhangup.carb

import org.bukkit.plugin.Plugin
import me.xiaozhangup.carb.command.Commands
import me.xiaozhangup.carb.chain.PlatformChainScheduler
import java.io.File

/** Supplies native services lazily, including before the native plugin constructor finishes. */
class Crab(anchor: Class<*>, dataFolder: () -> File, private val plugin: () -> Plugin) :
    CrabContext(anchor, dataFolder, { Commands(plugin()) }, { PlatformChainScheduler(plugin()) }) {
    /** Bind the native owner; queued tasks start only when start() is called. */
    constructor(plugin: org.bukkit.plugin.Plugin, dataFolder: File) : this(plugin.javaClass, { dataFolder }, { plugin }) {
        scanner.bind(plugin)
    }

    private val events by lazy { own(me.xiaozhangup.carb.event.Events(plugin())) }
    private val placeholders by lazy { own(me.xiaozhangup.carb.compat.Placeholders(plugin())) }
    val inputs by lazy { own(me.xiaozhangup.carb.input.PlayerInputs(plugin(), this)) }

    fun registerEvents() = events.register(scanner)
    fun registerPlaceholders() {
        if (org.bukkit.Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) placeholders.register(scanner)
    }
    fun registerPlaceholder(expansion: me.xiaozhangup.carb.compat.PlaceholderExpansion) {
        if (org.bukkit.Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) placeholders.register(expansion)
    }
    fun executeConsole(command: String) = org.bukkit.Bukkit.dispatchCommand(console(), command)
    fun console() = org.bukkit.Bukkit.getConsoleSender()
}
