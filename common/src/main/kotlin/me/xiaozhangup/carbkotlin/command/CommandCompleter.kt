/*
 * Derived from TabooLib 6.3.0-test-6-23-1 (MIT), Copyright (c) 2018 Bkm016.
 * Adapted for CarbKotlin shared commands and platform backends, 2026-09-25.
 * See src/main/resources/META-INF/licenses/TabooLib-command-LICENSE.txt.
 */
package me.xiaozhangup.carbkotlin.command

import net.kyori.adventure.audience.Audience

/**
 * TabooLib
 * taboolib.common.CommandTabCompleter
 *
 * @author sky
 * @since 2021/6/24 11:49 下午
 */
interface CommandCompleter {

    fun execute(sender: Audience, command: CommandStructure, name: String, args: Array<String>): List<String>?
}
