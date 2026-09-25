/*
 * Derived from TabooLib 6.3.0-test-6-23-1 (MIT), Copyright (c) 2018 Bkm016.
 * Adapted for CrabKotlin shared commands and platform backends, 2026-09-25.
 * See src/main/resources/META-INF/licenses/TabooLib-command-LICENSE.txt.
 */
package me.xiaozhangup.crab.command.component

class CommandComponentLiteral(
    val aliases: Array<String>,
    val hidden: Boolean,
    val description: String = "",
    index: Int,
    optional: Boolean,
    permission: String
) : CommandComponent(index, optional, permission) {

    override fun toString(): String {
        return "CommandComponentLiteral(aliases=${aliases.contentToString()})"
    }
}
