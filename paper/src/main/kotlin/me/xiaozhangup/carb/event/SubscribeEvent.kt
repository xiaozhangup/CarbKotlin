package me.xiaozhangup.carb.event

import org.bukkit.event.EventPriority

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubscribeEvent(val priority: EventPriority = EventPriority.NORMAL, val ignoreCancelled: Boolean = false)
