package me.xiaozhangup.crab

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import me.xiaozhangup.crab.lifecycle.LifeCycle
import java.nio.file.Path

/** Native Velocity entry point; Guice injects services after the subclass is constructed. */
abstract class CrabPlugin {
    @field:Inject
    lateinit var server: ProxyServer
        private set

    @field:Inject
    @field:DataDirectory
    lateinit var dataDirectory: Path
        private set

    val crab = Crab(javaClass, { dataDirectory.toFile() }, { this }).apply {
        scanner.bind(this@CrabPlugin)
    }

    protected open fun load() {}
    protected open fun enable() {}
    protected open fun active() {}
    protected open fun disable() {}

    // Member injection runs after subclass fields and its companion instance are ready.
    @Inject
    private fun initializeCrab() {
        crab.lifecycle.run(LifeCycle.CONST)
        crab.loadConfigurations()
        crab.lifecycle.run(LifeCycle.INIT)
    }

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        crab.lifecycle.run(LifeCycle.LOAD)
        load()
        crab.lifecycle.run(LifeCycle.ENABLE)
        crab.commands.registerAnnotated(crab.scanner)
        enable()
        crab.registerEvents()
        crab.start()
        crab.submitTask {
            crab.lifecycle.run(LifeCycle.ACTIVE)
            active()
        }
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        try {
            try {
                disable()
            } finally {
                crab.lifecycle.run(LifeCycle.DISABLE)
            }
        } finally {
            crab.close()
        }
    }
}
