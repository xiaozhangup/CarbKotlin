package me.xiaozhangup.crab.flexible.handler

import me.xiaozhangup.crab.flexible.FlexibleItemHandler

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*

internal object MinecraftHandler : FlexibleItemHandler {

    override fun getNamespace(): String = "minecraft"

    override fun getStack(string: String): Optional<ItemStack> {
        val material = Material.matchMaterial(string.uppercase()) ?: return Optional.empty()
        return Optional.of(ItemStack(material))
    }

    override fun getName(item: ItemStack): Optional<String> {
        if (!item.hasItemMeta()) {
            return Optional.of(item.type.name.lowercase())
        }
        return Optional.empty()
    }
}
