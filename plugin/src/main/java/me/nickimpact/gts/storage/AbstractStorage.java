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

package me.nickimpact.gts.storage;

import com.google.common.base.Throwables;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.utils.MessageUtils;
import me.nickimpact.gts.storage.dao.AbstractDao;
import me.nickimpact.gts.storage.wrappings.PhasedStorage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

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
	public CompletableFuture<Void> updateListing(Listing listing) {
		return makeFuture(() -> dao.updateListing(listing));
	}

	@Override
	public CompletableFuture<Void> removeListing(UUID uuid) {
		return makeFuture(() -> dao.removeListing(uuid));
	}

	@Override
	public CompletableFuture<List<Listing>> getListings() {
		return makeFuture(dao::getListings);
	}

	@Override
	public CompletableFuture<Void> addIgnorer(UUID uuid) {
		return makeFuture(() -> dao.addIgnorer(uuid));
	}

	@Override
	public CompletableFuture<Void> removeIgnorer(UUID uuid) {
		return makeFuture(() -> dao.removeIgnorer(uuid));
	}

	@Override
	public CompletableFuture<List<UUID>> getIgnorers() {
		return makeFuture(dao::getIgnorers);
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
