package me.xiaozhangup.crab.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

/** One plugin's connection pools and shutdown callbacks. No configuration files are read. */
open class Database(
    val name: String,
    private val warning: (String) -> Unit = { LoggerFactory.getLogger(name).warn(it) }
) : AutoCloseable {
    private val dataSources = CopyOnWriteArrayList<HikariDataSource>()
    private val callbackClose = CopyOnWriteArrayList<Runnable>()
    private val poolSequence = AtomicInteger()

    fun prepareClose(func: Runnable) {
        callbackClose += func
    }

    fun createDataSource(
        host: Host<*>,
        autoRelease: Boolean = true,
        configure: HikariConfig.() -> Unit = {}
    ): HikariDataSource = createDataSource(host, createHikariConfig(host).apply(configure), autoRelease)

    fun createDataSource(
        host: Host<*>,
        hikariConfig: HikariConfig,
        autoRelease: Boolean = true
    ): HikariDataSource {
        if (hikariConfig.poolName == null) {
            hikariConfig.poolName = "$name-${poolSequence.incrementAndGet()}"
        }
        return ManagedDataSource(hikariConfig, warning).also {
            if (autoRelease) dataSources += it
        }
    }

    /** Uses Hikari's own defaults, matching the original withoutConfig option. */
    fun createDataSourceWithoutConfig(host: Host<*>, autoRelease: Boolean = true): HikariDataSource {
        return createDataSource(host, connectionConfig(host), autoRelease)
    }

    /** Defaults from TabooLib 6.3.0-test-6-23-1's datasource.yml. */
    fun createHikariConfig(host: Host<*>): HikariConfig = connectionConfig(host).apply {
        driverClassName = host.driverClass
        isAutoCommit = true
        minimumIdle = 10
        maximumPoolSize = 10
        validationTimeout = 5_000
        connectionTimeout = 30_000
        idleTimeout = 600_000
        maxLifetime = 1_800_000
        keepaliveTime = 300_000
        addDataSourceProperty("prepStmtCacheSize", "250")
        addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        addDataSourceProperty("cachePrepStmts", "true")
        addDataSourceProperty("useServerPrepStmts", "true")
    }

    private fun connectionConfig(host: Host<*>): HikariConfig = HikariConfig().apply {
        jdbcUrl = host.connectionUrl
        when (host) {
            is HostSQL -> {
                username = host.user
                password = host.password
            }
            is HostPostgreSQL -> {
                username = host.user
                password = host.password
            }
        }
    }

    /** Call after the owning plugin has finished saving its data. */
    override fun close() {
        try {
            callbackClose.forEach { it.run() }
        } finally {
            dataSources.forEach { it.close() }
            dataSources.clear()
            callbackClose.clear()
        }
    }
}
