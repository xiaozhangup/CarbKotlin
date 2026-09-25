/*
 * Derived from TabooLib 6.3.0-test-6-23-1 (MIT), Copyright (c) 2018 Bkm016.
 * Adapted for CrabKotlin shared commands and platform backends, 2026-09-25.
 * See src/main/resources/META-INF/licenses/TabooLib-command-LICENSE.txt.
 */
package me.xiaozhangup.crab.command

import net.kyori.adventure.audience.Audience
import me.xiaozhangup.crab.command.component.CommandComponentDynamic
import me.xiaozhangup.crab.command.component.SuggestContext
import kotlin.enums.EnumEntries

/**
 * 创建参数补全
 *
 * @param suggest 补全表达式
 */
fun CommandComponentDynamic.suggest(suggest: SuggestContext<Audience>.() -> List<String>?): CommandComponentDynamic {
    return suggestion<Audience> { sender, ctx -> suggest(SuggestContext(sender, ctx)) }
}

/**
 * 创建一个不检查的参数不全
 *
 * @param suggest 补全表达式
 */
fun CommandComponentDynamic.suggestUncheck(suggest: SuggestContext<Audience>.() -> List<String>?): CommandComponentDynamic {
    return suggestion<Audience>(uncheck = true) { sender, ctx -> suggest(SuggestContext(sender, ctx)) }
}

/**
 * 创建参数补全（仅布尔值）
 */
fun CommandComponentDynamic.suggestBoolean(): CommandComponentDynamic {
    return suggest { listOf("true", "false", "t", "f", "1", "0") }
}

/**
 * 创建参数补全（仅枚举）
 *
 * @param suggest 额外建议
 */
@OptIn(ExperimentalStdlibApi::class)
fun CommandComponentDynamic.suggestEnums(enums: EnumEntries<*>, suggest: List<String> = emptyList()): CommandComponentDynamic {
    return suggest {
        enums.map { it.name } + suggest
    }
}

/**
 * 创建参数补全（仅在线玩家名称）
 *
 * @param suggest 额外建议
 */
fun CommandComponentDynamic.suggestPlayers(suggest: List<String> = emptyList()): CommandComponentDynamic {
    return suggest {
        ctx.command.registry.playerNames() + suggest
    }
}

