package me.xiaozhangup.carbkotlin.command

import me.xiaozhangup.carbkotlin.command.component.CommandBase
import me.xiaozhangup.carbkotlin.reflect.PluginScanner
import net.kyori.adventure.audience.Audience

/** One registry per plugin. DSL and argument parsing are shared by both platforms. */
abstract class CommandRegistry : AutoCloseable {
    var languageResolver: (Audience, String) -> String = { _, key -> key }
    abstract val registeredCommands: List<CommandStructure>
    abstract fun register(structure: CommandStructure, executor: CommandExecutor, completer: CommandCompleter)
    abstract fun unregister(name: String)
    abstract fun unregisterAll()
    abstract fun hasPermission(sender: Audience, permission: String): Boolean
    abstract fun playerNames(): List<String>

    fun registerAnnotated(instance: Any, notify: Notify? = null) = SimpleCommandRegister(this).register(instance, notify)
    fun registerAnnotated(type: Class<*>, notify: Notify? = null) = SimpleCommandRegister(this).register(type, notify)
    fun registerAnnotated(scanner: PluginScanner, notify: Notify? = null) {
        scanner.visitAnnotated(CommandHeader::class.java) { registerAnnotated(scanner.instance(it), notify) }
    }

    override fun close() = unregisterAll()
}

@Suppress("UNUSED_PARAMETER")
fun CommandRegistry.registerCommand(command: CommandStructure, executor: CommandExecutor,
    completer: CommandCompleter, commandBuilder: CommandBase.() -> Unit = {}) = register(command, executor, completer)
fun CommandRegistry.unregisterCommand(command: String) = unregister(command)
fun CommandRegistry.unregisterCommand(command: CommandStructure) = unregister(command.name)
fun CommandRegistry.unregisterCommands() = unregisterAll()
