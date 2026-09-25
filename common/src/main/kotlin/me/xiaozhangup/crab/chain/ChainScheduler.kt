package me.xiaozhangup.crab.chain

/** Delay and period use ticks (50 ms on Velocity). The callback is never called inline. */
fun interface ChainScheduler {
    fun schedule(type: DispatcherType, delay: Long, period: Long, action: Runnable): AutoCloseable
}
