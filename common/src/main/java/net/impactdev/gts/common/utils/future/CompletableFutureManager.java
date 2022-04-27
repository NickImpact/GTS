package net.impactdev.gts.common.utils.future;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.api.messaging.message.exceptions.MessagingException;
import net.impactdev.gts.api.util.ThrowingRunnable;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;

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

public class CompletableFutureManager {

	private static final ExecutorService DEFAULT_EXECUTOR = Executors.newFixedThreadPool(
			Runtime.getRuntime().availableProcessors(),
			new ThreadFactoryBuilder()
					.setNameFormat("GTS Messaging Service Executor - #%d")
					.setDaemon(true)
					.build()
	);

	public static <T> CompletableFuture<T> makeFuture(Callable<T> supplier) {
		return makeFuture(supplier, DEFAULT_EXECUTOR);
	}

	public static <T> CompletableFuture<T> makeFuture(Callable<T> supplier, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return supplier.call();
			} catch (Exception e) {
				ExceptionWriter.write(e);
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				throw new CompletionException(e);
			}
		}, executor);
	}

	public static CompletableFuture<Void> makeFuture(ThrowingRunnable runnable) {
		return CompletableFuture.runAsync(() -> {
			try {
				runnable.run();
			} catch (Exception e) {
				ExceptionWriter.write(e);
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				throw new CompletionException(e);
			}
		}, DEFAULT_EXECUTOR);
	}

	public static CompletableFuture<Void> makeFuture(ThrowingRunnable runnable, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			try {
				runnable.run();
			} catch (Exception e) {
				ExceptionWriter.write(e);
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				throw new CompletionException(e);
			}
		}, executor);
	}

	public static <T> CompletableFuture<T> makeFutureDelayed(Callable<T> callable, long delay, TimeUnit unit) {
		ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(0);

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
		return CompletableFutureManager.makeFutureDelayed(() -> {
			throw new MessagingException(ErrorCodes.REQUEST_TIMED_OUT, new TimeoutException());
		}, timeout, unit);
	}
}
