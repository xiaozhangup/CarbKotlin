/*
 * Derived from TabooLib 6.3.0-test-6-23-1 (MIT), Copyright (c) 2018 Bkm016.
 * Adapted for CrabKotlin shared commands and platform backends, 2026-09-25.
 * See src/main/resources/META-INF/licenses/TabooLib-command-LICENSE.txt.
 */
package me.xiaozhangup.crab.command

import me.xiaozhangup.crab.command.component.CommandComponent
import me.xiaozhangup.crab.command.component.CommandComponentDynamic
import me.xiaozhangup.crab.command.component.CommandSuggestProviderLoader
import kotlin.enums.EnumEntries

/**
 * 添加一层整型节点（自动约束）
 *
 * @param suggest 额外建议
 */
fun CommandComponent.int(
    comment: String = "int",
    suggest: List<String> = emptyList(),
    optional: Boolean = false,
    permission: String = "",
    dynamic: CommandComponentDynamic.() -> Unit = {}
): CommandComponentDynamic {
    return dynamic(comment = comment, optional = optional, permission = permission, dynamic = dynamic).also {
        CommandSuggestProviderLoader.getProvider().provideIntSuggest(it, comment, suggest)
    }
}

/**
 * 添加一层数字节点（自动约束）
 *
 * @param suggest 额外建议
 */
fun CommandComponent.decimal(
    comment: String = "decimal",
    suggest: List<String> = emptyList(),
    optional: Boolean = false,
    permission: String = "",
    dynamic: CommandComponentDynamic.() -> Unit = {}
): CommandComponentDynamic {
    return dynamic(comment = comment, optional = optional, permission = permission, dynamic = dynamic).also {
        CommandSuggestProviderLoader.getProvider().provideDecimalSuggest(it, comment, suggest)
    }
}

/**
 * 添加一层布尔值节点（自动约束、自动建议）
 */
fun CommandComponent.bool(
    comment: String = "boolean",
    optional: Boolean = false,
    permission: String = "",
    dynamic: CommandComponentDynamic.() -> Unit = {}
): CommandComponentDynamic {
    return dynamic(comment = comment, optional = optional, permission = permission, dynamic = dynamic).also {
        CommandSuggestProviderLoader.getProvider().provideBoolSuggest(it, comment)
    }
}

/**
 * 添加一层枚举节点（自动约束、自动建议）
 *
 * @param suggest 额外建议
 */
@OptIn(ExperimentalStdlibApi::class)
fun CommandComponent.enum(
    enums: EnumEntries<*>,
    comment: String = "enum",
    suggest: List<String> = emptyList(),
    optional: Boolean = false,
    permission: String = "",
    dynamic: CommandComponentDynamic.() -> Unit = {}
): CommandComponentDynamic {
    return dynamic(comment = comment, optional = optional, permission = permission, dynamic = dynamic).also {
        CommandSuggestProviderLoader.getProvider().provideEnumSuggest(it, enums, comment, suggest)
    }
}

/**
 * 添加一层玩家节点（自动约束、自动建议）
 *
 * @param suggest 额外建议
 */
fun CommandComponent.player(
    comment: String = "player",
    suggest: List<String> = emptyList(),
    optional: Boolean = false,
    permission: String = "",
    dynamic: CommandComponentDynamic.() -> Unit = {}
): CommandComponentDynamic {
    return dynamic(comment = comment, optional = optional, permission = permission, dynamic = dynamic).also {
        CommandSuggestProviderLoader.getProvider().providePlayerSuggest(it, comment, suggest)
    }
}
