package me.xiaozhangup.carbkotlin.database

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.sql.DataSource
import kotlin.use

/** Shared table lifecycle and JDBC helpers, using the owning plugin's data source. */
abstract class SQLTable(protected val dataSource: DataSource) {
    abstract val table: Table<Host<SQL>, SQL>

    open fun createTable() {
        table.createTable(dataSource)
        createIndexes()
    }

    open fun createIndexes() {}

    fun <T> executeQuery(sql: String, vararg params: Any?, mapper: ResultSet.() -> T): T? {
        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.bind(params)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        resultSet.mapper()
                    } else {
                        null
                    }
                }
            }
        }
    }

    fun <T> transaction(block: Connection.() -> T): T {
        return dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val result = connection.block()
                connection.commit()
                result
            } catch (ex: Throwable) {
                connection.rollback()
                throw ex
            } finally {
                connection.autoCommit = true
            }
        }
    }

    fun PreparedStatement.bind(params: Array<out Any?>) {
        params.forEachIndexed { index, param ->
            when (param) {
                is ByteArray -> setBytes(index + 1, param)
                else -> setObject(index + 1, param)
            }
        }
    }
}
