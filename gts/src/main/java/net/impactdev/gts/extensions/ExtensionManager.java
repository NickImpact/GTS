package net.impactdev.gts.extensions;

import com.google.common.collect.Lists;
import net.impactdev.gts.api.extensions.Extension;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.nio.file.Path;
import java.util.Collection;

public class ExtensionManager {

    public void construct(Extension extension) {

    }

    public void construct(Path path) throws Exception {

    }

    public void enable() {
        this.extensions().forEach(Extension::enable);
    }

    public void shutdown() {
        this.extensions().forEach(Extension::shutdown);
    }

    public @NonNull Collection<Extension> extensions() {
        return Lists.newArrayList();
    }

}
