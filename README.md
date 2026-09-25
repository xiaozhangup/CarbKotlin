# CrabKotlin

Shared Kotlin runtime and plugin services, built separately for Paper and Velocity.

## Modules and artifacts

- `common`: configuration, database, Redis, reflection, common utilities, command
  DSL and task-chain implementation; no dependency on either platform API.
- `paper`: Paper entry point, command registration, player/location helpers and scheduler.
- `velocity`: Velocity entry point, command registration, player helpers and scheduler.

Build and publish locally with `./gradlew build publishToMavenLocal`.

| Platform | Runtime JAR | Compile-only dependency |
| --- | --- | --- |
| Paper | `paper/build/libs/CrabKotlin-2.3.20-paper.jar` | `me.xiaozhangup.crab:CrabKotlin:2.3.20:paper` |
| Velocity | `velocity/build/libs/CrabKotlin-2.3.20-velocity.jar` | `me.xiaozhangup.crab:CrabKotlin:2.3.20:velocity` |

Both platform JARs are self-contained, including common and the bundled libraries.
Install only the appropriate runtime JAR on each server; replace the old combined
CrabKotlin JAR. Plugin identity remains `CrabKotlin` on Paper and `crabkotlin` on
Velocity, so plugin dependency declarations do not change. The `-api` and `-plain`
JARs are not server plugins. Do not put both platform variants on one classpath.

Consumers keep `isTransitive = false` and do not shade CrabKotlin. The thin API JAR
contains the common and selected platform API, not the bundled Kotlin runtime.

## Unified calls

Plugins extend `me.xiaozhangup.crab.CrabPlugin` and override `load()`, `enable()`,
`active()` or `disable()` as needed. The base owns initialization, annotated configuration,
commands/listeners, task startup and resource cleanup on both platforms. Each plugin
uses its inherited `crab` instance and module-local top-level wrappers:

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
`me.xiaozhangup.crab:CrabKotlin:2.3.20`. Full plugin JARs are build/upload artifacts,
not Maven publications.

## FlexibleItem (Paper)

`me.xiaozhangup.crab.flexible` provides `FlexibleItem`, `FlexibleItemHandler`
and the `flexibleItem` conversion functions. Crab owns the shared registry and API,
and supplies `minecraft`, `head`, and `base64` (`bukkit` alias) without Whale.
Built-in handlers are available on first use and need no lifecycle registration.

Explicit custom namespaces use their registered handler. Reverse conversion checks
registered handlers first, then the built-in Minecraft and player-head handlers.
Base64 is used only when all of those decline an item; `toFlexibleItem(item, false)`
returns `null` in that case. The existing material, head texture/owner and Base64
formats are preserved.

WhaleMechanism registers `craftengine` (`itemsadder` alias) and `customfishing`
in `onLoad`, before plugins enter `onEnable`. These provider integrations remain in
`me.xiaozhangup.whale.util.flexible.handler`. Plugins can add handlers with
`FlexibleItem.registerHandler(...)`.

## Shared binary codecs

`me.xiaozhangup.crab.serialization` in `common` provides `ByteReader`, `ByteWriter`,
the two `byteArray` functions and `UUIDSerializable` on both Paper and Velocity.
The format retains big-endian primitives, UTF-8 strings with a four-byte length,
UUIDs as two longs, collection/map element counts and optional GZIP compression.
Adventure components keep the existing Gson JSON representation; UUID JSON values
remain strings. This does not change ItemStack serialization formats.

```kotlin
import me.xiaozhangup.crab.serialization.byteArray

val bytes = byteArray { writeString("example"); writeInt(42) }
val result = byteArray(bytes) { readString() to readInt() }
```

Island, user and showcase codecs remain in SlimeCargoNext and SlimeMasterNext,
each in `utils/ByteArrayUtils.kt`, as extensions on the shared reader/writer.
Crab has no dependency on those domain objects.

Consumers must import these codecs from Crab directly. The former Whale, Cargo,
Master and Cubozoa codec implementations and the duplicated UUID serializers in
Master, Opossum and OrangDomain have been removed; plugins
compiled against them must be rebuilt. This also applies to callbacks exposed by
Whale's message and metadata APIs, whose receiver types now come from Crab.
