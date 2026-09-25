# Per-plugin Crab facade

Each plugin owns exactly one `me.xiaozhangup.carb.Crab`. Both platform
artifacts expose the same facade. No stack inspection or global default owner is
used. The instance owns command registrations, task chains, ordinary scheduled
tasks, configuration watchers and Redis connections.

Paper plugins use a native `JavaPlugin` entry class and construct
`internal val crab = Crab(this, dataFolder)`. Shared state is exposed from the
main class's companion object; the companion's Crab getter forwards to the native
instance. The native instance is bound to the scanner, so configuration and
lifecycle callbacks do not construct another plugin instance.

Both platform constructors queue tasks until the plugin calls `crab.start()`.
Paper `onLoad` dispatches CONST, INIT and LOAD, then business loading. INIT loads
annotated configurations. `onEnable` dispatches ENABLE, registers Crab event listeners and placeholder
expansions before business setup, starts queued tasks,
and schedules ACTIVE plus business activation one tick later. `onDisable`
dispatches DISABLE and business shutdown in nested finally blocks, then closes
Crab even if a callback fails.

```kotlin
crab.command("example") { /* existing DSL */ }
crab.submitTask(delay = 20) { /* main-thread task on Paper */ }
crab.submitAsyncTask(period = 20) { if (finished()) cancel() }
crab.submitChain { /* existing chain DSL */ }
crab.redis(config.getConfigurationSection("redis")!!)
crab.scanner.visitAnnotated(MyAnnotation::class.java) { /* ... */ }
crab.commands.registerAnnotated(existingObject)
```

Each consumer has exactly one `util/ext/Crab.kt` or `utils/ext/Crab.kt` in its own package. It exposes
`internal` top-level `command`, `submitTask`, `submitAsyncTask` and `submitChain`
forwarders with the full parameters/defaults. Other Kotlin modules cannot import
these helpers; they must use their own facade. JVM reflection/Java visibility is
not restricted. Each plugin's `crab` property is also internal.

## Scheduling and shutdown

Delays/periods are ticks (50 ms on Velocity). `now = true` runs once on the calling
thread after activation, ignoring delay/period, as before. Before activation all
submissions are queued. The task callback receives a common `Task` with `cancel()`.
One-shot completion, errors, explicit cancellation and close release tracked
handles. Native tasks remain attributed to the calling plugin. Velocity has no
Paper main-thread guarantee. Paper scheduling targets Paper, not Folia affinity.

`crab.runTasksImmediately = true` preserves Whale's synchronous shutdown/save
behavior for ordinary tasks. It does not make coroutine dispatch run inline.
`crab.close()` unregisters commands, cancels task chains and pending/repeating
tasks, and closes the remaining resources. It is idempotent. Running business
callbacks are not forcibly interrupted; shutdown logic should finish before close.

SlimeMaster retains its dedicated serial worker queue and TimeUnit semantics via
its internal `submitSerialTask`; Crab tracks/cancels these futures as well. Existing
specialized coroutine scopes (`submitScope`) and direct entity/region scheduling
are separate facilities and retain their existing behavior.

## Explicit lifecycle dispatch

`crab.lifecycle` is a platform-neutral `Lifecycle` utility built on the owning
plugin's scanner. It installs no platform hooks. Plugins explicitly call
`crab.lifecycle.run(LifeCycle.INIT)` (and LOAD, ENABLE, ACTIVE, DISABLE as needed).
Use `me.xiaozhangup.carb.lifecycle.Awake` on no-argument methods; lower
`priority` numbers execute first, with scanner order breaking ties. Each stage
executes once. Discovery reads bytecode without constructing every class; owners
are resolved only when their annotated method runs. This makes `SkipTo` unnecessary
for objects that are explicitly initialized by their plugin.

Cubozoa and SlimeMasterNext are native Velocity entry classes themselves.
Their companion objects expose `instance`, shared configuration/services/world state,
and a `server` getter backed by the injected instance. Member initialization creates
`Crab(this, dataDirectory.toFile())`, which binds that same instance for configuration
injection and lifecycle callbacks. After members have initialized, the constructor
sets `instance` and dispatches CONST/INIT (including configuration loading).
ProxyInitializeEvent dispatches LOAD, onLoad, ENABLE, onEnable, then registers
listeners and starts queued tasks. ACTIVE/onActive run as an owned task.
ProxyShutdownEvent performs business shutdown, dispatches all DISABLE handlers
in a finally block, then closes Crab. DISABLE collects handler exceptions so one
failure does not skip remaining cleanup. Paper plugins also use Crab lifecycle annotations and explicitly dispatch their stages.

