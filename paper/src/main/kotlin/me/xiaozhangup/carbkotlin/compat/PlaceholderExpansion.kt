package me.xiaozhangup.carbkotlin.compat

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

fun String.replacePlaceholder(): String {
    return try {
        PlaceholderAPI.setPlaceholders(null, this)
    } catch (ex: NoClassDefFoundError) {
        this
    }
}

fun List<String>.replacePlaceholder(): List<String> {
    return try {
        PlaceholderAPI.setPlaceholders(null, this)
    } catch (ex: NoClassDefFoundError) {
        this
    }
}

fun String.replacePlaceholder(player: Player): String {
    return try {
        PlaceholderAPI.setPlaceholders(player, this)
    } catch (ex: NoClassDefFoundError) {
        this
    }
}

fun List<String>.replacePlaceholder(player: Player): List<String> {
    return try {
        PlaceholderAPI.setPlaceholders(player, this)
    } catch (ex: NoClassDefFoundError) {
        this
    }
}

fun String.replacePlaceholder(player: OfflinePlayer): String {
    return try {
        PlaceholderAPI.setPlaceholders(player, this)
    } catch (ex: NoClassDefFoundError) {
        this
    }
}

fun List<String>.replacePlaceholder(player: OfflinePlayer): List<String> {
    return try {
        PlaceholderAPI.setPlaceholders(player, this)
    } catch (ex: NoClassDefFoundError) {
        this
    }
}

/** Plugin-owned PlaceholderAPI expansion; registered through Crab. */
interface PlaceholderExpansion {

    val identifier: String

    /**
     * 是否启用
     */
    val enabled: Boolean
        get() = true

    fun onPlaceholderRequest(player: Player?, args: String): String {
        return "onPlaceholderRequest(player: Player?, args: String) not implemented"
    }

    fun onPlaceholderRequest(player: OfflinePlayer?, args: String): String {
        if (player?.isOnline == true) {
            return onPlaceholderRequest(player.player, args)
        }
        return "onPlaceholderRequest(player: OfflinePlayer?, args: String) not implemented"
    }

}
