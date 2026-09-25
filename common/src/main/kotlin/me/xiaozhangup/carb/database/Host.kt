package me.xiaozhangup.carb.database

import javax.sql.DataSource

/**
 * Database address.
 *
 * @author sky
 * @since 2018-05-14 19:07
 */
abstract class Host<T : ColumnBuilder> {
    abstract val columnBuilder: ColumnBuilder
    abstract val connectionUrl: String?
    abstract val connectionUrlSimple: String?
    abstract val driverClass: String

    fun createDataSource(
        database: Database,
        autoRelease: Boolean = true,
        withoutConfig: Boolean = false
    ): DataSource {
        return if (withoutConfig) database.createDataSourceWithoutConfig(this, autoRelease)
        else database.createDataSource(this, autoRelease)
    }
}
