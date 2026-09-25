package me.xiaozhangup.carbkotlin.common.util

fun createBar(empty: String, fill: String, length: Int, percent: Double): String {
    return (1..length).joinToString("") {
        if (percent.isNaN() || percent == 0.0) empty else if (percent >= it.toDouble() / length) fill else empty
    }
}
