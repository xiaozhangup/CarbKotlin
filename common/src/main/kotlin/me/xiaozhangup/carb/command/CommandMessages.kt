/*
 * Adapted from TabooLib 6.3.0-test-6-23-1 (MIT), Copyright (c) 2018 Bkm016.
 * Paper Adventure messages, 2026-09-19.
 * See src/main/resources/META-INF/licenses/TabooLib-command-LICENSE.txt.
 */
package me.xiaozhangup.carb.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.audience.Audience
import java.util.Locale

object CommandMessages {
    const val defaultPermissionMessage = "§cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error."


    fun component(text: String): Component = LegacyComponentSerializer.legacySection().deserialize(text)

    /** Notify owns all colours; preserve literal text without carrying legacy styles into MiniMessage. */
    private fun notifyText(text: String): String = MiniMessage.miniMessage().escapeTags(
        PlainTextComponentSerializer.plainText().serialize(component(text))
    )

    fun noPermission(sender: Audience, command: CommandStructure, permission: String = command.permission) {
        val message = command.permissionMessage.ifEmpty { "你没有执行此命令的权限!" }
            .replace("<permission>", permission)
        if (command.notify != null) {
            command.notify.send(sender, notifyText(message))
        } else {
            sender.sendMessage(component(command.permissionMessage.ifEmpty { defaultPermissionMessage }.replace("<permission>", permission)))
        }
    }

    fun usage(sender: Audience, command: CommandStructure, message: String) {
        val notify = command.notify
        if (notify != null) notify.send(sender, notifyText(message))
        else sender.sendMessage(component(message))
    }

    fun incorrectSender(context: CommandContext<*>) {
        val notify = context.command.notify
        if (notify != null) {
            notify.send(context.sender(), "当前命令发送者无法执行此命令!")
        } else {
            context.sender().sendMessage(component("""
                §c不匹配的命令发送者类型。
                §cIncorrect sender for command.
            """.commandText()))
        }
    }

    fun emptyCommand(context: CommandContext<*>) {
        val notify = context.command.notify
        if (notify != null) {
            notify.send(context.sender(), "此命令尚未设置执行操作!")
        } else {
            context.sender().sendMessage(component("""
                §c空命令（无执行器）。
                §cEmpty command (no executor).
            """.commandText()))
        }
    }

    fun unknownCommand(context: CommandContext<*>, command: String, state: Int) {
        val notify = context.command.notify
        if (notify == null) {
            unknownCommand(context.sender(), command, state)
            return
        }
        val message = when (state) {
            1 -> "命令不完整，请检查用法!"
            2 -> "命令参数错误，请检查输入!"
            else -> return
        }
        // The command contains player input; keep MiniMessage tags literal.
        notify.send(context.sender(), "$message {0}", notifyText("/$command"))
    }

    fun unknownCommand(sender: Audience, command: String, state: Int) {
        val key = when (state) {
            1 -> "command.unknown.command"
            2 -> "command.unknown.argument"
            else -> return
        }
        sender.sendMessage(Component.translatable(key, NamedTextColor.RED))
        sender.sendMessage(
            component("§7$command").append(
                Component.translatable("command.context.here", NamedTextColor.RED).decorate(TextDecoration.ITALIC)
            )
        )
    }
}

/** Retains the upstream Chinese/English fallback messages based on the server locale. */
internal fun String.commandText(): String = trimIndent().lines().let {
    if (Locale.getDefault().language == "zh") it.first() else it.last()
}
