package me.xiaozhangup.carbkotlin.util

import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Projectile
import org.bukkit.entity.EvokerFangs
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent

fun PlayerInteractEvent.isLeftClickBlock() = action == Action.LEFT_CLICK_BLOCK
fun PlayerInteractEvent.isRightClickBlock() = action == Action.RIGHT_CLICK_BLOCK
val onlinePlayers get() = Bukkit.getOnlinePlayers().toList()
val EntityDamageByEntityEvent.attacker: LivingEntity?
    get() = when (val source = damager) {
        is LivingEntity -> source
        is Projectile -> source.shooter as? LivingEntity
        is EvokerFangs -> source.owner
        else -> null
    }
val EntityDeathEvent.killer: LivingEntity?
    get() = entity.killer ?: (entity.lastDamageCause as? EntityDamageByEntityEvent)?.attacker
