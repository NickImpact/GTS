package net.impactdev.gts.storage.implementation.file;

import net.impactdev.gts.api.elements.listings.Listing;
import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.gts.storage.implementation.GTSStorageImplementation;
import net.impactdev.impactor.api.storage.connection.configurate.ConfigurateLoader;
import net.impactdev.impactor.api.utility.printing.PrettyPrinter;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public final class ConfigurateStorage implements GTSStorageImplementation {

    private final GTSPlugin plugin;
    private final ConfigurateLoader loader;
    private final Path root;

    public ConfigurateStorage(GTSPlugin plugin, ConfigurateLoader loader, Path root) {
        this.plugin = plugin;
        this.loader = loader;
        this.root = root;
    }

    @Override
    public PrettyPrinter.IPrettyPrintable meta() {
        return null;
    }

    @Override
    public List<Listing> listings() throws Exception {
        return null;
    }

    @Override
    public void publishListing(Listing listing) throws Exception {

    }

    @Override
    public void deleteListing(UUID uuid) throws Exception {

    }
}
