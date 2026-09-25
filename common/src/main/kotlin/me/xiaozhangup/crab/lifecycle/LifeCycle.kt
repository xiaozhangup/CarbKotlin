package me.xiaozhangup.crab.lifecycle

/** Stages are dispatched explicitly by the owning plugin. */
enum class LifeCycle { CONST, INIT, LOAD, ENABLE, ACTIVE, DISABLE }
