/*
 * Derived from TabooLib 6.3.0-test-6-23-1 (MIT), Copyright (c) 2018 Bkm016.
 * Adapted for CrabKotlin shared commands and platform backends, 2026-09-25.
 * See src/main/resources/META-INF/licenses/TabooLib-command-LICENSE.txt.
 */
package me.xiaozhangup.crab.command

import me.xiaozhangup.crab.command.component.CommandComponent
import me.xiaozhangup.crab.command.Notify
import java.lang.reflect.Modifier
import java.util.Collections
import java.util.IdentityHashMap

/** Annotation entry point; discovery belongs to the plugin, not to this command library. */
class SimpleCommandRegister(private val registry: CommandRegistry) {
    fun register(type: Class<*>, notify: Notify? = null) {
        val instance = type.declaredFields.firstOrNull { it.name == "INSTANCE" && Modifier.isStatic(it.modifiers) }
            ?.also { it.isAccessible = true }?.get(null)
            ?: type.getDeclaredConstructor().also { it.isAccessible = true }.newInstance()
        register(instance, notify)
    }

    fun register(instance: Any, notify: Notify? = null) {
        val header = requireNotNull(instance.javaClass.getAnnotation(CommandHeader::class.java)) {
            "Missing @CommandHeader on ${instance.javaClass.name}"
        }
        var main: SimpleCommandMain? = null
        val visiting = Collections.newSetFromMap(IdentityHashMap<Any, Boolean>())
        fun bodies(owner: Any, root: Boolean): List<SimpleCommandBody> {
            require(visiting.add(owner)) { "Cyclic @CommandBody in ${owner.javaClass.name}" }
            try {
                return owner.javaClass.declaredFields.mapNotNull { field ->
                    val annotation = field.getAnnotation(CommandBody::class.java) ?: return@mapNotNull null
                    field.isAccessible = true
                    val value = requireNotNull(field.get(if (Modifier.isStatic(field.modifiers)) null else owner)) {
                        "Null @CommandBody field ${field.name}"
                    }
                    if (value is SimpleCommandMain) {
                        require(root) { "mainCommand must be declared at the command root" }
                        require(main == null) { "Multiple mainCommand fields in ${owner.javaClass.name}" }
                        main = value
                        return@mapNotNull null
                    }
                    SimpleCommandBody((value as? SimpleCommandBody)?.func ?: {}).apply {
                        name = field.name
                        aliases = annotation.aliases
                        optional = annotation.optional
                        permission = annotation.permission
                        permissionDefault = annotation.permissionDefault
                        hidden = annotation.hidden
                        description = annotation.description
                        if (value is SimpleCommandBody) children += value.children
                        else children += bodies(value, false)
                    }
                }
            } finally {
                visiting.remove(owner)
            }
        }

        val body = bodies(instance, true)
        val permissions = linkedMapOf<String, PermissionDefault>()
        fun collectPermissions(body: SimpleCommandBody) {
            if (body.permission.isNotEmpty()) permissions[body.permission] = body.permissionDefault
            body.children.forEach(::collectPermissions)
        }
        body.forEach(::collectPermissions)
        registry.command(
            header.name, header.aliases.toList(), header.description, header.usage,
            header.permission, header.permissionMessage, header.permissionDefault, permissions, header.newParser, notify,
        ) {
            main?.func?.invoke(this)
            fun add(body: SimpleCommandBody, parent: CommandComponent) {
                parent.literal(
                    body.name, *body.aliases, optional = body.optional, permission = body.permission,
                    hidden = body.hidden, description = body.description,
                ) {
                    body.func(this)
                    body.children.forEach { add(it, this) }
                }
            }
            body.forEach { add(it, this) }
        }
    }
}
