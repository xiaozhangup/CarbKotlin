/*
 * Derived from TabooLib 6.3.0-test-6-23-1 (MIT), Copyright (c) 2018 Bkm016.
 * Adapted for CarbKotlin shared commands and platform backends, 2026-09-25.
 * See src/main/resources/META-INF/licenses/TabooLib-command-LICENSE.txt.
 */
package me.xiaozhangup.carb.command.component

import me.xiaozhangup.carb.command.CommandContext

class CommandRestrict<T>(bind: Class<T>, val function: (sender: T, context: CommandContext<T>, argument: String) -> Boolean) : CommandBinder<T>(bind) {

    @Suppress("UNCHECKED_CAST")
    fun exec(context: CommandContext<*>, argument: String): Boolean? {
        val sender = cast(context)
        return if (sender != null) {
            function.invoke(sender, (context as CommandContext<T>).copy(sender = sender), argument)
        } else {
            null
        }
    }
}
