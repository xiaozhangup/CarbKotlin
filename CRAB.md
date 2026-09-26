# Per-plugin Crab facade

Each plugin owns exactly one `me.xiaozhangup.crab.Crab`. Both platform
artifacts expose the same facade. No stack inspection or global default owner is
used. The instance owns command registrations, task chains, ordinary scheduled
tasks, configuration watchers and Redis connections.

## Plugin entry point

Both platform artifacts expose `me.xiaozhangup.crab.CrabPlugin`. Paper's base
extends `JavaPlugin`; Velocity's base owns Guice injection and proxy event hooks.
Consumer entry classes need no Crab construction, lifecycle dispatch, event
registration or resource closure:

```kotlin
class ExamplePlugin : CrabPlugin() {
    override fun load() {
        // Annotated configurations are already available.
    }

    override fun enable() {
        crab.command("example") { /* command DSL */ }
    }

    override fun active() {
        // Business activation after enable.
    }

    override fun disable() {
        // Finish business saves; Crab is closed afterwards, even on failure.
    }
}
```

All four hooks are optional, protected and default to no-op. Do not call `super`.
Native entry callbacks are final. The base exposes one `crab` instance, binds the
actual entry to its scanner, and automatically loads `@Config` fields, registers
Crab listeners, Paper placeholder expansions and `@CommandHeader` commands,
starts queued tasks and releases owned resources.

Paper dispatches CONST, loads configurations, then dispatches INIT and LOAD
before `load()`. ENABLE, listener/placeholder/annotated-command registration and
`enable()` precede task startup. ACTIVE and `active()` run one tick later.
Shutdown dispatches DISABLE, calls `disable()` and closes Crab using nested
finally blocks.

Velocity supplies inherited `server: ProxyServer` and `dataDirectory: Path`.
Guice fills these and runs CONST/configuration loading/INIT after the subclass
constructor has completed. Access injected services from lifecycle hooks, not
constructor initializers. Proxy initialization dispatches LOAD/`load()`, then
ENABLE/annotated commands/`enable()`, registers listeners and starts queued tasks.
ACTIVE/`active()` run as an owned task, preserving the previous scheduling.
Shutdown calls `disable()`, dispatches DISABLE in a finally block and closes Crab.
Its native `@Subscribe` methods are inherited and registered once by Velocity.

Plugins may retain a companion `instance` for project-local helpers and shared
business state. Set it in `init { instance = this }`; the base does not invoke
business hooks from its constructor.

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
not restricted. The inherited `crab` property is public; project-local top-level wrappers remain `internal`.

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

## Lifecycle annotations and manual integration

`CrabPlugin` drives `crab.lifecycle` automatically. Use
`me.xiaozhangup.crab.lifecycle.Awake` on no-argument methods; lower `priority`
numbers execute first, with scanner order breaking ties. Each stage executes
once. Discovery reads bytecode without constructing every class; owners are
resolved only when their annotated method runs. DISABLE collects handler
exceptions so one failure does not skip the remaining handlers.

Do not manually dispatch stages, reload all annotated configurations, register
all annotated commands/listeners or close Crab from a `CrabPlugin` hook. Those
operations are owned by the base. Per-object dynamic registrations still use
`crab` directly. Specialized business resources remain the plugin's responsibility.

The standalone `Crab` and platform-neutral `LifeCycleDispatcher` utility remain available
for native integrations that cannot extend `CrabPlugin`. Such integrations still
construct/bind their Crab and explicitly drive initialization and shutdown.
The utility itself installs no platform hooks.

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

- `crab.registerEvents()` scans only `me.xiaozhangup.crab.event.SubscribeEvent`.
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

`me.xiaozhangup.crab.util.itemStack` 和 `ItemStackBuilder` 来自 Whale 的 Adventure 构建器，支持 Material、现有 ItemStack 和 FlexibleItem 标识。字符串 `name` / `lore` 使用 MiniMessage，并默认关闭斜体；Component 和 adventure-kt DSL 重载保持可用。

```kotlin
itemStack(Material.DIAMOND) {
    name("<aqua>奖励")
    lore("<gray>点击领取", "<gold>每日一次")
    hideAll()
}
```

纹理头像 `skull(texture)` 由 Crab Paper 提供；平台无关的 `componentList` 放在 common 的 `common.util` 包。Whale 保留查询玩家资料的头像封装。旧 Taboo ItemBuilder / buildItem 已移除。
