package net.impactdev.gts.util.future;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.impactor.api.utility.ExceptionPrinter;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Futures {

    private static final ExecutorService DEFAULT_EXECUTOR = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            new ThreadFactoryBuilder()
                    .setNameFormat("GTS Async Executor - #%d")
                    .setDaemon(true)
                    .build()
    );

    public static <T> CompletableFuture<T> execute(Callable<T> method) {
        return execute(DEFAULT_EXECUTOR, method);
    }

    public static <T> CompletableFuture<T> execute(Executor executor, Callable<T> method) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return method.call();
            } catch (Exception e) {
                ExceptionPrinter.print(GTSPlugin.instance().logger(), e);
                throw new CompletionException(e);
            }
        }, executor);
    }

    public static CompletableFuture<Void> execute(ThrowingRunnable runnable) {
        return execute(DEFAULT_EXECUTOR, runnable);
    }

    public static CompletableFuture<Void> execute(Executor executor, ThrowingRunnable runnable) {
        return CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                ExceptionPrinter.print(GTSPlugin.instance().logger(), e);
                throw new CompletionException(e);
            }
        }, executor);
    }

    public static <T> CompletableFuture<T> makeFutureDelayed(Callable<T> callable, long delay, TimeUnit unit) {
        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return callable.call();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new CompletionException(e);
            }
        }, r -> scheduler.schedule(() -> DEFAULT_EXECUTOR.execute(r), delay, unit));
    }

    public static CompletableFuture<Void> timed(ThrowingRunnable runnable, long duration, TimeUnit unit) {
        return execute(runnable).acceptEither(timeoutAfter(duration, unit), ignore -> {});
    }

    public static <T> CompletableFuture<T> timed(Callable<T> callable, long duration, TimeUnit unit) {
        return execute(callable).applyToEither(timeoutAfter(duration, unit), value -> value);
    }

    /**
     * Forces a completable future to timeout its actions after the specified amount of time. This is best used
     * with {@link CompletableFuture#acceptEither(CompletionStage, Consumer) acceptEither},
     * {@link CompletableFuture#applyToEither(CompletionStage, Function) applyToEither}, or any of their respective
     * async companions.
     *
     * @param timeout The amount of time that it should take before we forcibly raise a timeout exception
     * @param unit The time unit to measure our timeout value by
     * @param <W> The intended return type of the completable future (for compatibility with both run and supply)
     * @return A completable future who's sole purpose is to timeout after X amount of time
     */
    public static <W> CompletableFuture<W> timeoutAfter(long timeout, TimeUnit unit) {
        return makeFutureDelayed(() -> {
            throw new TimeoutException();
        }, timeout, unit);
    }

}
