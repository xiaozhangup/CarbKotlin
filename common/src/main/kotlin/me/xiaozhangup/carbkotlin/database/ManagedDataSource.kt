package me.xiaozhangup.carbkotlin.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

internal class ManagedDataSource(
    config: HikariConfig,
    val warning: (String) -> Unit
) : HikariDataSource(config)
