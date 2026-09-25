package me.xiaozhangup.carbkotlin.configuration

import me.xiaozhangup.carbkotlin.reflect.PluginScanner
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.ConcurrentHashMap

/** Configuration files and watchers belong to one plugin, never to a global registry. */
class ConfigurationManager(
    private val anchor: Class<*>,
    private val dataFolder: () -> File
) : AutoCloseable {
    private val files = LinkedHashMap<String, Configuration>()
    private data class WatchedFile(val configuration: Configuration, var modified: Long, var length: Long)
    private val watched = ConcurrentHashMap<File, WatchedFile>()
    private var watcher: ScheduledExecutorService? = null
    private val logger = LoggerFactory.getLogger(anchor.simpleName)

    @Synchronized
    fun load(name: String = "config.yml", target: String = name, autoReload: Boolean = false,
             migrate: Boolean = false, concurrent: Boolean = true): Configuration {
        val file = File(dataFolder(), target).absoluteFile
        val configuration = files.getOrPut(target) {
            if (!file.exists()) {
                file.parentFile.mkdirs()
                checkNotNull(anchor.classLoader.getResourceAsStream(name)) { "Missing configuration resource: $name" }.use {
                    file.outputStream().use(it::copyTo)
                }
            }
            Configuration.loadFromFile(file, concurrent = concurrent)
        }
        // Add new defaults without replacing user values or deleting existing keys.
        if (migrate) {
            val defaults = checkNotNull(anchor.classLoader.getResourceAsStream(name)) { "Missing configuration resource: $name" }
                .use { Configuration.loadFromInputStream(it, configuration.type) }
            var changed = false
            defaults.getKeys(true).forEach { key ->
                if (!configuration.contains(key)) {
                    configuration[key] = defaults[key]
                    configuration.setComments(key, defaults.getComments(key))
                    changed = true
                }
            }
            if (changed) configuration.saveToFile()
        }
        if (autoReload) watch(file, configuration)
        return configuration
    }

    fun loadAnnotated(scanner: PluginScanner) {
        scanner.classes.forEach { type ->
            type.structure.fields.filter { it.isAnnotationPresent(Config::class.java) }.forEach { field ->
                val annotation = field.getAnnotation(Config::class.java)
                val name = annotation.property("value", "config.yml")
                val target = annotation.property("target", "").ifEmpty { name }
                val configuration = load(name, target,
                    annotation.property("autoReload", false), annotation.property("migrate", false),
                    annotation.property("concurrent", true))
                if (field.isStatic) field.setStatic(configuration)
                else field.set(scanner.instance(type), configuration)
            }
        }
    }

    @Synchronized
    private fun watch(file: File, configuration: Configuration) {
        watched.putIfAbsent(file, WatchedFile(configuration, file.lastModified(), file.length()))
        if (watcher == null) {
            watcher = Executors.newSingleThreadScheduledExecutor { task ->
                Thread(task, "${anchor.simpleName}-configuration").apply { isDaemon = true }
            }.also { it.scheduleWithFixedDelay(::reloadChangedFiles, 500, 500, TimeUnit.MILLISECONDS) }
        }
    }

    private fun reloadChangedFiles() {
        for ((file, state) in watched) {
            val modified = file.lastModified()
            val length = file.length()
            if (modified == state.modified && length == state.length) continue
            state.modified = modified
            state.length = length
            if (!file.exists()) continue
            try {
                state.configuration.reload()
            } catch (ex: Exception) {
                logger.warn("Failed to reload $file", ex)
            }
        }
    }

    @Synchronized
    override fun close() {
        watcher?.shutdownNow()
        watcher = null
        watched.clear()
        files.clear()
    }
}
