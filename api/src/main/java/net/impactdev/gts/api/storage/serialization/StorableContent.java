package net.impactdev.gts.api.storage.serialization;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Represents an object which can be serialized and deserialized via JSON. An object is expected to
 * have a version identifier that helps to identify changes in data structure. For instance,
 * in one version, a type of storable content could be using version 1 at initial release. Overtime,
 * that content might have experienced a shift in its data structure, warranting a version 2. We could
 * have two deserializers for this content, which handle the deserialization mechanics for either
 * version 1 or 2. As such, this would allow legacy version 1 data to be translated into the version 2
 * schema without issue.
 */
public interface StorableContent {

    /**
     * Specifies the version of the content being written. This is meant to be a key identifier
     * determining the characteristics of the written content, so that a matching deserializer
     * can control how to translate the content back to an object.
     *
     * @return The content version of the data to be written
     */
    int version();

    /**
     * Serializes the inheriting object into a {@link JsonObject JSON Object}. The written content
     * should specify the version specified with {@link #version()} for future deserialization methods
     * which might be subject to change.
     *
     * @return A JSON object representing this object
     */
    JsonObject serialize();

    /**
     * A deserializer provides a means for translating raw JSON into a compatible {@link StorableContent}
     * object.
     *
     * @param <T> The type of object this deserializer should create from the raw JSON data
     */
    @FunctionalInterface
    interface Deserializer<T extends StorableContent> {

        /**
         * Attempts to deserialize the given raw JSON data into the target object typing.
         * In the event this data is malformed, it is expected that a deserializer will
         * throw a {@link JsonParseException exception} indicating the issue.
         *
         * @param json The raw JSON data to deserialize
         * @return The target object populated with data based on the given JSON data
         * @throws JsonParseException If the JSON is malformed at time of translation
         */
        T deserialize(JsonObject json) throws JsonParseException;

    }

}
