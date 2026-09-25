/*
 * Derived from TabooLib 6.3.0-test-6-23-1 (MIT), Copyright (c) 2018 Bkm016.
 * Adapted for CarbKotlin shared commands and platform backends, 2026-09-25.
 * See src/main/resources/META-INF/licenses/TabooLib-command-LICENSE.txt.
 */
package me.xiaozhangup.carb.command.component

import me.xiaozhangup.carb.command.CommandContext

abstract class CommandBinder<T>(val bind: Class<T>) {

    @Suppress("UNCHECKED_CAST")
    fun cast(context: CommandContext<*>): T? {
        val sender = context.sender()
        return when {
            bind.isInstance(sender) -> sender as? T
            else -> null
        }
    }
}
