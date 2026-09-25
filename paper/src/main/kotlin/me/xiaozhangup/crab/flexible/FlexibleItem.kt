package me.xiaozhangup.crab.flexible

import org.bukkit.inventory.ItemStack
import org.bukkit.Bukkit
import kotlin.jvm.optionals.getOrNull

object FlexibleItem {
    private val flexibleItem = mutableMapOf<String, FlexibleItemHandler>()

    fun getItemStack(item: String): ItemStack? {
        val handler = when (val namespace = item.substringBefore(':')) {
            "base64", "bukkit" -> Base64Handler
            else -> flexibleItem[namespace]
        }
        val stack = handler?.getStack(item.substringAfter(':'))?.getOrNull()
        if (stack == null) {
            Bukkit.getLogger().warning("[FlexibleItem] Can't find item \"$item\"")
        }
        return stack
    }

    fun toFlexibleItem(itemStack: ItemStack): String {
        return toFlexibleItem(itemStack, useBase64 = false)
            ?: "base64:${Base64Handler.getName(itemStack).get()}"
    }

    fun toFlexibleItem(itemStack: ItemStack, useBase64: Boolean = false): String? {
        if (useBase64) return toFlexibleItem(itemStack)
        for (handler in flexibleItem.values) {
            val name = handler.getName(itemStack).getOrNull()
            if (name != null) {
                return "${handler.getNamespace()}:$name"
            }
        }

        return null
    }

    fun registerHandler(handler: FlexibleItemHandler) {
        flexibleItem[handler.getNamespace()] = handler
        for (alias in handler.getNamespaceAlias()) {
            flexibleItem[alias] = handler
        }
    }

    fun unregisterHandler(handler: FlexibleItemHandler) {
        flexibleItem.remove(handler.getNamespace())
        for (alias in handler.getNamespaceAlias()) {
            flexibleItem.remove(alias)
        }
    }
}

fun flexibleItem(item: String): ItemStack? {
    return FlexibleItem.getItemStack(item)
}

fun flexibleItem(item: ItemStack, deep: Boolean = false): String {
    return FlexibleItem.toFlexibleItem(item)
}
