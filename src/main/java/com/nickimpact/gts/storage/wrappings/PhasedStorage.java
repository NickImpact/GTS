/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.nickimpact.gts.storage.wrappings;

import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.entries.Entry;
import com.nickimpact.gts.api.listings.entries.EntryHolder;
import com.nickimpact.gts.api.listings.pricing.Price;
import com.nickimpact.gts.api.listings.pricing.PriceHolder;
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
	public CompletableFuture<Void> updateListing(Listing listing) {
		phaser.register();
		try {
			return delegate.updateListing(listing);
		} finally {
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public CompletableFuture<Void> removeListing(UUID uuid) {
		phaser.register();
		try {
			return delegate.removeListing(uuid);
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
	public CompletableFuture<Void> addHeldElement(EntryHolder holder) {
		phaser.register();
		try {
			return delegate.addHeldElement(holder);
		} finally {
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public CompletableFuture<Void> removeHeldElement(EntryHolder holder) {
		phaser.register();
		try {
			return delegate.removeHeldElement(holder);
		} finally {
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public CompletableFuture<List<EntryHolder>> getHeldElements() {
		phaser.register();
		try {
			return delegate.getHeldElements();
		} finally {
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public CompletableFuture<Void> addHeldPrice(PriceHolder holder) {
		phaser.register();
		try {
			return delegate.addHeldPrice(holder);
		} finally {
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public CompletableFuture<Void> removeHeldPrice(PriceHolder holder) {
		phaser.register();
		try {
			return delegate.removeHeldPrice(holder);
		} finally {
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public CompletableFuture<List<PriceHolder>> getHeldPrices() {
		phaser.register();
		try {
			return delegate.getHeldPrices();
		} finally {
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public CompletableFuture<Void> addIgnorer(UUID uuid) {
		phaser.register();
		try {
			return delegate.addIgnorer(uuid);
		} finally {
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public CompletableFuture<Void> removeIgnorer(UUID uuid) {
		phaser.register();
		try {
			return delegate.removeIgnorer(uuid);
		} finally {
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public CompletableFuture<List<UUID>> getIgnorers() {
		phaser.register();
		try {
			return delegate.getIgnorers();
		} finally {
			phaser.arriveAndDeregister();
		}
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
