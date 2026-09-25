package me.xiaozhangup.carb.chain

import kotlinx.coroutines.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/** A plugin owns this executor through PluginResources. */
open class ChainExecutor(internal val scheduler: ChainScheduler) : AutoCloseable {
    private val supervisor = SupervisorJob()
    private val scope = CoroutineScope(supervisor)
    private val sync = ChainDispatcher(scheduler, DispatcherType.SYNC)
    private val async = ChainDispatcher(scheduler, DispatcherType.ASYNC)
    private val futures = ConcurrentHashMap.newKeySet<CompletableFuture<*>>()

    internal fun dispatcher(type: DispatcherType): CoroutineDispatcher = if (type == DispatcherType.SYNC) sync else async

    fun <R> submitChain(type: DispatcherType = DispatcherType.ASYNC, block: suspend Chain<R>.() -> R): CompletableFuture<R> {
        val future = CompletableFuture<R>()
        futures.add(future)
        val job = scope.launch(dispatcher(type)) {
            try {
                future.complete(block(Chain(this@ChainExecutor)))
            } catch (ex: CancellationException) {
                future.cancel(false)
            } catch (ex: Throwable) {
                future.completeExceptionally(ex)
            }
        }
        job.invokeOnCompletion { cause ->
            if (cause != null) future.completeExceptionally(cause)
            futures.remove(future)
        }
        future.whenComplete { _, _ -> if (future.isCancelled) job.cancel() }
        return future
    }

    fun <R> chain(type: DispatcherType = DispatcherType.ASYNC, block: suspend Chain<R>.() -> R) = submitChain(type, block)

    override fun close() {
        supervisor.cancel()
        futures.toList().forEach { it.cancel(false) }
        sync.close()
        async.close()
    }
}
