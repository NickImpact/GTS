package net.impactdev.gts.placeholders;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.placeholder.PlaceholderParser;

public class PlaceholderMetadata {

    private final ResourceKey key;
    private final PlaceholderParser parser;

    public static PlaceholderMetadata of(ResourceKey key, PlaceholderParser parser) {
        return new PlaceholderMetadata(key, parser);
    }

    public PlaceholderMetadata(ResourceKey key, PlaceholderParser parser) {
        this.key = key;
        this.parser = parser;
    }

    public ResourceKey getKey() {
        return this.key;
    }

    public PlaceholderParser getParser() {
        return this.parser;
    }

}
