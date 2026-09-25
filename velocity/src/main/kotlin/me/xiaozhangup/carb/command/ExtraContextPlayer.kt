/*
 * Derived from TabooLib 6.3.0-test-6-23-1 (MIT), Copyright (c) 2018 Bkm016.
 * Adapted for CarbKotlin shared commands and platform backends, 2026-09-25.
 * See src/main/resources/META-INF/licenses/TabooLib-command-LICENSE.txt.
 */
package me.xiaozhangup.carb.command


import com.velocitypowered.api.proxy.Player

/**
 * 根据节点名称获取输入参数并转换为玩家
 *
 * @param id 参数名称
 * @return 指定位置的输入参数
 * @throws IllegalStateException 参数不存在，或者玩家不存在
 */
fun <T> CommandContext<T>.player(id: String): Player {
    return server.getPlayer(get(id).substringBefore(' ')).orElse(null)!!
}

/**
 * 根据节点名称获取输入参数并转换为玩家
 *
 * @param id 参数名称
 * @return 指定位置的输入参数
 */
fun <T> CommandContext<T>.playerOrNull(id: String): Player? {
    return server.getPlayer(getOrNull(id)?.substringBefore(' ') ?: return null).orElse(null)
}

/**
 * 根据节点名称获取输入参数并转换为玩家
 * 根据输入的参数，如果是 "*" 则获取所有在线玩家，反之与 [player] 一致
 *
 * @param id 参数名称
 * @return 指定位置的输入参数
 * @throws IllegalStateException 参数不存在，或者玩家不存在
 */
fun <T> CommandContext<T>.players(id: String): List<Player> {
    val text = get(id).substringBefore(' ')
    return if (text == "*") server.allPlayers.toList() else listOf(server.getPlayer(text).orElse(null)!!)
}

/**
 * 根据节点名称获取输入参数并转换为玩家
 *
 * @param id 参数名称
 * @return 指定位置的输入参数
 */
fun <T> CommandContext<T>.playersOrNull(id: String): List<Player>? {
    val text = getOrNull(id)?.substringBefore(' ') ?: return null
    return if (text == "*") server.allPlayers.toList() else listOf(server.getPlayer(text).orElse(null) ?: return null)
}

fun CommandContext<*>.player(): Player = sender() as Player

private val CommandContext<*>.server get() = (command.registry as Commands).server
