package me.xiaozhangup.carb.event

import com.velocitypowered.api.event.Subscribe
import me.xiaozhangup.carb.reflect.PluginScanner
import me.xiaozhangup.carb.CarbKotlin

/** Owns only listeners registered through this instance, leaving the plugin entry untouched. */
class Events(private val plugin: Any) : AutoCloseable {
    private val manager get() = CarbKotlin.getServer().eventManager
    private val listeners = mutableListOf<Any>()

    fun register(scanner: PluginScanner) {
        scanner.classes.filter { type ->
            type.name != plugin.javaClass.name && type.structure.methods.any { it.isAnnotationPresent(Subscribe::class.java) }
        }.forEach { type ->
            val listener = scanner.instance(type)
            manager.register(plugin, listener)
            listeners += listener
        }
    }

    override fun close() {
        listeners.forEach { manager.unregisterListener(plugin, it) }
        listeners.clear()
    }
}
