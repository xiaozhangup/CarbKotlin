/*
 * Derived from TabooLib 6.3.0-test-6-23-1 (MIT), Copyright (c) 2018 Bkm016.
 * Adapted for CarbKotlin shared commands and platform backends, 2026-09-25.
 * See src/main/resources/META-INF/licenses/TabooLib-command-LICENSE.txt.
 */
package me.xiaozhangup.carb.command.component

import me.xiaozhangup.carb.command.CommandContext

class CommandSuggestion<T>(bind: Class<T>, val uncheck: Boolean, val function: (sender: T, context: CommandContext<T>) -> List<String>?) : CommandBinder<T>(bind) {

    @Suppress("UNCHECKED_CAST")
    fun exec(context: CommandContext<*>): List<String>? {
        val sender = cast(context)
        return if (sender != null) {
            function.invoke(sender, (context as CommandContext<T>).copy(sender = sender))
        } else {
            null
        }
    }
}
