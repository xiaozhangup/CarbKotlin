package me.xiaozhangup.carb.util

import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.ItemStack

fun HumanEntity.giveItem(itemStack: List<ItemStack>) {
    itemStack.forEach { giveItem(it) }
}

/**
 * 给予玩家物品。
 *
 * @param itemStack 要给予的物品。
 * @param repeat 重复给予的次数，默认为 1。
 */
fun HumanEntity.giveItem(itemStack: ItemStack?, repeat: Int = 1) {
    if (itemStack.isNotAir()) {
        // CraftInventory.addItem 的执行过程中, 实质上有可能修改 ItemStack 的 amount, 如果不注意这一点, 则会吞物品而不自知
        val preAmount = itemStack.amount
        repeat(repeat) {
            inventory.addItem(itemStack).values.forEach { world.dropItem(location, it) }
            itemStack.amount = preAmount
        }
    }
}

