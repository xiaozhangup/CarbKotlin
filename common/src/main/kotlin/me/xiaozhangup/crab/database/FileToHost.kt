package me.xiaozhangup.crab.database

import java.io.File

fun File.getHost(): HostSQLite = HostSQLite(this)

fun Map<String, Any?>.getHost(): HostSQL = HostSQL(this)

fun Map<String, Map<String, Any?>>.getHost(name: String): HostSQL = HostSQL(this[name].orEmpty())
