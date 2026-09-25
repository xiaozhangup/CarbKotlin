package me.xiaozhangup.carbkotlin.command

import com.velocitypowered.api.command.CommandMeta
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.audience.Audience
import java.util.Locale

/** Uses the same command tree as Paper, with native Velocity senders and registrations. */
class Commands(private val owner: Any) : CommandRegistry() {
    internal val server: ProxyServer = me.xiaozhangup.crab.CarbKotlin.getServer()
    private val registrations = linkedMapOf<String, Pair<CommandStructure, CommandMeta>>()
    override val registeredCommands: List<CommandStructure>
        @Synchronized get() = registrations.values.map { it.first }

    override fun hasPermission(sender: Audience, permission: String): Boolean =
        (sender as CommandSource).hasPermission(permission)

    override fun playerNames(): List<String> = server.allPlayers.map { it.username }

    @Synchronized
    override fun register(structure: CommandStructure, executor: CommandExecutor, completer: CommandCompleter) {
        val labels = (listOf(structure.name) + structure.aliases).map { it.lowercase(Locale.ROOT) }
        require(labels.all { it.isNotBlank() && it.none(Char::isWhitespace) && ':' !in it }) {
            "Command names and aliases must be nonempty, unqualified words"
        }
        unregister(structure.name)
        val manager = server.commandManager
        val meta = manager.metaBuilder(structure.name).aliases(*structure.aliases.toTypedArray()).plugin(owner).build()
        val permission = structure.permission
        manager.register(meta, object : SimpleCommand {
            private fun allowed(source: CommandSource): Boolean =
                permission.isEmpty() || source.hasPermission(permission)

            override fun hasPermission(invocation: SimpleCommand.Invocation): Boolean =
                structure.notify != null || allowed(invocation.source())

            override fun execute(invocation: SimpleCommand.Invocation) {
                val sender = invocation.source()
                if (!allowed(sender)) {
                    CommandMessages.noPermission(sender, structure, permission)
                    return
                }
                val result = executor.execute(sender, structure, invocation.alias(), invocation.arguments())
                if (!result && structure.usage.isNotEmpty()) {
                    structure.usage.replace("<command>", invocation.alias()).lines().forEach {
                        CommandMessages.usage(sender, structure, it)
                    }
                }
            }

            override fun suggest(invocation: SimpleCommand.Invocation): List<String> {
                if (!allowed(invocation.source())) return emptyList()
                val args = invocation.arguments().ifEmpty { arrayOf("") }
                return completer.execute(invocation.source(), structure, invocation.alias(), args) ?: emptyList()
            }
        })
        registrations[structure.name] = structure to meta
    }

    @Synchronized
    override fun unregister(name: String) {
        val key = name.lowercase(Locale.ROOT)
        val registration = registrations[key] ?: registrations.values.firstOrNull {
            it.second.aliases.contains(key)
        } ?: return
        registrations.remove(registration.first.name)
        val manager = server.commandManager
        registration.second.aliases.forEach { alias ->
            if (manager.getCommandMeta(alias) === registration.second) manager.unregister(alias)
        }
    }

    @Synchronized
    override fun unregisterAll() {
        registrations.keys.toList().asReversed().forEach(::unregister)
    }
}
