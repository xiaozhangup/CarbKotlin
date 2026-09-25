/*
 * Derived from TabooLib 6.3.0-test-6-23-1 (MIT), Copyright (c) 2018 Bkm016.
 * Adapted for CarbKotlin shared commands and platform backends, 2026-09-25.
 * See src/main/resources/META-INF/licenses/TabooLib-command-LICENSE.txt.
 */
package me.xiaozhangup.carbkotlin.command.component

import me.xiaozhangup.carbkotlin.command.*
import kotlin.enums.EnumEntries

/**
 * 默认的命令建议提供者实现
 */
class DefaultCommandSuggestProvider : CommandSuggestProvider {

    override fun provideIntSuggest(component: CommandComponentDynamic, comment: String, suggest: List<String>) {
        // 如果没有额外建议则约束参数输入
        if (suggest.isEmpty()) {
            component.restrictInt()
        } else {
            component.suggestUncheck { suggest }
        }
    }

    override fun provideDecimalSuggest(component: CommandComponentDynamic, comment: String, suggest: List<String>) {
        // 如果没有额外建议则约束参数输入
        if (suggest.isEmpty()) {
            component.restrictDouble()
        } else {
            component.suggestUncheck { suggest }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun provideEnumSuggest(component: CommandComponentDynamic, enums: EnumEntries<*>, comment: String, suggest: List<String>) {
        component.suggestEnums(enums, suggest)
    }

    override fun provideBoolSuggest(component: CommandComponentDynamic, comment: String) {
        component.suggestBoolean()
    }

    override fun providePlayerSuggest(component: CommandComponentDynamic, comment: String, suggest: List<String>) {
        component.suggestPlayers(suggest)
    }
}