## Native Velocity services

`crab.registerEvents()` discovers native `@Subscribe` listeners only inside the
owning plugin. It skips the native entry (already registered by Velocity), retains
native priority/async semantics, and unregisters its listeners on close.
`crab.fireEvent(event)` returns Velocity's CompletableFuture; the migrated Master
notification events are ordinary classes and their callers intentionally do not
wait, matching the former asynchronous dispatch. Consumers that depended on the
old TabooLib event superclass must be rebuilt/migrated.

Logging (`info`, `warning`, `severe`), `getDataFolder()` and
`releaseResourceFolder(path, overwrite)` are common services. Velocity also has
`onlinePlayers()`, `console()` and `executeConsole(command)`.

Master now publishes its regular and `api` JARs with `build publishToMavenLocal`;
there is no special TabooLib API-build mode. Cubozoa uses Shadow for its embedded
libraries and service descriptors. Both native plugin descriptors are explicit
resources and retain their existing required plugin dependencies.

Inside the two Velocity plugins, `util.ext.Crab.kt` / `utils.ext.Crab.kt` exposes
`internal val plugin get() = MainClass.instance` and
`internal val crab get() = plugin.crab`. Business code imports shared members from `MainClass.Companion` directly,
or qualifies them with `MainClass` where a local name would conflict. Existing
internal `command` / `submitTask` forwarders use the one instance-owned Crab.
The native main class is already registered as an event listener by Velocity;
`crab.registerEvents()` skips it to prevent duplicate lifecycle event delivery.

## Native Paper services

- `crab.registerEvents()` scans only `me.xiaozhangup.carb.event.SubscribeEvent`.
  The annotation uses Bukkit `EventPriority` and `ignoreCancelled`. Existing native
  `@EventHandler` listeners keep their explicit registration, avoiding duplicates.
- `CrabEvent` supplies cancellation and `call()` for shared plugin events. Each
  concrete event declares its own Bukkit `HandlerList` and static `getHandlerList`.
- `crab.registerPlaceholders()` registers `compat.PlaceholderExpansion` implementations
  when PlaceholderAPI is enabled, and owns their unregistration.
- `compat.replacePlaceholder` and Vault balance/account helpers remain Paper-only.
  PlaceholderAPI and Vault are compile-only dependencies and external server plugins.
- `util` supplies item giving/counting/removal, click predicates, attackers/killers,
  Adventure item construction with MiniMessage strings and the existing compressed Bukkit serialization format.
- `crab.inputs` owns chat/book input callbacks, listeners and quit/disable cleanup.
  Project-local Player extension functions forward to this instance.
- `crab.debounce` uses the common task executor; `Baffle`, `RandomList` and text
  progress bars are platform-neutral common utilities. Debounce delays use milliseconds.

Native Paper builds use Shadow plus explicit `plugin.yml`, and publish runtime and
`api` artifacts in one `build publishToMavenLocal` invocation where publication is
configured. There is no separate API-build mode. Shared Crab libraries are not
embedded by consumers.

### Paper 物品构建

`me.xiaozhangup.carb.util.itemStack` 和 `ItemStackBuilder` 来自 Whale 的 Adventure 构建器，支持 Material、现有 ItemStack 和 FlexibleItem 标识。字符串 `name` / `lore` 使用 MiniMessage，并默认关闭斜体；Component 和 adventure-kt DSL 重载保持可用。

```kotlin
itemStack(Material.DIAMOND) {
    name("<aqua>奖励")
    lore("<gray>点击领取", "<gold>每日一次")
    hideAll()
}
```

纹理头像 `skull(texture)` 由 Crab Paper 提供；平台无关的 `componentList` 放在 common 的 `common.util` 包。Whale 保留查询玩家资料的头像封装。旧 Taboo ItemBuilder / buildItem 已移除。
