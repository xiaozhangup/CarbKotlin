package me.xiaozhangup.carbkotlin.flexible

import org.bukkit.inventory.ItemStack
import org.bukkit.Bukkit
import kotlin.jvm.optionals.getOrNull

object FlexibleItem {
    private val flexibleItem = mutableMapOf<String, FlexibleItemHandler>()

    fun getItemStack(item: String): ItemStack? {
        val stack = flexibleItem[item.substringBefore(':')]?.getStack(item.substringAfter(':'))?.getOrNull()
        if (stack == null) {
            Bukkit.getLogger().warning("[FlexibleItem] Can't find item \"$item\"")
        }
        return stack
    }

    fun toFlexibleItem(itemStack: ItemStack): String {
        return checkNotNull(toFlexibleItem(itemStack, useBase64 = false)) {
            "No FlexibleItem handler can serialize ${itemStack.type}"
        }
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