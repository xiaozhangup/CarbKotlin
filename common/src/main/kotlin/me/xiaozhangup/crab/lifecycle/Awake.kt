package me.xiaozhangup.crab.lifecycle

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Awake(val value: LifeCycle, val priority: Int = 0)
