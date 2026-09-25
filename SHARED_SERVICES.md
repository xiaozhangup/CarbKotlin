# Shared configuration, Redis and reflection

Consumers use the non-transitive `me.xiaozhangup.crab:CrabKotlin:2.3.20:paper`
compile-only artifact. Use classifier `velocity` instead for Velocity. Each full platform plugin provides
the runtime classes for that platform; consumers must not shade these packages.

```kotlin
val crab = Crab(javaClass, ::getDataFolder) { nativePlugin }

// Call during plugin initialization, before other code reads configuration.
crab.loadConfigurations()

@Config("config.yml", autoReload = true)
lateinit var config: Configuration

// Owned connection, subscriptions and lock renewal stop with crab.close().
val redis by lazy { crab.redis(config.getConfigurationSection("redis")!!) }

// Call after business shutdown/save operations.
crab.close()
```

Configuration types and `@Config` live in `me.xiaozhangup.crab.configuration`.
The existing section API, delegates, YAML comments and reload callbacks remain.
`migrate = true` copies missing defaults without overwriting configured values.
File auto-reload polls modification time and length every 500 ms and runs callbacks
on the owner's configuration thread. As before, callbacks that touch game state
must switch to the appropriate server scheduler.

`crab.scanner.visitAnnotated(MyAnnotation::class.java) { type -> ... }`
scans only the owner's package and JAR, using that plugin's classloader.
`Crab(nativePlugin, dataFolder)` binds the existing owner so instance fields and
annotated methods reuse it; discovery never constructs another plugin entry. Business
registration remains in the plugin. The shared command registry and explicit
`crab.lifecycle` dispatcher reuse this scanner. Native Velocity listeners can be
registered with `crab.registerEvents()`. See COMMANDS.md and CRAB.md.

Redis types live in `me.xiaozhangup.crab.redis`. `connector.connection()`
returns one shared connection facade per connector; ordinary operations borrow
and return a pooled client. Each connector owns its subscriptions and lock renewal.
Locks retain the `taboo_redis_lock__lock` key prefix and use per-owner tokens.

## Source attribution

- Configuration and Coerce: TabooLib 6.3.0-test-6-23-1, MIT; see
  `common/src/main/resources/META-INF/licenses/TabooLib-database-LICENSE` (also applies
  to the copied configuration sources). Packages and platform integration adapted.
- AlkaidRedis: the same TabooLib release's `database-alkaid-redis` sources,
  Apache-2.0; see `META-INF/licenses/AlkaidRedis-Apache-2.0-LICENSE`.
  Connection ownership, scheduling, subscriptions and locks adapted.
- Reflex/analyser 1.2.4: CC0; see `META-INF/licenses/Reflex-CC0-LICENSE`.
  Package renamed; plugin-scoped bytecode scanner added.
- NightConfig 3.6.7 conversion helper: LGPL-3.0; see
  `META-INF/licenses/NightConfig-LICENSE`. AnnotationUtils is copied into the
  configuration internal package; the patched ObjectConverter comes from
  TabooLib's configuration sources. Full modified sources are in this repository.

Paper consumers use native entry classes and CrabKotlin services. They no longer
install BukkitUtil, BukkitHook, minecraft-i18n or their internal configuration copies.
The migrated inventory/serialization/Vault/placeholder helpers and Baffle/RandomList
are adapted from the same MIT-licensed TabooLib release; the included TabooLib
license also covers these sources.
