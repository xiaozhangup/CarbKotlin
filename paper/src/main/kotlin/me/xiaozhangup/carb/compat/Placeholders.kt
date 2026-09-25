package me.xiaozhangup.carb.compat

import me.xiaozhangup.carb.reflect.PluginScanner
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class Placeholders(private val plugin: Plugin) : AutoCloseable {
    private val expansions = mutableListOf<me.clip.placeholderapi.expansion.PlaceholderExpansion>()

    fun register(scanner: PluginScanner) {
        scanner.classes.filter { type ->
            type.structure.interfaces.any { it.name == PlaceholderExpansion::class.java.name }
                // Anonymous adapters capture their module and are registered explicitly.
                && !type.structure.owner.instance!!.isAnonymousClass
        }.forEach { type ->
            register(scanner.instance(type) as PlaceholderExpansion)
        }
    }

    fun register(expansion: PlaceholderExpansion) {
        if (!expansion.enabled) return
        val bridge = object : me.clip.placeholderapi.expansion.PlaceholderExpansion() {
            override fun getIdentifier() = expansion.identifier
            override fun getAuthor() = this@Placeholders.plugin.description.authors.joinToString()
            override fun getVersion() = this@Placeholders.plugin.description.version
            override fun persist() = true
            override fun onPlaceholderRequest(player: Player?, params: String) = expansion.onPlaceholderRequest(player, params)
            override fun onRequest(player: OfflinePlayer?, params: String) = expansion.onPlaceholderRequest(player, params)
        }
        check(bridge.register()) { "Cannot register placeholder expansion ${expansion.identifier}" }
        expansions += bridge
    }

    override fun close() {
        expansions.forEach { it.unregister() }
        expansions.clear()
    }
}
