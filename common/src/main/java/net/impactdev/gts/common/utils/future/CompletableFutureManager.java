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
		});
	}

}
