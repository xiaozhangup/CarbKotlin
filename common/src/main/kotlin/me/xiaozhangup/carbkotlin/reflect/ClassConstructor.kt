package me.xiaozhangup.carbkotlin.reflect

import me.xiaozhangup.carbkotlin.reflect.asm.AsmClassConstructor
import me.xiaozhangup.carbkotlin.reflect.asm.AsmClassMethod
import me.xiaozhangup.carbkotlin.reflect.serializer.BinaryReader
import me.xiaozhangup.carbkotlin.reflect.serializer.BinarySerializable

/**
 * @author 坏黑
 * @since 2022/1/21 6:41 PM
 */
abstract class ClassConstructor(name: String, owner: LazyClass) : ClassMember(name, owner), BinarySerializable {

    abstract val parameter: List<LazyAnnotatedClass>

    abstract fun instance(vararg values: Any?): Any?

    val parameterTypes by lazy {
        parameter.map { p -> p.instance ?: Unknown::class.java }.toTypedArray()
    }

    override fun toString(): String {
        return "ClassConstructor(parameter=$parameter)"
    }

    companion object {

        fun of(reader: BinaryReader, classFinder: ClassAnalyser.ClassFinder?): ClassConstructor {
            val type = reader.readInt()
            if (type == 1) {
                val method = AsmClassMethod.readFrom(reader, classFinder)
                return AsmClassConstructor(
                    method.name,
                    method.owner,
                    method.descriptor,
                    method.access,
                    method.parameterAnnotations,
                    classFinder ?: ClassAnalyser.ClassFinder.default,
                    method.annotations,
                    method.parameter
                )
            } else {
                error("Unknown type: $type")
            }
        }
    }
}