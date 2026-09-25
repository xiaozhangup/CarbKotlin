/*
 * Derived from TabooLib 6.3.0-test-6-23-1 (MIT), Copyright (c) 2018 Bkm016.
 * Adapted for CarbKotlin shared commands and platform backends, 2026-09-25.
 * See src/main/resources/META-INF/licenses/TabooLib-command-LICENSE.txt.
 */
package me.xiaozhangup.carbkotlin.command

import me.xiaozhangup.carbkotlin.command.Notify
import java.util.Locale

/**
 * TabooLib
 * taboolib.common.CommandStructure
 *
 * @author sky
 * @since 2021/6/24 11:48 下午
 */
class CommandStructure(
    val registry: CommandRegistry,
    name: String,
    val aliases: List<String>,
    val description: String,
    val usage: String,
    val permission: String,
    val permissionMessage: String,
    val permissionDefault: PermissionDefault,
    val permissionChildren: Map<String, PermissionDefault>,
    val newParser: Boolean,
    val notify: Notify? = null,
) {

    val name = name.lowercase(Locale.ROOT)
}
