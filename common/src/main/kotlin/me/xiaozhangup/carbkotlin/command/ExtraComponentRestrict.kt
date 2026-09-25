/*
 * Derived from TabooLib 6.3.0-test-6-23-1 (MIT), Copyright (c) 2018 Bkm016.
 * Adapted for CarbKotlin shared commands and platform backends, 2026-09-25.
 * See src/main/resources/META-INF/licenses/TabooLib-command-LICENSE.txt.
 */
package me.xiaozhangup.carbkotlin.command

import net.kyori.adventure.audience.Audience
import me.xiaozhangup.carbkotlin.command.component.CommandComponentDynamic
import kotlin.enums.EnumEntries

/**
 * 创建参数约束（仅 int 类型）
 */
fun CommandComponentDynamic.restrictInt(): CommandComponentDynamic {
    return restrict<Audience> { _, _, args -> args.toIntOrNull() != null }
}

/**
 * 创建参数约束（仅 double 类型）
 */
fun CommandComponentDynamic.restrictDouble(): CommandComponentDynamic {
    return restrict<Audience> { _, _, args -> args.toDoubleOrNull() != null }
}

/**
 * 创建参数约束（仅 boolean 类型）
 */
fun CommandComponentDynamic.restrictBoolean(): CommandComponentDynamic {
    return restrict<Audience> { _, _, args -> args.toBooleanStrictOrNull() != null }
}
