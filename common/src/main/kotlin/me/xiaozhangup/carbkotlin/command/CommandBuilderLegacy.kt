/*
 * Derived from TabooLib 6.3.0-test-6-23-1 (MIT), Copyright (c) 2018 Bkm016.
 * Adapted for CarbKotlin shared commands and platform backends, 2026-09-25.
 * See src/main/resources/META-INF/licenses/TabooLib-command-LICENSE.txt.
 */
@file:Suppress("DEPRECATION") // Retain the upstream positional compatibility helpers.

package me.xiaozhangup.carbkotlin.command

import me.xiaozhangup.carbkotlin.command.*


@Deprecated("Use named command arguments instead of positional offsets", ReplaceWith("int(id)"))
fun <T> CommandContext<T>.int(offset: Int): Int {
    return argument(offset).toInt()
}

@Deprecated("Use named command arguments instead of positional offsets", ReplaceWith("int(id)"))
fun <T> CommandContext<T>.intAt(index: Int): Int {
    return get(index).toInt()
}

@Deprecated("Use named command arguments instead of positional offsets", ReplaceWith("double(id)"))
fun <T> CommandContext<T>.double(offset: Int): Double {
    return argument(offset).toDouble()
}

@Deprecated("Use named command arguments instead of positional offsets", ReplaceWith("double(id)"))
fun <T> CommandContext<T>.doubleAt(index: Int): Double {
    return get(index).toDouble()
}

@Deprecated("Use named command arguments instead of positional offsets", ReplaceWith("bool(id)"))
fun <T> CommandContext<T>.bool(offset: Int): Boolean {
    return argument(offset).toBooleanStrict()
}

@Deprecated("Use named command arguments instead of positional offsets", ReplaceWith("bool(id)"))
fun <T> CommandContext<T>.boolAt(index: Int): Boolean {
    return get(index).toBoolean()
}

