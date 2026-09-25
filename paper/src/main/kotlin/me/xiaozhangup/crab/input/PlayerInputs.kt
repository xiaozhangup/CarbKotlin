package me.xiaozhangup.crab.input

import me.xiaozhangup.crab.Crab
import me.xiaozhangup.crab.util.giveItem
import me.xiaozhangup.crab.util.takeItem
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEditBookEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.plugin.Plugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PlayerInputs(plugin: Plugin, private val crab: Crab) : Listener, AutoCloseable {
    private val chats = ConcurrentHashMap<UUID, (String) -> Unit>()
    private val books = ConcurrentHashMap<UUID, Pair<Boolean, (List<String>) -> Unit>>()
    private val bookMarker = "§7Right-Click to open and write."

    init { plugin.server.pluginManager.registerEvents(this, plugin) }

    fun nextChat(player: Player, callback: (String) -> Unit) { chats[player.uniqueId] = callback }
    fun cancelNextChat(player: Player, execute: Boolean = true) {
        val callback = chats.remove(player.uniqueId)
        if (execute) callback?.invoke("")
    }

    @Suppress("DEPRECATION")
    fun inputBook(player: Player, display: String, disposable: Boolean = true, content: List<String> = emptyList(), callback: (List<String>) -> Unit) {
        player.inventory.takeItem(99) { it.itemMeta?.lore?.contains(bookMarker) == true }
        player.giveItem(ItemStack(Material.WRITABLE_BOOK).apply {
            editMeta { meta ->
                meta as BookMeta
                meta.setDisplayName("§f$display")
                meta.lore = listOf(bookMarker, if (disposable) "§cDisposable" else "§aReusable")
                meta.pages = listOf(content.joinToString("\n"))
            }
        })
        books[player.uniqueId] = disposable to callback
    }

    @EventHandler
    fun chat(event: AsyncChatEvent) {
        val callback = chats.remove(event.player.uniqueId) ?: return
        event.isCancelled = true
        callback(PlainTextComponentSerializer.plainText().serialize(event.originalMessage()))
    }

    @EventHandler
    @Suppress("DEPRECATION")
    fun book(event: PlayerEditBookEvent) {
        if (event.newBookMeta.lore?.firstOrNull() != bookMarker) return
        val (disposable, callback) = books[event.player.uniqueId] ?: return
        callback(event.newBookMeta.pages.flatMap { it.replace("§0", "").split('\n') })
        if (disposable) {
            books.remove(event.player.uniqueId)
            crab.submitTask(delay = 1) {
                event.player.inventory.takeItem(99) { it.itemMeta?.lore?.contains(bookMarker) == true }
            }
        }
    }

    @EventHandler
    fun quit(event: PlayerQuitEvent) {
        chats.remove(event.player.uniqueId)
        books.remove(event.player.uniqueId)
    }

    override fun close() {
        HandlerList.unregisterAll(this)
        chats.clear()
        books.clear()
    }
}
