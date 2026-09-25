package me.xiaozhangup.carb.configuration.internal

fun String.decodeUnicode(): String {
    val builder = StringBuilder()
    var i = 0
    while (i < length) {
        val c = this[i]
        if (c == '\\' && i + 1 < length && this[i + 1] == 'u') {
            val hex = substring(i + 2, i + 6)
            builder.append(hex.toInt(16).toChar())
            i += 6
        } else {
            builder.append(c)
            i++
        }
    }
    return builder.toString()
}
fun Any.asList(): List<String> = when (this) {
    is Array<*> -> map { it.toString() }
    is Iterable<*> -> map { it.toString() }
    else -> toString().lines()
}
