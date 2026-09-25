package me.xiaozhangup.carb.event

import me.xiaozhangup.carb.reflect.PluginScanner
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

class Events(private val plugin: Plugin) : AutoCloseable {
    private val listener = object : Listener {}

    fun register(scanner: PluginScanner) {
        scanner.classes.forEach { type ->
            type.structure.methods.filter { it.isAnnotationPresent(SubscribeEvent::class.java) }.forEach { method ->
                require(method.parameter.size == 1) { "${type.name}.${method.name} must accept one Event" }
                val eventType = method.parameterTypes.single().asSubclass(Event::class.java)
                val annotation = method.getAnnotation(SubscribeEvent::class.java)
                val owner = if (method.isStatic) null else scanner.instance(type)
                plugin.server.pluginManager.registerEvent(eventType, listener,
                    annotation.enum("priority", EventPriority.NORMAL), { _, event ->
                        if (eventType.isInstance(event)) {
                            if (owner == null) method.invokeStatic(event) else method.invoke(owner, event)
                        }
                    }, plugin, annotation.property("ignoreCancelled", false))
            }
        }
    }

    override fun close() = HandlerList.unregisterAll(listener)
}
