package me.xiaozhangup.crab.common.util

/**
 * 将任意对象转换为字符串列表
 * 
 * 根据对象类型进行不同的转换:
 * - 如果是数组，将每个元素转为字符串
 * - 如果是可迭代对象，将每个元素转为字符串
 * - 其他情况，将对象转为字符串并按行分割
 *
 * @receiver 任意对象
 * @return 转换后的字符串列表
 */
fun Any.asList(): List<String> {
    return when (this) {
        is Array<*> -> map { it.toString() }
        is Iterable<*> -> map { it.toString() }
        else -> toString().lines()
    }
}

/**
 * 安全的写入元素
 *
 * @param index 下标
 * @param element 元素
 * @param def 若写入位置之前存在空缺，则写入该默认值
 */
fun <T> MutableList<T>.setSafely(index: Int, element: T, def: T) {
    while (index >= size) {
        add(def)
    }
    this[index] = element
}

/**
 * 安全的插入元素
 *
 * @param index 下标
 * @param element 元素
 * @param def 若写入位置之前存在空缺，则写入该默认值
 */
fun <T> MutableList<T>.addSafely(index: Int, element: T, def: T) {
    while (index >= size) {
        add(def)
    }
    add(index, element)
}

/**
 * 根据条件删除元素并返回
 *
 * @param con 匹配条件
 * @return 返回被删除的元素
 *
 * @author Neon -老廖
 */
inline fun <T> MutableCollection<T>.removeAndBackIf(con: (T) -> Boolean): List<T> {
    if (isEmpty()) {
        return emptyList()
    }
    // 没有匹配元素时应该返回更轻的空列表
    var b: MutableList<T>? = null
    val i = this.iterator()
    while (i.hasNext()) {
        val a = i.next()
        if (a != null && con(a)) {
            if (b == null) {
                b = mutableListOf()
            }
            b.add(a)
            i.remove()
        }
    }
    return b ?: emptyList()
}

/**
 * 将列表中的元素从指定位置开始使用分隔符连接成字符串
 * 曾用来拼命令，现在已不再使用
 *
 * @param args 要连接的字符串列表
 * @param start 开始位置（默认为 0）
 * @param separator 分隔符（默认为空格）
 * @return 连接后的字符串
 */
@Deprecated("Use Kotlin's built-in joinToString function instead")
fun join(args: List<String>, start: Int = 0, separator: String = " "): String {
    return args.filterIndexed { index, _ -> index >= start }.joinToString(separator)
}

/**
 * 将数组中的元素从指定位置开始使用分隔符连接成字符串
 * 曾用来拼命令，现在已不再使用
 *
 * @param args 要连接的字符串数组
 * @param start 开始位置（默认为 0）
 * @param separator 分隔符（默认为空格）
 * @return 连接后的字符串
 */
@Deprecated("Use Kotlin's built-in joinToString function instead")
fun join(args: Array<String>, start: Int = 0, separator: String = " "): String {
    return args.filterIndexed { index, _ -> index >= start }.joinToString(separator)
}

/**
 * 获取列表中特定范围内的元素
 * 从 Java 时代继承下来的
 *
 * @param list 列表
 * @param start 开始位置
 * @param end 结束位置（默认为元素数量）
 */
@Deprecated("Use Kotlin's built-in subList function instead")
fun <T> subList(list: List<T>, start: Int = 0, end: Int = list.size): List<T> {
    return list.filterIndexed { index, _ -> index in start until end }
}

/**
 * 获取 Map 中特定范围内的元素
 * 从 Java 时代继承下来的
 *
 * @param map 要获取元素的 Map
 * @param start 开始位置
 * @param end 结束位置
 */
@Deprecated("Use Kotlin's built-in subList function instead")
fun <K, V> subMap(map: Map<K, V>, start: Int = 0, end: Int = map.size - 1): List<Map.Entry<K, V>> {
    return map.entries.filterIndexed { index, _ -> index in start..end }
}
