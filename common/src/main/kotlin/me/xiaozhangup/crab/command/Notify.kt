package me.xiaozhangup.crab.command

import me.xiaozhangup.crab.command.internal.ColorTransform
import net.kyori.adventure.text.Component
import net.kyori.adventure.audience.Audience
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.mini
import plutoproject.adventurekt.text.raw

open class Notify(
    prefix: String,
    val color: String
) {
    private val header = "<dark_gray>[<color:$color>$prefix</color>]</dark_gray> "
    val colorMessage = ColorTransform.white(color, 10, 8)
    val colorArgs = ColorTransform.white(color, 10, 6)
    val prefix by lazy {
        component {
            mini(header)
        }
    }

    fun build(message: String, vararg placeholder: Any?): Component {
        return component {
            raw(prefix)
            mini(
                "<color:$colorMessage>${
                    message.replaceWithOrder(*placeholder.map { "<color:$colorArgs>$it</color>" }.toTypedArray())
                }</color>"
            )
        }
    }

    fun send(sender: Audience, message: String, vararg placeholder: Any?) {
        sender.sendMessage(build(message, *placeholder))
    }

}

private fun String.replaceWithOrder(vararg args: Any?): String =
    Regex("\\{(\\d+)}").replace(this) { match ->
        val index = match.groupValues[1].toIntOrNull()
        if (index != null && index in args.indices) args[index].toString() else match.value
    }
