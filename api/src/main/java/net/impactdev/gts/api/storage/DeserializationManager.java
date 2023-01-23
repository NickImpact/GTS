package net.impactdev.gts.api.storage;

import java.util.Optional;

public interface DeserializationManager {

    /**
     * Attempts to locate a deserializer responsible for translating an object into the given typing
     * with the specific schema ID. Should one not exist, this will return an empty optional.
     *
     * @param type The type of object our deserializer should be translating JSON into
     * @param version The schema version ID for the deserializer
     * @return A matching deserializer under the typing and version schema identifiers
     * @param <T> The type of object we should be translating into
     */
    <T extends StorableContent> Optional<StorableContent.Deserializer<T>> deserializer(Class<T> type, int version);

    /**
     * Attempts to register a deserializer under the given typing and version. If a deserializer is already
     * registered under the criteria of the type and version, then this call will reject it in favor of the
     * first to further indicate to the end user that they likely have a copy/paste issue on their side. It's
     * very unlikely to have two different deserializers attempt to be registered under the same schema parameters.
     *
     * @param type The type of object for the deserializer
     * @param version The schema version ID of the content being deserialized
     * @param deserializer The actual deserializer responsible for translating the JSON data
     * @return <code>true</code> if and only if no deserializer under the type and version schema is already
     *         available, otherwise <code>false</code> to indicate the register rejection.
     * @param <T> The type of object a deserializer will attempt to create
     */
    <T extends StorableContent> boolean register(Class<T> type, int version, StorableContent.Deserializer<T> deserializer);
}
