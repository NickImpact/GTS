package com.nickimpact.gts.storage;

import com.google.common.base.Throwables;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.entries.Entry;
import com.nickimpact.gts.api.listings.entries.EntryHolder;
import com.nickimpact.gts.api.listings.pricing.Price;
import com.nickimpact.gts.api.listings.pricing.PriceHolder;
import com.nickimpact.gts.api.utils.MessageUtils;
import com.nickimpact.gts.logs.Log;
import com.nickimpact.gts.storage.dao.AbstractDao;
import com.nickimpact.gts.storage.wrappings.PhasedStorage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.Sponge;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AbstractStorage implements Storage {

	public static Storage create(GTS plugin, AbstractDao backing) {
		return PhasedStorage.of(new AbstractStorage(plugin, backing));
	}

	private final GTS plugin;
	private final AbstractDao dao;

	private <T> CompletableFuture<T> makeFuture(Callable<T> supplier) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return supplier.call();
			} catch (Exception e) {
				Throwables.propagateIfPossible(e);
				throw new CompletionException(e);
			}
		}, GTS.getInstance().getAsyncExecutorService());
	}

	private CompletableFuture<Void> makeFuture(ThrowingRunnable runnable) {
		return CompletableFuture.runAsync(() -> {
			try {
				runnable.run();
			} catch (Exception e) {
				Throwables.propagateIfPossible(e);
				throw new CompletionException(e);
			}
		}, GTS.getInstance().getAsyncExecutorService());
	}

	private interface ThrowingRunnable {
		void run() throws Exception;
	}

	@Override
	public String getName() {
		return dao.getName();
	}

	@Override
	public void init() {
		try {
			dao.init();
		} catch (Exception e) {
			MessageUtils.genAndSendErrorMessage(
					"Storage Init Error",
					"Failed to load storage dao",
					"Error report is as follows: "
			);
			e.printStackTrace();
		}
	}

	@Override
	public void shutdown() {
		try {
			dao.shutdown();
		} catch (Exception e) {
			MessageUtils.genAndSendErrorMessage(
					"Storage Shutdown Error",
					"Failed to close storage dao",
					"Error report is as follows: "
			);
			e.printStackTrace();
		}
	}

	@Override
	public CompletableFuture<Void> addListing(Listing listing) {
		return makeFuture(() -> dao.addListing(listing));
	}

	@Override
	public CompletableFuture<Void> removeListing(int id) {
		return makeFuture(() -> dao.removeListing(id));
	}

	@Override
	public CompletableFuture<List<Listing>> getListings() {
		return makeFuture(dao::getListings);
	}

	@Override
	public CompletableFuture<Void> addLog(Log log) {
		return makeFuture(() -> dao.addLog(log));
	}

	@Override
	public CompletableFuture<Void> removeLog(int id) {
		return makeFuture(() -> dao.removeLog(id));
	}

	@Override
	public CompletableFuture<List<Log>> getLogs() {
		return makeFuture(dao::getLogs);
	}

	@Override
	public CompletableFuture<Void> addHeldElement(EntryHolder holder) {
		return makeFuture(() -> dao.addHeldElement(holder));
	}

	@Override
	public CompletableFuture<Void> removeHeldElement(EntryHolder holder) {
		return makeFuture(() -> dao.removeHeldElement(holder));
	}

	@Override
	public CompletableFuture<List<EntryHolder>> getHeldElements() {
		return makeFuture(dao::getHeldElements);
	}

	@Override
	public CompletableFuture<Void> addHeldPrice(PriceHolder holder) {
		return makeFuture(() -> dao.addHeldPrice(holder));
	}

	@Override
	public CompletableFuture<Void> removeHeldPrice(PriceHolder holder) {
		return makeFuture(() -> dao.removeHeldPrice(holder));
	}

	@Override
	public CompletableFuture<List<PriceHolder>> getHeldPrices() {
		return makeFuture(dao::getHeldPrices);
	}

	@Override
	public CompletableFuture<Void> purge(boolean logs) {
		return makeFuture(() -> dao.purge(logs));
	}

	@Override
	public CompletableFuture<Void> save() {
		return makeFuture(dao::save);
	}
}
