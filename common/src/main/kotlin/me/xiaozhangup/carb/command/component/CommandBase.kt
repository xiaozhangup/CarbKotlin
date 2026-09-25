/*
 * Derived from TabooLib 6.3.0-test-6-23-1 (MIT), Copyright (c) 2018 Bkm016.
 * Adapted for CarbKotlin shared commands and platform backends, 2026-09-25.
 * See src/main/resources/META-INF/licenses/TabooLib-command-LICENSE.txt.
 */
package me.xiaozhangup.carb.command.component

import net.kyori.adventure.audience.Audience
import me.xiaozhangup.carb.command.CommandContext
import me.xiaozhangup.carb.command.CommandMessages

@Suppress("DuplicatedCode")
class CommandBase : CommandComponent(-1, false) {

    internal var result = true

    internal var commandIncorrectSender: CommandUnknownNotify<*> =
        CommandUnknownNotify(Audience::class.java) { _, context, _, _ ->
            CommandMessages.incorrectSender(context)
        }

    internal var commandIncorrectCommand: CommandUnknownNotify<*> =
        CommandUnknownNotify(Audience::class.java) { _, context, index, state ->
            val args = context.realArgs.take(index.coerceAtLeast(0))
            var str = context.name
            if (args.size > 1) {
                str += " "
                str += args.dropLast(1).joinToString(" ").trim()
            }
            if (str.length > 10) {
                str = "...${str.substring(str.length - 10, str.length)}"
            }
            if (args.isNotEmpty()) {
                str += " "
                str += if (context.command.notify == null) "§c§n${args.last()}" else args.last()
            }
            CommandMessages.unknownCommand(context, str, state)
        }

    fun execute(context: CommandContext<*>): Boolean {
        result = true
        // 空参数是一种特殊的状态，指的是玩家输入根命令且不附带任何参数，例如 [/test] 而不是 [/test ]
        if (context.realArgs.isEmpty()) {
            // 获取下级节点
            val children = findChildren(context)
            // 下级节点为空 || 下级节点存在可选（optional）|| 当前节点存在执行器
            return if (children.isEmpty() || children.any { it.optional } || commandExecutor != null) {
                context.index = 0
                // 缺少 execute 代码块
                if (commandExecutor == null) {
                    CommandMessages.emptyCommand(context)
                } else {
                    commandExecutor!!.exec(this, context, "")
                }
                result
            } else {
                commandIncorrectCommand.exec(context, -1, 1)
                false
            }
        }
        fun process(cur: Int, component: CommandComponent): Boolean {
            // 更新参数内容
            context.index = cur
            context.currentComponent = component
            // 检索节点
            val find = component.findChildren(context, context.realArgs[cur])
            return if (find != null) {
                // 获取下级节点
                val children = find.findChildren(context)
                // 存在下级输入参数 && 下级节点有效
                if (cur + 1 < context.realArgs.size &&
                    (children.isNotEmpty() || (context.command.notify != null &&
                        find.findDeniedChild(context, context.realArgs[cur + 1]) != null))
                ) {
                    process(cur + 1, find)
                } else {
                    // 下级节点为空 || 下级节点存在可选（optional）|| 当前节点存在执行器
                    if (children.isEmpty() || children.any { it.optional } || find.commandExecutor != null) {
                        context.currentComponent = find
                        // 缺少 execute 代码块
                        if (find.commandExecutor == null) {
                            CommandMessages.emptyCommand(context)
                        } else {
                            find.commandExecutor!!.exec(this, context, context.self())
                        }
                        result
                    } else {
                        commandIncorrectCommand.exec(context, cur + 1, 1)
                        false
                    }
                }
            } else {
                val denied = if (context.command.notify != null) component.findDeniedChild(context, context.realArgs[cur]) else null
                if (denied != null) {
                    CommandMessages.noPermission(context.sender(), context.command, denied.permission)
                    true
                } else {
                    commandIncorrectCommand.exec(context, cur + 1, 2)
                    false
                }
            }
        }
        return process(0, this)
    }

    fun suggest(context: CommandContext<*>): List<String>? {
        // 空参数不需要触发补全机制
        if (context.realArgs.isEmpty()) {
            return null
        }
        fun process(cur: Int, component: CommandComponent): List<String>? {
            context.index = cur
            context.currentComponent = component
            // 获取当前输入参数
            val current = context.realArgs[cur]
            // 检索节点
            val find = component.findChildren(context, current)
            if (find != null) {
                context.currentComponent = find
            }
            return when {
                find != null && cur + 1 < context.realArgs.size -> {
                    process(cur + 1, find)
                }
                cur + 1 == context.realArgs.size -> {
                    val suggest = component.findChildren(context).flatMap {
                        when (it) {
                            is CommandComponentLiteral -> if (it.hidden) emptyList() else it.aliases.toList()
                            is CommandComponentDynamic -> it.commandSuggestion?.exec(context) ?: emptyList()
                            else -> emptyList()
                        }
                    }
                    suggest.filter { current.isEmpty() || it.contains(current, ignoreCase = true) }.ifEmpty { null }
                }
                else -> null
            }
        }
        return process(0, this)
    }

    fun incorrectSender(function: (sender: Audience, context: CommandContext<Audience>) -> Unit) {
        this.commandIncorrectSender = CommandUnknownNotify(Audience::class.java) { sender, context, _, _ -> function(sender, context) }
    }

    fun incorrectCommand(function: (sender: Audience, context: CommandContext<Audience>, index: Int, state: Int) -> Unit) {
        this.commandIncorrectCommand = CommandUnknownNotify(Audience::class.java, function)
    }

    fun setResult(value: Boolean) {
        result = value
    }
}
