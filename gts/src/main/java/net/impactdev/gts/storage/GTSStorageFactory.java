package net.impactdev.gts.storage;

import net.impactdev.gts.configuration.GTSConfigKeys;
import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.gts.storage.implementation.GTSStorageImplementation;
import net.impactdev.gts.storage.implementation.file.ConfigurateStorage;
import net.impactdev.impactor.api.storage.StorageType;
import net.impactdev.impactor.api.storage.connection.configurate.loaders.JsonLoader;

import java.nio.file.Paths;

public final class GTSStorageFactory {

    public static GTSStorage create(StorageType fallback) throws Exception {
        GTSPlugin plugin = GTSPlugin.instance();

        StorageType method = plugin.configuration().get(GTSConfigKeys.STORAGE_METHOD);
        if(method == null) {
            method = fallback;
        }

        plugin.logger().info("Loading storage method... [" + method.getName() + "]");
        GTSStorage storage = new GTSStorage(plugin, createNewImplementation(plugin, method));
        storage.init();
        return storage;
    }

    private static GTSStorageImplementation createNewImplementation(GTSPlugin plugin, StorageType method) {
        switch (method) {
            case JSON:
                return new ConfigurateStorage(
                        plugin,
                        new JsonLoader(),
                        GTSPlugin.instance().bootstrapper().dataDir().resolve("storage").resolve("json")
                );
        }

        throw new UnsupportedOperationException("Non-supported storage method: " + method);
    }
}
