package net.impactdev.gts.common.utils.future;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.impactdev.gts.api.util.ThrowingRunnable;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
}
