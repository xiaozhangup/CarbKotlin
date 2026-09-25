package me.xiaozhangup.crab.flexible

import org.bukkit.inventory.ItemStack
import java.util.*

interface FlexibleItemHandler {
    fun getNamespace(): String

    fun getNamespaceAlias(): List<String> = listOf()

    fun getStack(string: String): Optional<ItemStack>

    fun getName(item: ItemStack): Optional<String>
}