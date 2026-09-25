package me.xiaozhangup.crab.common.util

import net.kyori.adventure.text.Component
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.ComponentKt
import plutoproject.adventurekt.text.actions
import plutoproject.adventurekt.text.newlineAction
import plutoproject.adventurekt.util.cleanBuild

fun componentList(lore: ComponentKt.() -> Unit): MutableList<Component> {
    val components = mutableListOf<Component>()
    val last = component {
        actions {
            newlineAction {
                components.add(it.cleanBuild())
            }
        }
        lore()
    }
    components.add(last)
    return components
}

