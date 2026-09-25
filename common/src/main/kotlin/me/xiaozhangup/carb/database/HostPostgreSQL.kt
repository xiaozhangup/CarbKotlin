package me.xiaozhangup.carb.database


/**
 * PostgreSQL 数据库地址
 *
 * @author sky
 * @since 2024-01-01
 */
class HostPostgreSQL(
    val host: String,
    val port: String,
    val user: String,
    val password: String,
    val database: String,
    val schema: String = "public"
) : Host<PostgreSQL>() {

    val flags = arrayListOf<String>()

    val flagsURL: String
        get() = if (flags.isEmpty()) "" else "?${flags.joinToString("&")}"

    override val columnBuilder: ColumnBuilder
        get() = PostgreSQL()

    override val connectionUrl: String
        get() = "jdbc:postgresql://$host:$port/$database$flagsURL"

    override val connectionUrlSimple: String
        get() = "jdbc:postgresql://$host:$port/$database"

    override val driverClass: String
        get() = "org.postgresql.Driver"

    constructor(section: Map<String, Any?>) : this(
        section["host"]?.toString() ?: "localhost",
        section["port"]?.toString() ?: "5432",
        section["user"]?.toString() ?: "postgres",
        section["password"]?.toString() ?: "",
        section["database"]?.toString() ?: "test",
        section["schema"]?.toString() ?: "public",
    )

    override fun toString(): String {
        return "HostPostgreSQL(host='$host', port='$port', user='$user', password='***', database='$database', schema='$schema', connectionUrl='$connectionUrl')"
    }
}
