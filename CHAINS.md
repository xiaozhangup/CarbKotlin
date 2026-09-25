# Task chains

The common `me.xiaozhangup.carb.chain` DSL retains `async`, `sync`,
`wait`, repeating blocks and `Cancellable.cancel()`. `submitChain` returns a
CompletableFuture. Failure completes it exceptionally; cancelling the future
cancels its coroutine and repeating tasks.

Each plugin submits through its Crab instance (or its module-local forwarding function):

```kotlin
crab.submitChain {
    val result = async { loadData() }
    sync { updateMenu(result) }
}
```

Default entry mode is ASYNC; `DispatcherType.SYNC` starts on the synchronous
backend. Paper uses its main-thread/asynchronous scheduler. Velocity uses its
native scheduler for both modes; SYNC does not imply a global main thread there.
This Paper adapter targets Paper, not Folia entity/region affinity.

`wait(n, DurationType.MILLIS)` uses milliseconds. MINECRAFT_TICK waits use a fixed
50 ms conversion, matching the previous implementation. Repeating sync/async
blocks use server ticks on Paper and tick * 50 ms on Velocity. A positive period
is required. Calling `cancel()` inside a repeating block stops repetition and
returns that invocation's value to the chain. As with TabooLib's `submit(now=true)`,
`now=true` invokes the block once immediately, ignoring delay/period; the block
must call cancel() to finish the repeat step. Otherwise it awaits cancellation.

`crab.close()` cancels the plugin's task chains, including delayed/repeating
steps and pending Futures. Cancelled continuations can finish cleanup on an IO
thread during shutdown; plugin-state operations must not be placed in shutdown
cleanup that assumes a live server main thread. Cancellation is cooperative and
does not interrupt an already-running synchronous business function.

The API follows TabooLib basic-submit-chain 6.3.0-test-6-23-1 (MIT). The scheduling
and coroutine ownership implementation is adapted for CarbKotlin. The MIT license
is included at META-INF/licenses/TabooLib-command-LICENSE.txt.
