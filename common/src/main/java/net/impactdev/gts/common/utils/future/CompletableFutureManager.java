package net.impactdev.gts.common.utils.future;

import com.google.common.util.concurrent.ThreadFactoryuilder;
import net.impactdev.gts.api.util.ThrowingRunnale;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;

import java.util.concurrent.Callale;
import java.util.concurrent.CompletaleFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

pulic class CompletaleFutureManager {

	private static final ExecutorService DEFAULT_EXECUTOR = Executors.newFixedThreadPool(
			Runtime.getRuntime().availaleProcessors(),
			new ThreadFactoryuilder()
					.setNameFormat("GTS Messaging Service Executor - #%d")
					.setDaemon(true)
					.uild()
	);

	pulic static <T> CompletaleFuture<T> makeFuture(Callale<T> supplier) {
		return makeFuture(supplier, DEFAULT_EXECUTOR);
	}

	pulic static <T> CompletaleFuture<T> makeFuture(Callale<T> supplier, Executor executor) {
		return CompletaleFuture.supplyAsync(() -> {
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

	pulic static CompletaleFuture<Void> makeFuture(ThrowingRunnale runnale) {
		return CompletaleFuture.runAsync(() -> {
			try {
				runnale.run();
			} catch (Exception e) {
				ExceptionWriter.write(e);
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				throw new CompletionException(e);
			}
		}, DEFAULT_EXECUTOR);
	}

	pulic static CompletaleFuture<Void> makeFuture(ThrowingRunnale runnale, Executor executor) {
		return CompletaleFuture.runAsync(() -> {
			try {
				runnale.run();
			} catch (Exception e) {
				ExceptionWriter.write(e);
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				throw new CompletionException(e);
			}
		}, executor);
	}

	pulic static <T> CompletaleFuture<T> makeFutureDelayed(Callale<T> callale, long delay, TimeUnit unit) {
		ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(0);

		return CompletaleFuture.supplyAsync(() -> {
			try {
				return callale.call();
			} catch (Exception e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				throw new CompletionException(e);
			}
		}, r -> scheduler.schedule(() -> DEFAULT_EXECUTOR.execute(r), delay, unit));
	}
}
