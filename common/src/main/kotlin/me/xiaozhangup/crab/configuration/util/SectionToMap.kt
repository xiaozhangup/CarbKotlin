package me.xiaozhangup.crab.configuration.util

import me.xiaozhangup.crab.configuration.ConfigurationSection
import me.xiaozhangup.crab.configuration.internal.Coerce

@Suppress("UNCHECKED_CAST")
inline fun <reified K, V> ConfigurationSection.getMap(path: String): Map<K, V> {
    val map = HashMap<K, V>()
    getConfigurationSection(path)?.let { section ->
        section.getKeys(false).forEach { key ->
            try {
                val convertedKey = convertKey<K>(key)
                map[convertedKey] = section[key] as V
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
    }
    return map
}

@Suppress("UNCHECKED_CAST")
inline fun <reified K> convertKey(key: String): K {
    return when (K::class) {
        Byte::class -> Coerce.toByte(key) as K
        Short::class -> Coerce.toShort(key) as K
        Int::class -> Coerce.toInteger(key) as K
        Long::class -> Coerce.toLong(key) as K
        Double::class -> Coerce.toDouble(key) as K
        Float::class -> Coerce.toFloat(key) as K
        Boolean::class -> Coerce.toBoolean(key) as K
        Char::class -> Coerce.toChar(key) as K
        else -> key as K
    }
}