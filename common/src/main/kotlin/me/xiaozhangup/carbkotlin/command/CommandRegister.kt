/*
 * Derived from TabooLib 6.3.0-test-6-23-1 (MIT), Copyright (c) 2018 Bkm016.
 * Adapted for CarbKotlin shared commands and platform backends, 2026-09-25.
 * See src/main/resources/META-INF/licenses/TabooLib-command-LICENSE.txt.
 */
package me.xiaozhangup.carbkotlin.command

import net.kyori.adventure.audience.Audience
import me.xiaozhangup.carbkotlin.command.Notify
import me.xiaozhangup.carbkotlin.command.component.CommandBase

/**
 * 注册一个命令
 *
 * @param name 命令名
 * @param aliases 命令别名
 * @param description 命令描述
 * @param usage 命令用法
 * @param permission 命令权限
 * @param permissionMessage 命令权限提示
 * @param permissionDefault 命令权限默认值
 * @param permissionChildren 命令权限子节点
 * @param notify 默认错误提示使用的通知样式；传入后根权限在执行前检查
 * @param commandBuilder 命令构建器
 */
fun CommandRegistry.command(
    name: String,
    aliases: List<String> = emptyList(),
    description: String = "",
    usage: String = "",
    permission: String = "",
    permissionMessage: String = "",
    permissionDefault: PermissionDefault = PermissionDefault.OP,
    permissionChildren: Map<String, PermissionDefault> = emptyMap(),
    newParser: Boolean = false,
    notify: Notify? = null,
    commandBuilder: CommandBase.() -> Unit,
) {
    registerCommand(
        // 创建命令结构
        CommandStructure(this, name, aliases, description, usage, permission, permissionMessage, permissionDefault, permissionChildren, newParser, notify),
        // 创建执行器
        object : CommandExecutor {

            override fun execute(sender: Audience, command: CommandStructure, name: String, args: Array<String>): Boolean {
                val commandBase = CommandBase().also(commandBuilder)
                return commandBase.execute(CommandContext(sender, command, name, commandBase, newParser, args))
            }
        },
        // 创建补全器
        object : CommandCompleter {

            override fun execute(sender: Audience, command: CommandStructure, name: String, args: Array<String>): List<String>? {
                val commandBase = CommandBase().also(commandBuilder)
                return commandBase.suggest(CommandContext(sender, command, name, commandBase, newParser, args))
            }
        },
        // 传入原始命令构建器
        commandBuilder
    )
}

/**
 * 注册一个简易命令
 *
 * @param name 命令名
 * @param aliases 命令别名
 * @param description 命令描述
 * @param usage 命令用法
 * @param permission 命令权限
 * @param permissionMessage 命令权限提示
 * @param permissionDefault 命令权限默认值
 * @param permissionChildren 命令权限子节点
 * @param notify 默认错误提示使用的通知样式
 * @param executor 命令构建器
 */
fun CommandRegistry.simpleCommand(
    name: String,
    aliases: List<String> = emptyList(),
    description: String = "",
    usage: String = "",
    permission: String = "",
    permissionMessage: String = "",
    permissionDefault: PermissionDefault = PermissionDefault.OP,
    permissionChildren: Map<String, PermissionDefault> = emptyMap(),
    completer: CommandCompleter? = null,
    notify: Notify? = null,
    executor: (sender: Audience, args: Array<String>) -> Unit,
) {
    registerCommand(
        // 创建命令结构
        CommandStructure(this, name, aliases, description, usage, permission, permissionMessage, permissionDefault, permissionChildren, false, notify),
        // 创建执行器
        object : CommandExecutor {

            override fun execute(sender: Audience, command: CommandStructure, name: String, args: Array<String>): Boolean {
                executor(sender, args)
                return true
            }
        },
        // 创建补全器
        completer ?: object : CommandCompleter {

            override fun execute(sender: Audience, command: CommandStructure, name: String, args: Array<String>): List<String> {
                return emptyList()
            }
        },
    ) {}
}
