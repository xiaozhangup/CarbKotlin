package me.xiaozhangup.carb

import me.xiaozhangup.carb.chain.*
import me.xiaozhangup.carb.command.*
import me.xiaozhangup.carb.command.component.CommandBase
import me.xiaozhangup.carb.task.Task
import me.xiaozhangup.carb.task.TaskExecutor
import java.io.File

/** Shared services and lifecycle for exactly one owning plugin. */
open class CrabContext(
    anchor: Class<*>,
    dataFolder: () -> File,
    commandFactory: () -> CommandRegistry,
    schedulerFactory: () -> ChainScheduler,
) : PluginResources(anchor, dataFolder) {
    private val tasks = own(TaskExecutor(schedulerFactory))
    val commands by lazy { own(commandFactory()) }
    private val chains by lazy { own(ChainExecutor(tasks)) }

    /** Start queued tasks when the owning plugin is enabled. */
    fun start() = tasks.start()

    /** Used during synchronous shutdown/save operations before close(). */
    var runTasksImmediately: Boolean
        get() = tasks.runImmediately
        set(value) { tasks.runImmediately = value }

    fun command(
        name: String,
        aliases: List<String> = emptyList(),
        description: String = "",
        usage: String = "",
        permission: String = "",
        permissionMessage: String = "",
        permissionDefault: PermissionDefault = PermissionDefault.OP,
        permissionChildren: Map<String, PermissionDefault> = emptyMap(),
        newParser: Boolean = false,
        notify: Notify? = null,
        commandBuilder: CommandBase.() -> Unit,
    ) = commands.command(name, aliases, description, usage, permission, permissionMessage, permissionDefault, permissionChildren, newParser, notify, commandBuilder)

    fun submitTask(now: Boolean = false, async: Boolean = false, delay: Long = 0, period: Long = 0,
                   executor: Task.() -> Unit): Task = tasks.submit(now, async, delay, period, executor = executor)

    fun submitAsyncTask(now: Boolean = false, delay: Long = 0, period: Long = 0,
                        executor: Task.() -> Unit): Task = submitTask(now, true, delay, period, executor)

    /** Retains a plugin's dedicated serial work queue while tracking its task handles. */
    fun submitTask(executor: java.util.concurrent.ScheduledExecutorService, delay: Long = 0,
                   unit: java.util.concurrent.TimeUnit = java.util.concurrent.TimeUnit.MILLISECONDS,
                   runnable: Runnable): Task = tasks.submit(false, true, 0, 0, schedule = { action ->
        val future = executor.schedule(action, delay, unit)
        AutoCloseable { future.cancel(false) }
    }) { runnable.run() }

    fun <R> submitChain(type: DispatcherType = DispatcherType.ASYNC, block: suspend Chain<R>.() -> R) =
        chains.submitChain(type, block)

    fun debounce(delay: Long, async: Boolean = false, action: () -> Unit) =
        me.xiaozhangup.carb.task.Debounce(this, delay, async, action)
}
