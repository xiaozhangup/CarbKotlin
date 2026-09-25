package me.xiaozhangup.crab.reflect

import java.io.File
import java.io.InputStream
import java.util.jar.JarFile

/** Reads only the owning plugin's bytecode; discovering annotations does not initialize classes. */
class PluginScanner(private val anchor: Class<*>, private val packageName: String = anchor.packageName) {
    private val instances = mutableMapOf<String, Any>()

    /** Reuse a platform-created instance for configuration and annotated callbacks. */
    fun bind(instance: Any) {
        instances[instance.javaClass.name] = instance
    }

    private val finder = object : ClassAnalyser.ClassFinder {
        override fun findClass(name: String): Class<*> = Class.forName(name, false, anchor.classLoader)
    }

    val classes: List<ReflexClass> by lazy {
        val prefix = packageName.replace('.', '/') + "/"
        val source = File(anchor.protectionDomain.codeSource.location.toURI())
        val result = mutableListOf<ReflexClass>()
        fun read(path: String, stream: InputStream) {
            stream.use {
                val type = LazyClass.of(path.removeSuffix(".class"), classFinder = finder)
                result += ReflexClass(ClassAnalyser.analyseByASM(type, it, finder), AnalyseMode.ASM_ONLY)
            }
        }
        if (source.isDirectory) {
            File(source, prefix).walkTopDown().filter { it.isFile && it.extension == "class" }.forEach {
                read(it.relativeTo(source).invariantSeparatorsPath, it.inputStream())
            }
        } else {
            JarFile(source).use { jar ->
                jar.entries().asSequence().filter { it.name.startsWith(prefix) && it.name.endsWith(".class") }.forEach {
                    read(it.name, jar.getInputStream(it))
                }
            }
        }
        result.sortedWith(compareBy<ReflexClass> { it.name != anchor.name }.thenBy { it.name })
    }

    fun visitAnnotated(vararg annotations: Class<out Annotation>, visitor: (ReflexClass) -> Unit) {
        classes.filter { type -> annotations.any(type::hasAnnotation) }.forEach(visitor)
    }

    fun instance(type: ReflexClass): Any = instances[type.name] ?: type.getInstance(finder) ?: type.newInstance()
        ?: error("Cannot create ${type.name}")
}
