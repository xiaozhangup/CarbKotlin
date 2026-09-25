package me.xiaozhangup.carb.chain

import org.bukkit.plugin.Plugin

/** Plugin-owned task chains using this artifact's native platform scheduler. */
class Chains(plugin: Plugin) : ChainExecutor(PlatformChainScheduler(plugin))
