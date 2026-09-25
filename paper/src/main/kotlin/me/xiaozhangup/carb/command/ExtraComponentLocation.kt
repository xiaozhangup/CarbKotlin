/*
 * Derived from TabooLib 6.3.0-test-6-23-1 (MIT), Copyright (c) 2018 Bkm016.
 * Adapted for CarbKotlin shared commands and platform backends, 2026-09-25.
 * See src/main/resources/META-INF/licenses/TabooLib-command-LICENSE.txt.
 */
package me.xiaozhangup.carb.command


import org.bukkit.entity.Player
import me.xiaozhangup.carb.command.component.CommandComponent
import me.xiaozhangup.carb.command.component.CommandComponentDynamic

/**
 * 添加一层世界节点（自动约束、自动建议）
 *
 * @param suggest 额外建议
 */
fun CommandComponent.world(
    comment: String = "world",
    suggest: List<String> = listOf("~"),
    optional: Boolean = false,
    permission: String = "",
    dynamic: CommandComponentDynamic.() -> Unit = {}
): CommandComponentDynamic {
    return dynamic(comment = comment, optional = optional, permission = permission, dynamic = dynamic).suggestWorlds(suggest)
}

/**
 * 添加一层坐标 X,Y,Z 节点（自动约束、自动建议）
 */
fun CommandComponent.xyz(
    x: String = "x",
    y: String = "y",
    z: String = "z",
    optional: Boolean = false,
    permission: String = "",
    dynamic: CommandComponentDynamic.() -> Unit = {}
): CommandComponentDynamic {
    return decimal(x, optional = optional, permission = permission)
        .suggestionUncheck<Player> { sender, _ -> of("~", sender.location.x) }
        .decimal(y)
        .suggestionUncheck<Player> { sender, _ -> of("~", sender.location.y) }
        .decimal(z, dynamic = dynamic)
        .suggestionUncheck<Player> { sender, _ -> of("~", sender.location.z) }
}

/**
 * 添加一层坐标 YAW,PITCH 节点（自动约束、自动建议）
 */
fun CommandComponent.euler(
    yaw: String = "yaw",
    pitch: String = "pitch",
    optional: Boolean = false,
    permission: String = "",
    dynamic: CommandComponentDynamic.() -> Unit = {}
): CommandComponentDynamic {
    return decimal(yaw, optional = optional, permission = permission)
        .suggestionUncheck<Player> { sender, _ -> of("~", sender.location.yaw) }
        .decimal(pitch, dynamic = dynamic)
        .suggestionUncheck<Player> { sender, _ -> of("~", sender.location.pitch) }
}

/**
 * 添加一层坐标节点（自动约束、自动建议）
 */
fun CommandComponent.location(
    world: String = "world",
    x: String = "x",
    y: String = "y",
    z: String = "z",
    yaw: String = "yaw",
    pitch: String = "pitch",
    euler: Boolean = true,
    optional: Boolean = false,
    permission: String = "",
    dynamic: CommandComponentDynamic.() -> Unit = {}
): CommandComponentDynamic {
    return world(world, optional = optional, permission = permission).xyz(x, y, z, dynamic = dynamic).also {
        if (euler) {
            it.euler(yaw, pitch, dynamic = dynamic)
        }
    }
}

/**
 * 添加一层坐标节点（自动约束、自动建议）
 */
fun CommandComponent.locationWithoutWorld(
    x: String = "x",
    y: String = "y",
    z: String = "z",
    yaw: String = "yaw",
    pitch: String = "pitch",
    euler: Boolean = true,
    optional: Boolean = false,
    permission: String = "",
    dynamic: CommandComponentDynamic.() -> Unit = {}
): CommandComponentDynamic {
    return xyz(x, y, z, optional, permission, dynamic).also {
        if (euler) {
            it.euler(yaw, pitch, dynamic = dynamic)
        }
    }
}

private fun of(vararg elements: Any): List<String> {
    return if (elements.isNotEmpty()) elements.map { it.toString() } else emptyList()
}
