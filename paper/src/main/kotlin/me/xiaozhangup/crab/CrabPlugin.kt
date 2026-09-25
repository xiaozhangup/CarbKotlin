package me.xiaozhangup.crab

import me.xiaozhangup.crab.lifecycle.LifeCycle
import org.bukkit.plugin.java.JavaPlugin

/** Native Paper entry point with one automatically managed Crab instance. */
abstract class CrabPlugin : JavaPlugin() {
    val crab = Crab(this, dataFolder)

    protected open fun load() {}
    protected open fun enable() {}
    protected open fun active() {}
    protected open fun disable() {}

    final override fun onLoad() {
        crab.lifecycle.run(LifeCycle.CONST)
        crab.loadConfigurations()
        crab.lifecycle.run(LifeCycle.INIT)
        crab.lifecycle.run(LifeCycle.LOAD)
        load()
    }

    final override fun onEnable() {
        crab.lifecycle.run(LifeCycle.ENABLE)
        crab.registerEvents()
        crab.registerPlaceholders()
        crab.commands.registerAnnotated(crab.scanner)
        enable()
        crab.start()
        crab.submitTask(delay = 1) {
            crab.lifecycle.run(LifeCycle.ACTIVE)
            active()
        }
    }

    final override fun onDisable() {
        try {
            try {
                crab.lifecycle.run(LifeCycle.DISABLE)
            } finally {
                disable()
            }
        } finally {
            crab.close()
        }
    }
}
