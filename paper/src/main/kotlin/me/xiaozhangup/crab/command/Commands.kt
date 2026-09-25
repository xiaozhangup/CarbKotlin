/*
 * Adapted from TabooLib 6.3.0-test-6-23-1 (MIT), Copyright (c) 2018 Bkm016.
 * Paper-only registration and lifecycle implementation, 2026-09-19.
 * See src/main/resources/META-INF/licenses/TabooLib-command-LICENSE.txt.
 */
package me.xiaozhangup.crab.command


import me.xiaozhangup.crab.command.component.CommandBase
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandException
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginIdentifiableCommand
import org.bukkit.permissions.Permission
import org.bukkit.plugin.Plugin
import java.util.Locale

/** Paper command-map backend. Initialise once during plugin load; close during plugin disable. */
class Commands(private val owner: Plugin) : CommandRegistry() {
    private val registrations = linkedMapOf<String, Registration>()
    private val permissions = linkedMapOf<String, Permission>()
    private var refreshPending = false

    var permissionProvider: (CommandStructure) -> String = {
        "${owner.name.lowercase(Locale.ROOT)}.command.${it.name}.use"
    }

    override val registeredCommands: List<CommandStructure>
        @Synchronized get() = registrations.values.map { it.command.structure }

    @Synchronized
    override fun register(structure: CommandStructure, executor: CommandExecutor, completer: CommandCompleter) {
        val labels = (listOf(structure.name) + structure.aliases).map { it.lowercase(Locale.ROOT) }
        require(labels.all { it.isNotBlank() && it.none(Char::isWhitespace) && ':' !in it }) {
            "Command names and aliases must be nonempty, unqualified words"
        }
        unregister(structure.name)
        val permission = structure.permission.ifEmpty { permissionProvider(structure) }
        registerPermission(permission, structure.permissionDefault)
        structure.permissionChildren.forEach(::registerPermission)

        val command = PaperCommand(owner, structure, permission, executor, completer)
        val map = Bukkit.getCommandMap()
        val previous = linkedMapOf<String, Command?>()
        // Match TabooLib's overriding behaviour, including aliases such as /help and /list.
        val keys = (listOf("${owner.name.lowercase(Locale.ROOT)}:${structure.name}") + labels).distinct()
        command.register(map)
        for (key in keys) previous[key] = map.knownCommands.put(key, command)
        registrations[structure.name] = Registration(command, previous).also { activeRegistrations.add(it) }
        refreshPlayers()
    }

    @Synchronized
    override fun unregister(name: String) {
        val key = name.lowercase(Locale.ROOT)
        val registration = registrations[key] ?: registrations.values.firstOrNull {
            it.previous.containsKey(key)
        } ?: return
        registrations.remove(registration.command.structure.name)
        val map = Bukkit.getCommandMap()
        for ((label, previous) in registration.previous) {
            if (map.knownCommands[label] === registration.command) {
                if (previous == null) map.knownCommands.remove(label)
                else map.knownCommands[label] = previous
            }
            // An overlapping alias must not resurrect an already unregistered local command.
            activeRegistrations.forEach {
                if (it.previous[label] === registration.command) it.previous[label] = previous
            }
        }
        activeRegistrations.remove(registration)
        registration.command.unregister(map)
        refreshPlayers()
    }

    @Synchronized
    override fun unregisterAll() {
        registrations.keys.toList().asReversed().forEach(::unregister)
        val manager = Bukkit.getPluginManager()
        permissions.forEach { (name, permission) ->
            if (manager.getPermission(name) === permission) manager.removePermission(permission)
        }
        permissions.clear()
    }

    private fun registerPermission(name: String, default: PermissionDefault) {
        if (name.isEmpty()) return
        val manager = Bukkit.getPluginManager()
        if (manager.getPermission(name) != null) return
        val permission = Permission(name, org.bukkit.permissions.PermissionDefault.valueOf(default.name))
        manager.addPermission(permission)
        manager.recalculatePermissionDefaults(permission)
        permissions[name] = permission
    }

    private fun refreshPlayers() {
        if (!owner.isEnabled || refreshPending) return
        refreshPending = true
        // Paper's command map updates Brigadier directly; only the clients need refreshing.
        Bukkit.getGlobalRegionScheduler().run(owner) {
            synchronized(this) { refreshPending = false }
            Bukkit.getOnlinePlayers().forEach { player ->
                player.scheduler.run(owner, { player.updateCommands() }, null)
            }
        }
    }

    override fun hasPermission(sender: net.kyori.adventure.audience.Audience, permission: String) =
        (sender as CommandSender).hasPermission(permission)

    override fun playerNames(): List<String> = Bukkit.getOnlinePlayers().map { it.name }

    private companion object {
        val activeRegistrations = mutableListOf<Registration>()
    }

    private data class Registration(val command: PaperCommand, val previous: MutableMap<String, Command?>)

    @Suppress("DEPRECATION") // Preserve permissionMessage for console/dispatchCommand callers.
    private class PaperCommand(
        private val owner: Plugin,
        val structure: CommandStructure,
        private val requiredPermission: String,
        private val executor: CommandExecutor,
        private val completer: CommandCompleter,
    ) : Command(structure.name, structure.description.ifEmpty { structure.name }, structure.usage, structure.aliases),
        PluginIdentifiableCommand {

        init {
            // Let opted-in commands reach execute so Paper cannot hide the denial message.
            setPermission(if (structure.notify == null) requiredPermission else null)
            permissionMessage(CommandMessages.component(structure.permissionMessage.ifEmpty { CommandMessages.defaultPermissionMessage }))
        }

        override fun getPlugin(): Plugin = owner

        private fun hasRequiredPermission(sender: CommandSender): Boolean =
            requiredPermission.isEmpty() || requiredPermission.split(';').any { sender.hasPermission(it) }

        override fun testPermission(target: CommandSender): Boolean {
            if (structure.notify == null) return super.testPermission(target)
            if (hasRequiredPermission(target)) return true
            CommandMessages.noPermission(target, structure, requiredPermission)
            return false
        }

        override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
            check(owner.isEnabled) { "Cannot execute /$commandLabel: ${owner.name} is disabled" }
            if (!testPermission(sender)) return true
            val result = try {
                executor.execute(sender, structure, commandLabel, args)
            } catch (exception: Exception) {
                throw CommandException("Unhandled exception executing /$commandLabel in ${owner.name}", exception)
            }
            if (!result && usage.isNotEmpty()) {
                usage.replace("<command>", commandLabel).lines().forEach { CommandMessages.usage(sender, structure, it) }
            }
            return result
        }

        override fun tabComplete(sender: CommandSender, alias: String, args: Array<String>): List<String> {
            if (!owner.isEnabled || !hasRequiredPermission(sender)) return emptyList()
            return try {
                completer.execute(sender, structure, alias, args) ?: emptyList()
            } catch (exception: Exception) {
                throw CommandException("Unhandled exception completing /$alias in ${owner.name}", exception)
            }
        }
    }
}

