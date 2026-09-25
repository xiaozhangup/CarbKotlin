package me.xiaozhangup.crab.lifecycle

import me.xiaozhangup.crab.reflect.PluginScanner

/** Discovers methods without initializing their owners until their stage is invoked. */
class Lifecycle(private val scanner: PluginScanner) {
    private val handlers by lazy {
        scanner.classes.flatMap { type ->
            type.structure.methods.filter { it.isAnnotationPresent(Awake::class.java) }.map { method ->
                require(method.parameter.isEmpty()) { "${type.name}.${method.name}: @Awake requires no arguments" }
                val annotation = method.getAnnotation(Awake::class.java)
                Triple(annotation.enum<LifeCycle>("value"), annotation.property("priority", 0), {
                    if (method.isStatic) method.invokeStatic() else method.invoke(scanner.instance(type))
                    Unit
                })
            }
        }.sortedBy { it.second }
    }
    private val executed = java.util.EnumSet.noneOf(LifeCycle::class.java)

    /** Each stage runs once. Lower priority numbers run first. No platform hooks are installed. */
    fun run(stage: LifeCycle) {
        check(executed.add(stage)) { "Lifecycle stage $stage already executed" }
        var failure: Throwable? = null
        handlers.filter { it.first == stage }.forEach { (_, _, action) ->
            try {
                action()
            } catch (ex: Exception) {
                if (stage != LifeCycle.DISABLE) throw ex
                if (failure == null) failure = ex else failure!!.addSuppressed(ex)
            }
        }
        failure?.let { throw it }
    }
}
