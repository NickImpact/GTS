package net.impactdev.gts.common.storage.translators;

import com.google.common.collect.Lists;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.storage.GTSStorage;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.storage.StorageFactory;
import net.impactdev.gts.common.utils.future.CompletableFutureManager;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.dependencies.DependencyManager;
import net.impactdev.impactor.api.storage.StorageType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StorageTranslator implements PrettyPrinter.IPrettyPrintable {

    private final StorageType to;

    public StorageTranslator(StorageType to) {
        this.to = to;
    }

    public StorageType to() {
        return this.to;
    }

    public CompletableFuture<Boolean> run() {
        return CompletableFutureManager.makeFuture(() -> {
            // TODO - Lock the database from further communication to prevent lost data on transfer
            // TODO - due to race conditions

            List<Listing> listings = GTSPlugin.getInstance().getStorage().fetchListings().join();
            // TODO - Options to query for other storable information

            DependencyManager dependencies = Impactor.getInstance().getRegistry().get(DependencyManager.class);
            dependencies.loadStorageDependencies(Lists.newArrayList(this.to()));
            GTSStorage replacement = new StorageFactory(GTSPlugin.getInstance()).getInstance(this.to());
            listings.forEach(listing -> replacement.publishListing(listing).join());

            // TODO - Replace active storage type and unlock database
            // TODO - Change messaging service if current selection is not compatible with new storage type
            // TODO - Update configuration so that the selected storage is now the bootable storage
            return true;
        });
    }

    @Override
    public void print(PrettyPrinter printer) {

    }
}
