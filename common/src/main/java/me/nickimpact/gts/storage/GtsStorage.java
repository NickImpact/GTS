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

import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.plugin.IGTSPlugin;
import me.nickimpact.gts.api.storage.IGtsStorage;
import me.nickimpact.gts.api.util.ThrowingRunnable;
import me.nickimpact.gts.storage.implementation.StorageImplementation;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class GtsStorage implements IGtsStorage {

    private final IGTSPlugin plugin;
    private final StorageImplementation implementation;

    public GtsStorage(IGTSPlugin plugin, StorageImplementation implementation) {
        this.plugin = plugin;
        this.implementation = implementation;
    }

    /**
     * Attempts to initialize the storage implementation
     */
    public void init() {
        try {
            this.implementation.init();
        } catch (Exception e) {
            // Log the failure
            e.printStackTrace();
        }
    }

    /**
     * Attempts to shutdown the storage implementation
     */
    public void shutdown() {
        try {
            this.implementation.shutdown();
        } catch (Exception e) {
            // Log the failure
            e.printStackTrace();
        }
    }

    /**
     * Represents any properties which might be set against a storage
     * implementation.
     *
     * @return A mapping of flags to values representing storage implementation
     * properties
     */
    public Map<String, String> getMeta() {
        return this.implementation.getMeta();
    }

    private <T> CompletableFuture<T> makeFuture(Callable<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.call();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new CompletionException(e);
            }
        });
    }

    private CompletableFuture<Void> makeFuture(ThrowingRunnable runnable) {
        return CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<Boolean> addListing(Listing listing) {
        return this.makeFuture(() -> this.implementation.addListing(listing));
    }

    public CompletableFuture<Boolean> deleteListing(UUID uuid) {
        return this.makeFuture(() -> this.implementation.deleteListing(uuid));
    }

    public CompletableFuture<List<Listing>> getListings() {
        return this.makeFuture(this.implementation::getListings);
    }

    public CompletableFuture<Boolean> addIgnorer(UUID uuid) {
        return this.makeFuture(() -> this.implementation.addIgnorer(uuid));
    }

    public CompletableFuture<Boolean> removeIgnorer(UUID uuid) {
        return this.makeFuture(() -> this.implementation.removeIgnorer(uuid));
    }

    public CompletableFuture<List<UUID>> getAllIgnorers() {
        return this.makeFuture(() -> this.implementation.getAllIgnorers());
    }

    public CompletableFuture<Boolean> purge() {
        return this.makeFuture(this.implementation::purge);
    }
}
