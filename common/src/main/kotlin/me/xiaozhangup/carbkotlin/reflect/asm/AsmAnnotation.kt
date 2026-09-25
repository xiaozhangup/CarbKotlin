package me.xiaozhangup.carbkotlin.reflect.asm

import me.xiaozhangup.carbkotlin.reflect.ClassAnnotation
import me.xiaozhangup.carbkotlin.reflect.Internal
import me.xiaozhangup.carbkotlin.reflect.LazyClass
import me.xiaozhangup.carbkotlin.reflect.serializer.BinaryWriter

/**
 * @author 坏黑
 * @since 2022/1/24 8:48 PM
 */
@Internal
class AsmAnnotation(source: LazyClass, val propertyMap: Map<String, Any>) : ClassAnnotation(source) {

    constructor(annotationVisitor: AsmClassAnnotationVisitor) : this(annotationVisitor.source, annotationVisitor.propertyMap)

    @Suppress("UNCHECKED_CAST")
    override fun <T> property(name: String): T? {
        return propertyMap[name] as? T?
    }

    override fun <T> property(name: String, def: T): T {
        return property(name) ?: def
    }

    override fun properties(): Map<String, Any> {
        return propertyMap
    }

    override fun propertyKeys(): Set<String> {
        return propertyMap.keys
    }

    override fun toString(): String {
        return "AsmAnnotation() ${super.toString()}"
    }

    override fun writeTo(writer: BinaryWriter) {
        writer.writeInt(1) // 1: ASM MODE
        writer.writeObj(source)
        try {
            writer.writeAnnotationProperties(propertyMap)
        } catch (ex: Throwable) {
            println("Failed to write annotation properties $propertyMap")
            println("Source: $source")
            throw ex
        }
    }

    companion object {

        private val internalMethods = arrayOf("equals", "hashCode", "toString")
    }
}