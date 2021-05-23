package net.impactdev.gts.common.utils.future

import com.google.common.util.concurrent.ThreadFactoryBuilder
import net.impactdev.gts.api.util.ThrowingRunnable
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter
import java.util.concurrent.*
import java.util.function.Supplier

object CompletableFutureManager {
    private val DEFAULT_EXECUTOR = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors(),
        ThreadFactoryBuilder()
            .setNameFormat("GTS Messaging Service Executor - #%d")
            .setDaemon(true)
            .build()
    )

    fun <T> makeFuture(supplier: Callable<T>): CompletableFuture<T> {
        return makeFuture(supplier, DEFAULT_EXECUTOR)
    }

    fun <T> makeFuture(supplier: Callable<T>, executor: Executor?): CompletableFuture<T> {
        return CompletableFuture.supplyAsync(Supplier<T> {
            try {
                return@supplyAsync supplier.call()
            } catch (e: Exception) {
                ExceptionWriter.write(e)
                if (e is RuntimeException) {
                    throw e
                }
                throw CompletionException(e)
            }
        }, executor)
    }

    fun makeFuture(runnable: ThrowingRunnable): CompletableFuture<Void> {
        return CompletableFuture.runAsync(Runnable {
            try {
                runnable.run()
            } catch (e: Exception) {
                ExceptionWriter.write(e)
                if (e is RuntimeException) {
                    throw e
                }
                throw CompletionException(e)
            }
        }, DEFAULT_EXECUTOR)
    }

    @kotlin.jvm.JvmStatic
    fun makeFuture(runnable: ThrowingRunnable, executor: Executor?): CompletableFuture<Void> {
        return CompletableFuture.runAsync(Runnable {
            try {
                runnable.run()
            } catch (e: Exception) {
                ExceptionWriter.write(e)
                if (e is RuntimeException) {
                    throw e
                }
                throw CompletionException(e)
            }
        }, executor)
    }

    fun <T> makeFutureDelayed(callable: Callable<T>, delay: Long, unit: TimeUnit?): CompletableFuture<T> {
        val scheduler: ScheduledExecutorService = ScheduledThreadPoolExecutor(0)
        return CompletableFuture.supplyAsync({
            try {
                return@supplyAsync callable.call()
            } catch (e: Exception) {
                if (e is RuntimeException) {
                    throw e
                }
                throw CompletionException(e)
            }
        }) { r: Runnable? -> scheduler.schedule({ DEFAULT_EXECUTOR.execute(r) }, delay, unit) }
    }
}