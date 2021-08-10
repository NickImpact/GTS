package net.impactdev.gts.test.configurate;

import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.storage.implementation.file.ConfigurateStorage;
import net.impactdev.gts.common.storage.implementation.file.loaders.ConfigurateLoader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

public class TestableConfigurateStorage extends ConfigurateStorage {

    public TestableConfigurateStorage(GTSPlugin plugin, String implementationName, ConfigurateLoader loader, String extension, String dataDirName) {
        super(plugin, implementationName, loader, extension, dataDirName);

        if(this.getResourcePath().toFile().exists()) {
            try(Stream<Path> walker = Files.walk(this.getResourcePath())) {
                walker.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (Exception e) {
                throw new RuntimeException("Failure during cleanup file walk", e);
            }
        }
    }

    @Override
    protected Path getResourcePath() {
        return Paths.get("src/test/configurate");
    }
}
