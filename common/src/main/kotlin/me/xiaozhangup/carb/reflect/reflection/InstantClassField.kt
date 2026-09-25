package me.xiaozhangup.carb.reflect.reflection

import me.xiaozhangup.carb.reflect.ClassAnnotation
import me.xiaozhangup.carb.reflect.Internal
import me.xiaozhangup.carb.reflect.JavaClassField
import me.xiaozhangup.carb.reflect.LazyClass
import me.xiaozhangup.carb.reflect.serializer.BinaryWriter
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * @author 坏黑
 * @since 2022/1/21 7:11 PM
 */
@Internal
class InstantClassField(owner: LazyClass, private val field: Field) : JavaClassField(field.name, owner) {

    val annotationsLocal = field.declaredAnnotations.map { InstantAnnotation(it) }

    override val type: LazyClass
        get() = LazyClass.of(this.field.type)

    override val isTransient: Boolean
        get() = Modifier.isTransient(this.field.modifiers)

    override val isStatic: Boolean
        get() = Modifier.isStatic(this.field.modifiers)

    override val isFinal: Boolean
        get() = Modifier.isFinal(this.field.modifiers)

    override val isPublic: Boolean
        get() = Modifier.isPublic(this.field.modifiers)

    override val isProtected: Boolean
        get() = Modifier.isProtected(this.field.modifiers)

    override val isPrivate: Boolean
        get() = Modifier.isPrivate(this.field.modifiers)

    override val annotations: List<ClassAnnotation>
        get() = annotationsLocal

    override fun toString(): String {
        return "InstantClassField(field=$field)"
    }

    override fun writeTo(writer: BinaryWriter) {
        error("InstantClassField cannot be serialized")
    }
}