package me.xiaozhangup.carbkotlin.common.util

import com.google.common.math.LongMath
import java.math.RoundingMode
import kotlin.math.abs

/**
 * 获取整数的位数
 */
fun Int.length(): Int {
    return toLong().length()
}

/**
 * 获取长整数的位数
 */
fun Long.length(): Int {
    return if (this == 0L) 1 else LongMath.log10(abs(this), RoundingMode.FLOOR) + 1
}

/**
 * 获取整数的每一位数字列表
 */
fun Int.digits(): List<Int> {
    return toLong().digits()
}

/**
 * 获取长整数的每一位数字列表
 */
fun Long.digits(): List<Int> {
    val digits = ArrayList<Int>()
    var num = abs(this)
    if (num == 0L) {
        digits.add(0)
    } else {
        while (num > 0) {
            digits.add((num % 10).toInt())
            num /= 10
        }
        digits.reverse()
    }
    return digits
}
