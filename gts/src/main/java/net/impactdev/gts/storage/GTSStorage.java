package net.impactdev.gts.storage;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import net.impactdev.gts.api.elements.listings.Listing;
import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.gts.storage.implementation.GTSStorageImplementation;
import net.impactdev.gts.util.future.Futures;
import net.impactdev.impactor.api.storage.Storage;
import net.impactdev.impactor.api.utility.printing.PrettyPrinter;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public final class GTSStorage implements Storage {

    private final GTSPlugin plugin;
    private final GTSStorageImplementation implementation;

    private final LoadingCache<UUID, ReentrantLock> locks = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(key -> new ReentrantLock());

    public GTSStorage(GTSPlugin plugin, GTSStorageImplementation implementation) {
        this.plugin = plugin;
        this.implementation = implementation;
    }

    @Override
    public void init() throws Exception {
        this.implementation.init();
    }

    @Override
    public void shutdown() throws Exception {
        this.implementation.shutdown();
    }

    public CompletableFuture<Void> meta(PrettyPrinter printer) {
        return Futures.timed(() -> this.implementation.meta(printer), 5, TimeUnit.SECONDS);
    }

    public CompletableFuture<List<Listing>> listings() {
        return Futures.timed(this.implementation::listings, 5, TimeUnit.SECONDS);
    }

    public CompletableFuture<Void> publishListing(Listing listing) {
        return Futures.timed(() -> this.implementation.publishListing(listing), 5, TimeUnit.SECONDS);
    }

}
