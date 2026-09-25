package me.xiaozhangup.carbkotlin.command

import me.xiaozhangup.carbkotlin.command.component.CommandComponentDynamic
import org.bukkit.Bukkit

/**
 * 创建参数补全（仅世界名称）
 *
 * @param suggest 额外建议
 */
fun CommandComponentDynamic.suggestWorlds(suggest: List<String> = emptyList()): CommandComponentDynamic {
    return suggest {
        Bukkit.getWorlds().map { it.name } + suggest
    }
}
