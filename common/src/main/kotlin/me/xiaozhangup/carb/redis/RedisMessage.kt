package me.xiaozhangup.carb.redis

import redis.clients.jedis.JedisPubSub
import me.xiaozhangup.carb.configuration.Configuration
import me.xiaozhangup.carb.configuration.Type
import java.io.Closeable

/**
 * TabooLib
 * me.xiaozhangup.carb.redis.RedisMessage
 *
 * @author 坏黑
 * @since 2022/8/30 12:11
 */
class RedisMessage(val channel: String, val message: String, internal val pubSub: JedisPubSub, internal val patternMode: Boolean) : Closeable {

    /**
     * 获取并借助 [Configuration] 反序列化
     *
     * @param ignoreConstructor 是否忽略构造函数
     */
    inline fun <reified T> get(ignoreConstructor: Boolean = false): T {
        return Configuration.deserialize(Configuration.loadFromString(message, Type.FAST_JSON), ignoreConstructor)
    }

    /**
     * 获取并借助 [Configuration] 反序列化
     *
     * @param obj 原始对象
     * @param ignoreConstructor 是否忽略构造函数
     */
    fun <T> get(obj: T, ignoreConstructor: Boolean = false): T {
        return Configuration.deserialize(Configuration.loadFromString(message, Type.FAST_JSON), obj, ignoreConstructor)
    }

    /**
     * 取消订阅
     */
    override fun close() {
        if (patternMode) {
            pubSub.punsubscribe()
        } else {
            pubSub.unsubscribe()
        }
    }
}