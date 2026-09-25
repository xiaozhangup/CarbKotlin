/*
 * Derived from TabooLib 6.3.0-test-6-23-1 (MIT), Copyright (c) 2018 Bkm016.
 * Adapted for CarbKotlin shared commands and platform backends, 2026-09-25.
 * See src/main/resources/META-INF/licenses/TabooLib-command-LICENSE.txt.
 */
@file:Suppress("DEPRECATION") // Retain the upstream positional compatibility helpers.

package me.xiaozhangup.carb.command


import org.bukkit.entity.Player
import org.bukkit.Bukkit

@Deprecated("Use named command arguments instead of positional offsets", ReplaceWith("player(id)"))
fun <T> CommandContext<T>.player(offset: Int): Player {
    return Bukkit.getOnlinePlayers().toList().first { it.name == argument(offset) }
}

@Deprecated("Use named command arguments instead of positional offsets", ReplaceWith("player(id)"))
fun <T> CommandContext<T>.playerAt(index: Int): Player {
    return Bukkit.getOnlinePlayers().toList().first { it.name == get(index) }
}
