package me.xiaozhangup.crab.common.reflect

import me.xiaozhangup.crab.reflect.UnsafeAccess
import java.lang.reflect.Field

@Deprecated("Use me.xiaozhangup.crab.reflect.UnsafeAccess")
object Ref {

    fun put(src: Any?, field: Field, value: Any?) {
        UnsafeAccess.put(src, field, value)
    }

    fun <T> get(src: Any?, field: Field): T? {
        return UnsafeAccess.get(src, field)
    }
}
