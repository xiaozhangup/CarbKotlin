package me.xiaozhangup.crab.common.io

import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.Future

private val executor = Executors.newThreadPerTaskExecutor(
    Thread.ofVirtual().name("crabkotlin-file-delete-", 0).factory()
)

/** Delete the complete tree in one task, without workers waiting for child tasks. */
fun File.deepDeleteAsync(await: Boolean = false, futures: MutableSet<Future<*>>? = null) {
    val future = executor.submit { deepDelete() }
    futures?.add(future)
    if (await) future.get()
}
