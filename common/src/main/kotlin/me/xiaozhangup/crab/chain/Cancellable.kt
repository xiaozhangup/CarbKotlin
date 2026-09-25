package me.xiaozhangup.crab.chain

open class Cancellable {
    var cancelled = false
        private set
    fun cancel() { cancelled = true }
    fun <T> call(block: Cancellable.() -> T): T = block()
}
