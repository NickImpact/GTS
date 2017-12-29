package com.nickimpact.gts.storage.wrappings;

import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.entries.Entry;
import com.nickimpact.gts.api.listings.pricing.Price;
import com.nickimpact.gts.logs.Log;
import com.nickimpact.gts.storage.Storage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PhasedStorage implements Storage {

	public static PhasedStorage of(Storage storage) {
		return new PhasedStorage(storage);
	}

	private final Storage delegate;

	private final Phaser phaser = new Phaser();

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public void init() {
		delegate.init();
	}

	@Override
	public void shutdown() {
		try {
			phaser.awaitAdvanceInterruptibly(phaser.getPhase(), 10, TimeUnit.SECONDS);
		} catch (InterruptedException | TimeoutException e) {
			e.printStackTrace();
		}

		delegate.shutdown();
	}

	@Override
	public CompletableFuture<Void> addListing(Listing listing) {
		phaser.register();
		try {
			return delegate.addListing(listing);
		} finally {
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public CompletableFuture<Void> removeListing(int id) {
		phaser.register();
		try {
			return delegate.removeListing(id);
		} finally {
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public CompletableFuture<List<Listing>> getListings() {
		phaser.register();
		try {
			return delegate.getListings();
		} finally {
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public CompletableFuture<Void> addLog(Log log) {
		phaser.register();
		try {
			return delegate.addLog(log);
		} finally {
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public CompletableFuture<Void> removeLog(int id) {
		phaser.register();
		try {
			return delegate.removeLog(id);
		} finally {
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public CompletableFuture<List<Log>> getLogs() {
		phaser.register();
		try {
			return delegate.getLogs();
		} finally {
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public CompletableFuture<Void> addHeldElement(UUID uuid, Entry entry) {
		return null;
	}

	@Override
	public CompletableFuture<Void> removeHeldElement(UUID uuid, Entry entry) {
		return null;
	}

	@Override
	public CompletableFuture<List<Entry>> getHeldElements() {
		return null;
	}

	@Override
	public CompletableFuture<Void> addHeldPrice(UUID uuid, Price price) {
		return null;
	}

	@Override
	public CompletableFuture<Void> removeHeldPrice(UUID uuid, Price price) {
		return null;
	}

	@Override
	public CompletableFuture<List<Price>> getHeldPrices() {
		return null;
	}

	@Override
	public CompletableFuture<Void> purge(boolean logs) {
		phaser.register();
		try {
			return delegate.purge(logs);
		} finally {
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public CompletableFuture<Void> save() {
		phaser.register();
		try {
			return delegate.save();
		} finally {
			phaser.arriveAndDeregister();
		}
	}
}
