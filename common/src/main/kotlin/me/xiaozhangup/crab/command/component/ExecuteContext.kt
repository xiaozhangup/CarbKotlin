/*
 * Derived from TabooLib 6.3.0-test-6-23-1 (MIT), Copyright (c) 2018 Bkm016.
 * Adapted for CrabKotlin shared commands and platform backends, 2026-09-25.
 * See src/main/resources/META-INF/licenses/TabooLib-command-LICENSE.txt.
 */
package me.xiaozhangup.crab.command.component

import me.xiaozhangup.crab.command.CommandContext

/**
 * TabooLib
 * me.xiaozhangup.crab.command.component.ExecuteContext
 *
 * @author 坏黑
 * @since 2024/3/24 15:42
 */
data class ExecuteContext<T>(val sender: T, val ctx: CommandContext<T>, val args: String)
