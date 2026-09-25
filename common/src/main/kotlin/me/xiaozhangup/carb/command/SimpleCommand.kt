/*
 * Derived from TabooLib 6.3.0-test-6-23-1 (MIT), Copyright (c) 2018 Bkm016.
 * Adapted for CarbKotlin shared commands and platform backends, 2026-09-25.
 * See src/main/resources/META-INF/licenses/TabooLib-command-LICENSE.txt.
 */
package me.xiaozhangup.carb.command

import me.xiaozhangup.carb.command.component.CommandBase
import me.xiaozhangup.carb.command.component.CommandComponent
import me.xiaozhangup.carb.command.component.ExecuteContext

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandHeader(
    val name: String,
    val aliases: Array<String> = [],
    val description: String = "",
    val usage: String = "",
    val permission: String = "",
    val permissionMessage: String = "",
    val permissionDefault: PermissionDefault = PermissionDefault.OP,
    val newParser: Boolean = false,
)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandBody(
    val aliases: Array<String> = [],
    val optional: Boolean = false,
    val permission: String = "",
    val permissionDefault: PermissionDefault = PermissionDefault.OP,
    val hidden: Boolean = false,
    val description: String = "",
)

fun mainCommand(func: CommandBase.() -> Unit): SimpleCommandMain {
    return SimpleCommandMain(func)
}

fun subCommand(func: CommandComponent.() -> Unit): SimpleCommandBody {
    return SimpleCommandBody(func)
}

inline fun <reified T> subCommandExec(crossinline func: ExecuteContext<T>.() -> Unit): SimpleCommandBody {
    return SimpleCommandBody { exec<T> { func() } }
}

class SimpleCommandMain(val func: CommandBase.() -> Unit = {})

class SimpleCommandBody(val func: CommandComponent.() -> Unit = {}) {

    var name = ""
    var aliases = emptyArray<String>()
    var optional = false
    var permission = ""
    var permissionDefault: PermissionDefault = PermissionDefault.OP
    var hidden = false
    var description = ""
    val children = ArrayList<SimpleCommandBody>()

    override fun toString(): String {
        return "SimpleCommandBody(name='$name', children=$children)"
    }
}
