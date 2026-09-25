/*
 * Derived from TabooLib 6.3.0-test-6-23-1 (MIT), Copyright (c) 2018 Bkm016.
 * Adapted for CarbKotlin shared commands and platform backends, 2026-09-25.
 * See src/main/resources/META-INF/licenses/TabooLib-command-LICENSE.txt.
 */
package me.xiaozhangup.carb.command.component

import me.xiaozhangup.carb.command.CommandContext

class CommandExecutor<T>(bind: Class<T>, val function: (sender: T, context: CommandContext<T>, argument: String) -> Unit) : CommandBinder<T>(bind) {

    @Suppress("UNCHECKED_CAST")
    fun exec(commandBase: CommandBase, context: CommandContext<*>, argument: String) {
        val sender = cast(context)
        if (sender != null) {
            function.invoke(sender, (context as CommandContext<T>).copy(sender = sender), argument)
        } else {
            commandBase.commandIncorrectSender.exec(context, 0, 0)
        }
    }
}
