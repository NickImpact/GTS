package net.impactdev.gts.extensions;

import net.impactdev.gts.api.extensions.Extension;
import net.impactdev.gts.api.extensions.ExtensionManager;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.nio.file.Path;
import java.util.Collection;

public class GTSExtensionManager implements ExtensionManager {

    @Override
    public void construct(Extension extension) {

    }

    @Override
    public void construct(Path path) throws Exception {

    }

    @Override
    public void enable() {
        this.extensions().forEach(Extension::enable);
    }

    @Override
    public void shutdown() {
        this.extensions().forEach(Extension::shutdown);
    }

    @Override
    public @NonNull Collection<Extension> extensions() {
        return null;
    }

}
