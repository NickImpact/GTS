package net.impactdev.gts.api.elements.listings.deserialization;

import net.impactdev.gts.api.elements.content.Content;

import java.time.LocalDateTime;
import java.util.UUID;

public final class DeserializationKeys {

    public static final DeserializationKey<UUID> UUID = create("uuid");
    public static final DeserializationKey<UUID> LISTER = create("lister");
    public static final DeserializationKey<Integer> VERSION = create("version");
    public static final DeserializationKey<LocalDateTime> PUBLISHED_TIME = create("published");
    public static final DeserializationKey<LocalDateTime> EXPIRATION_TIME = create("expiration");
    public static final DeserializationKey<Content<?>> CONTENT = create("content");

    public static <T> DeserializationKey<T> create(String key) {
        return new DeserializationKey<>(key);
    }


}
