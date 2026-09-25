package me.xiaozhangup.carbkotlin

import me.xiaozhangup.carbkotlin.configuration.ConfigurationManager
import me.xiaozhangup.carbkotlin.configuration.ConfigurationSection
import me.xiaozhangup.carbkotlin.reflect.PluginScanner
import me.xiaozhangup.carbkotlin.redis.AlkaidRedis
import me.xiaozhangup.carbkotlin.redis.SingleRedisConnection
import me.xiaozhangup.carbkotlin.redis.fromConfig
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

/** Shared services with per-plugin ownership and one shutdown entry point. */
open class PluginResources(private val anchor: Class<*>, private val dataFolder: () -> File) : AutoCloseable {
    val scanner = PluginScanner(anchor)
    val lifecycle by lazy { me.xiaozhangup.carbkotlin.lifecycle.Lifecycle(scanner) }
    private val logger = org.slf4j.LoggerFactory.getLogger(anchor.simpleName)

    fun getDataFolder(): File = dataFolder().apply { mkdirs() }
    fun info(vararg messages: Any?) = messages.forEach { logger.info(it.toString()) }
    fun warning(vararg messages: Any?) = messages.forEach { logger.warn(it.toString()) }
    fun severe(vararg messages: Any?) = messages.forEach { logger.error(it.toString()) }

    /** Extract a bundled directory, preserving existing files unless overwrite is requested. */
    fun releaseResourceFolder(path: String, overwrite: Boolean = false) {
        val prefix = path.trimEnd('/') + "/"
        val root = getDataFolder().toPath().toAbsolutePath().normalize()
        fun copy(name: String, input: () -> java.io.InputStream) {
            val target = root.resolve(name).normalize()
            require(target.startsWith(root)) { "Resource escapes data directory: $name" }
            if (overwrite || !java.nio.file.Files.exists(target)) {
                java.nio.file.Files.createDirectories(target.parent)
                input().use { source -> java.nio.file.Files.newOutputStream(target).use(source::copyTo) }
            }
        }
        val source = File(anchor.protectionDomain.codeSource.location.toURI())
        if (source.isDirectory) {
            File(source, prefix).walkTopDown().filter { it.isFile }.forEach { file ->
                copy(file.relativeTo(source).invariantSeparatorsPath, file::inputStream)
            }
        } else {
            java.util.jar.JarFile(source).use { jar ->
                jar.entries().asSequence().filter { !it.isDirectory && it.name.startsWith(prefix) }.forEach { entry ->
                    copy(entry.name) { jar.getInputStream(entry) }
                }
            }
        }
    }
    fun releaseResourceFile(path: String, replace: Boolean = false): File {
        val root = getDataFolder().toPath().toAbsolutePath().normalize()
        val target = root.resolve(path).normalize()
        require(target.startsWith(root)) { "Resource escapes data directory: $path" }
        if (replace || !java.nio.file.Files.exists(target)) {
            val source = requireNotNull(anchor.classLoader.getResourceAsStream(path)) { "Missing resource: $path" }
            source.use {
                java.nio.file.Files.createDirectories(target.parent)
                java.nio.file.Files.newOutputStream(target).use(it::copyTo)
            }
        }
        return target.toFile()
    }

    val configurations = ConfigurationManager(anchor, dataFolder)
    private val connections = CopyOnWriteArrayList<SingleRedisConnection>()

    private var closed = false
    private val owned = CopyOnWriteArrayList<AutoCloseable>()

    @Synchronized
    fun <T : AutoCloseable> own(resource: T): T {
        if (closed) {
            resource.close()
            error("Plugin resources are closed")
        }
        owned.add(resource)
        return resource
    }

    fun loadConfigurations() = configurations.loadAnnotated(scanner)

    @Synchronized
    fun redis(config: ConfigurationSection): SingleRedisConnection {
        check(!closed) { "Plugin resources are closed" }
        return AlkaidRedis.create().fromConfig(config).connect().connection().also(connections::add)
    }

    override fun close() {
        val closing = synchronized(this) {
            if (closed) return
            closed = true
            (owned.toList().asReversed() + connections.toList() + configurations).also {
                owned.clear()
                connections.clear()
            }
        }
        var failure: Throwable? = null
        closing.forEach {
            try { it.close() } catch (ex: Exception) {
                if (failure == null) failure = ex else failure!!.addSuppressed(ex)
            }
        }
        failure?.let { throw it }
    }
}
