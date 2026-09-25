# Shared commands and Notify

`me.xiaozhangup.carbkotlin.command` contains one command tree, argument parser,
executor binding, completion, annotation API and CommandHelper implementation.
The common module has no Bukkit or Velocity imports; each platform artifact adds
its own implementation under the same public package.

Each plugin owns one Crab instance and calls `crab.command(...)`, or its own
`internal command(...)` forwarding function. Registry creation and cleanup happen
inside Crab. `crab.commands` remains available for annotated registration and
advanced registry settings. See [CRAB.md](CRAB.md).

Paper uses the
owning plugin's permission prefix and command namespace. Velocity preserves the
previous TabooLib behavior: an empty root permission allows all senders; explicit
permissions are resolved by Velocity (Bukkit OP defaults do not apply there).

```kotlin
import me.xiaozhangup.carbkotlin.command.Notify
import me.xiaozhangup.carbkotlin.command.createHelper

val notify = Notify("示例", "#99ccee")
crab.command("example", permission = "example.use", notify = notify) {
    literal("send") {
        dynamic("message") {
            // Use org.bukkit.entity.Player or com.velocitypowered.api.proxy.Player.
            execute<Player> { player, _, message ->
                notify.send(player, "消息: {0}", message)
            }
        }
    }
    createHelper()
}
```

The DSL keeps `literal`, `dynamic`, `execute`, `exec`, restrictions, suggestions,
optional/hidden nodes, aliases, descriptions, quotes/options and argument access.
Executors receive native platform types, without TabooLib ProxyPlayer/ProxyCommandSender.
`context.sender()` exposes the common Adventure Audience; the typed executor
parameter retains Player/CommandSender/CommandSource.

Player lookup extensions are in `me.xiaozhangup.carbkotlin.command` on both platforms. World and
coordinate helpers are Paper-only. Player-name completion is shared and delegates
to the registry's platform. `createHelper` and `createDescriptionHelper` use the
same common implementation on both platforms, with Notify when supplied.
`crab.commands.languageResolver` resolves `@key` descriptions per plugin and defaults
to the key itself. It does not load TabooLib's language module.

Use `crab.commands.registerAnnotated(instance)` for an existing annotated object or
`crab.commands.registerAnnotated(crab.scanner)` for plugin-local discovery. Whale
keeps its business visitor so disabled modules and existing module instances are
handled in the same way as before.

## Notify

`Notify` is an open class. Prefix, body/argument colours, MiniMessage formatting,
`build(...)` and `send(Audience, ...)` live in CarbKotlin. It can also be used outside
commands. Whale's existing `util.chat.Notify` subclasses it and adds only the
`send(NetworkPlayer, ...)` overload. Cubozoa uses the common class directly.

Explicit Notify command messages preserve the existing escaped plain-text
handling of command errors and help, rather than interpreting command input as
MiniMessage. Existing plugin-specific notification helpers may retain their own
styles independently.

Sources derive from Whale's adaptation of TabooLib 6.3.0-test-6-23-1 (MIT).
The license is included at `META-INF/licenses/TabooLib-command-LICENSE.txt`.
