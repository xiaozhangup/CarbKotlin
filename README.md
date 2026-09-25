# CarbKotlin

Shared Kotlin runtime and plugin services, built separately for Paper and Velocity.

## Modules and artifacts

- `common`: configuration, database, Redis, reflection, common utilities, command
  DSL and task-chain implementation; no dependency on either platform API.
- `paper`: Paper entry point, command registration, player/location helpers and scheduler.
- `velocity`: Velocity entry point, command registration, player helpers and scheduler.

Build and publish locally with `./gradlew build publishToMavenLocal`.

| Platform | Runtime JAR | Compile-only dependency |
| --- | --- | --- |
| Paper | `paper/build/libs/CarbKotlin-2.3.20-paper.jar` | `me.xiaozhangup.crab:CarbKotlin:2.3.20:paper` |
| Velocity | `velocity/build/libs/CarbKotlin-2.3.20-velocity.jar` | `me.xiaozhangup.crab:CarbKotlin:2.3.20:velocity` |

Both platform JARs are self-contained, including common and the bundled libraries.
Install only the appropriate runtime JAR on each server; replace the old combined
CarbKotlin JAR. Plugin identity remains `CarbKotlin` on Paper and `carbkotlin` on
Velocity, so plugin dependency declarations do not change. The `-api` and `-plain`
JARs are not server plugins. Do not put both platform variants on one classpath.

Consumers keep `isTransitive = false` and do not shade CarbKotlin. The thin API JAR
contains the common and selected platform API, not the bundled Kotlin runtime.

## Unified calls

Each plugin holds one `Crab` instance and uses module-local top-level wrappers:

```kotlin
command("example") { /* command DSL */ }
submitTask(delay = 20) { /* task */ }
submitAsyncTask { /* async task */ }
submitChain { /* chain DSL */ }
```

These helpers are `internal` in each plugin's `util/ext/Crab.kt` or `utils/ext/Crab.kt` and forward to
that plugin's Crab. The common services own registration and cleanup. See
[Crab initialization, ownership and shutdown](CRAB.md).

- [Commands, CommandHelper and Notify](COMMANDS.md)
- [Task chains](CHAINS.md)
- [Database DSL](DATABASE.md)
- [Configuration, Redis and reflection](SHARED_SERVICES.md)
- [Common utilities](COMMON_UTIL.md)

Library versions remain in `versions/`.

Maven publishes only classifiers `paper` and `velocity` under
`me.xiaozhangup.crab:CarbKotlin:2.3.20`. Full plugin JARs are build/upload artifacts,
not Maven publications.

## FlexibleItem (Paper)

`me.xiaozhangup.carb.flexible` provides `FlexibleItem`, `FlexibleItemHandler`
and the `flexibleItem` conversion functions. Crab owns the shared registry and API,
plus a built-in `base64` handler (`bukkit` alias). Base64 is kept outside the
registry and used only after all registered handlers decline an item, regardless
of registration order. Explicit `toFlexibleItem(item, false)` returns `null`
when no registered handler matches. Base64 decoding is available without Whale.

WhaleMechanism registers the default handlers in `onLoad`, before plugins enter
`onEnable`: `minecraft`, `craftengine` (`itemsadder` alias), `customfishing`,
`head`. These item-provider implementations live in
`me.xiaozhangup.whale.util.flexible`. Existing item ID formats are preserved.
Plugins can add handlers with `FlexibleItem.registerHandler(...)`.
