package me.xiaozhangup.carb.redis.lock

import me.xiaozhangup.carb.redis.IRedisConnection
import java.util.UUID
import java.util.concurrent.ScheduledFuture

/** Connection-owned Redis lock renewal; an owner token prevents releasing another owner's lock. */
class Lock(val connection: IRedisConnection, lockName: String) {
    val lockName = prefixName("taboo_redis_lock__lock", lockName)
    var internalLockLeaseTime = 30L
    private val token = UUID.randomUUID().toString()
    private var renewal: ScheduledFuture<*>? = null

    fun tryLock(): Boolean {
        val acquired = connection.eval(
            "if redis.call('setnx',KEYS[1],ARGV[1]) == 1 then redis.call('expire',KEYS[1],ARGV[2]) return 1 else return 0 end",
            listOf(lockName), listOf(token, internalLockLeaseTime.toString())
        ) == 1L
        if (acquired) extended()
        return acquired
    }

    fun unlock() {
        renewal?.cancel(false)
        renewal = null
        val result = connection.eval(
            "if redis.call('get',KEYS[1]) == false then return 1 elseif redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 2 end",
            listOf(lockName), listOf(token)
        )
        check(result == 1L) { "Cannot unlock $lockName: owner changed" }
    }

    fun extended() {
        renewal?.cancel(false)
        renewal = connection.schedule(1000) {
            val result = connection.eval(
                "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('expire',KEYS[1],ARGV[2]) else return 0 end",
                listOf(lockName), listOf(token, internalLockLeaseTime.toString())
            )
            if (result != 1L) renewal?.cancel(false)
        }
    }

    fun prefixName(prefix: String, name: String): String =
        if (name.contains("{")) "$prefix:$name" else "$prefix:{$name}"
}
