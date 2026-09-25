# Common utilities

Based on TabooLib common-util 6.3.0-test-6-23-1 (MIT). Consumers use the existing
non-transitive CarbKotlin `api` compile-only artifact; never shade these classes.

## Packages

All public general-purpose tools live under `me.xiaozhangup.carb.common`:

- `util`: collections, iteration, strings/placeholders, variable parsing, progress
  bars, random numbers, numeric helpers, optional values, lazy delegates, timing,
  Java primitive aliases, `Location`, `Vector`, `Version`, and composite class maps.
- `io`: file creation/copy/traversal/deletion, digests, ZIP and GZIP.
- `function`: all throttle overloads and their function types.
- `event`: internal event bus, cancellable events and listeners. This is a Crab
  event bus; it does not receive TabooLib's internal framework events.
- `reflect`: annotation helpers and legacy reflection wrappers delegating to the
  shared Reflex implementation. `ClassHelper` is a Kotlin alias for the existing
  `me.xiaozhangup.carb.reflect.util.ClassHelper` (Java callers use that class).

```kotlin
import me.xiaozhangup.carb.common.io.newFile
import me.xiaozhangup.carb.common.util.orNull
import me.xiaozhangup.carb.common.util.randomDouble
import me.xiaozhangup.carb.common.function.throttle
```

No TabooLib runtime is needed by these tools. Guava is provided by the supported
Paper/Velocity platforms, as with the other shared services.

## Deliberate integration differences

- `String.t()` chooses Chinese using the JVM default locale.
- Asynchronous deletion runs each complete tree in one virtual-thread task. It
  preserves `await`/`futures` without a fixed-pool parent/child waiting deadlock.
- ZIP extraction checks canonical output paths against the destination directory,
  including existing symlinks, and creates directory entries as directories.
- Internal events, throttle registries and lazy reset groups are now shared across
  plugins. Keep group names plugin-specific; cancel listeners/clear throttles when
  their owner shuts down. Existing consumers use local lazy reset and singleton
  throttles and do not need global registration.
- Scanning uses the existing owner-scoped `PluginResources.scanner`/`PluginScanner`,
  rather than copying TabooLib's global classloader scanner and binary cache.

## Framework code not copied

The original JAR also contains `@Awake`, `@DelayTo`, `@SkipTo`, `@Inject`, `@Requires`,
`@Ghost`, `Releasable`, ClassVisitor and its handler/groups, framework `Test`,
`UnsupportedVersionException`, OpenContainer, ProjectInfo and BinaryCache.
These belong to the original lifecycle/bootstrap and are not copied as inert
annotations. Consumers now use native plugin entries and Crab lifecycle/event
registration. `crab.debounce` uses the shared task executor; `Baffle`, `RandomList`
and `createBar` live in `common.util`. No consumer needs the original framework
for these utilities.

Source license: `META-INF/licenses/TabooLib-common-util-LICENSE.txt`.
