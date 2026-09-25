# Shared database DSL

`me.xiaozhangup.carbkotlin.database` contains the database module from
`io.izzel.taboolib:database:6.3.0-test-6-23-1`. The SQL DSL, SQL generation,
result processing, indexes, transactions and MySQL / SQLite / PostgreSQL
types are retained. Configuration and lifecycle integration are independent
of TabooLib. The upstream MIT license is included in both artifacts under
`META-INF/licenses/TabooLib-database-LICENSE`.

## Plugin dependency

```kotlin
compileOnly("me.xiaozhangup.crab:CarbKotlin:2.3.20:paper") {
    isTransitive = false
}
```

Declare CarbKotlin as a required plugin dependency. The `api` artifact is
only for compilation: it contains the database classes and the Hikari types
used in their signatures, without Kotlin runtimes or JDBC drivers. Deploy
only the normal `CarbKotlin-2.3.20.jar`; it supplies the runtime for all plugins.
Build and publish both artifacts with `sh gradlew build publishToMavenLocal`.

## Connection ownership

Each plugin's existing database manager can inherit `Database`:

```kotlin
object DatabaseManager : Database("MyPlugin") {
    lateinit var host: HostSQL
        private set
    lateinit var dataSource: javax.sql.DataSource
        private set

    fun init(connection: Map<String, Any?>) {
        host = HostSQL(connection)
        dataSource = createDataSource(host)
    }
}
```

The connection map accepts `host`, `port`, `user`, `password`, `database`;
the defaults match the original HostSQL constructor. A plugin using
TabooLib configuration can pass `databaseConfig.getValues(false)`. The
database library never reads or creates `datasource.yml`.

After the plugin finishes saving its data, close its manager. Kotlin's
`use` keeps this short and closes the pools even if saving fails:

```kotlin
override fun onDisable() {
    DatabaseManager.use {
        // Existing shutdown and data-saving operations.
    }
}
```

Each manager owns only its own pools and `prepareClose` callbacks. Errors
from managed data sources are logged under the manager's plugin name via
SLF4J; a custom warning function can be passed to the Database constructor.

## Defaults and code overrides

The defaults follow the upstream `datasource.yml`, including MinimumIdle=10:

| Hikari setting | Default |
| --- | --- |
| autoCommit | true |
| minimumIdle / maximumPoolSize | 10 / 10 |
| validationTimeout | 5000 ms |
| connectionTimeout | 30000 ms |
| idleTimeout | 600000 ms |
| maxLifetime | 1800000 ms |
| keepaliveTime | 300000 ms |
| connectionTestQuery | unset |
| prepStmtCacheSize | 250 |
| prepStmtCacheSqlLimit | 2048 |
| cachePrepStmts / useServerPrepStmts | true / true |

The driver follows the host type. Override any Hikari setting directly in
code when required:

```kotlin
dataSource = createDataSource(host) {
    maximumPoolSize = 20
}
```

`createHikariConfig`, the explicit HikariConfig overload,
`createDataSourceWithoutConfig`, `autoRelease=false`, `prepareClose`,
`File.getHost()` and map-based host construction are also available.
Pools created with `autoRelease=false` must be closed by their caller.

The runtime includes HikariCP 4.0.3 and MySQL Connector/J 8.4.0, matching
the copied module's dependency declarations, plus SQLite JDBC 3.53.0.0
and PostgreSQL JDBC 42.7.10. JDBC service descriptors are merged into the
runtime JAR. SQL tables and stored data do not need a schema migration.

## Shared SQL tables

Inherit `SQLTable` and pass the plugin's data source; keep the existing table DSL:

```kotlin
class TablePlayer : SQLTable(DatabaseManager.dataSource) {
    override val table: Table<Host<SQL>, SQL> = Table("players", DatabaseManager.host) {
        add("name") { type(ColumnTypeSQL.VARCHAR, 64) }
    }
}
```

`createTable()` creates the table and calls the overridable `createIndexes()` hook.
Subclasses use the inherited `dataSource` for DSL operations. The shared class
also provides `executeQuery(sql, vararg params, mapper)` for an optional first
row, `transaction { ... }` for commit/rollback, and `PreparedStatement.bind`
for parameters (including byte arrays). Connections are closed after each call;
the pool remains owned by the plugin's database manager.
