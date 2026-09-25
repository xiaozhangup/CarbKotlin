package me.xiaozhangup.crab.flexible.handler

import me.xiaozhangup.crab.flexible.FlexibleItemHandler
import org.bukkit.inventory.ItemStack
import java.util.*

/** Built-in fallback; kept outside the registry so custom handlers always run first. */
internal object Base64Handler : FlexibleItemHandler {

    override fun getNamespace(): String = "base64"

    override fun getNamespaceAlias(): List<String> {
        return listOf("bukkit")
    }

    override fun getStack(string: String): Optional<ItemStack> {
        return try {
            Optional.of(ItemStack.deserializeBytes(Base64.getDecoder().decode(string)))
        } catch (e: Exception) {
            e.printStackTrace()
            Optional.empty()
        }
    }

    override fun getName(item: ItemStack): Optional<String> {
        return Optional.of(Base64.getEncoder().encodeToString(item.serializeAsBytes()))
    }

}
