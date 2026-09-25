package me.xiaozhangup.carbkotlin.chain

import kotlin.Any

/** Plugin-owned task chains using this artifact's native platform scheduler. */
class Chains(plugin: Any) : ChainExecutor(PlatformChainScheduler(plugin))
