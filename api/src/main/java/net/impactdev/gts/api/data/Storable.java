package net.impactdev.gts.api.data;

import com.google.gson.JsonObject;
import net.impactdev.impactor.api.json.factory.JObject;

/**
 * Represents an objects that can be translated to and from JSON representable data.
 */
public interface Storable {

    int getVersion();

    /**
     * Processes the request to serialize a GTS Entry into representable JSON data.
     *
     * @return A JObject that represents which represents the serialized components of the entry
     */
    JObject serialize();

    @FunctionalInterface
    interface Deserializer<T> {

        T deserialize(JsonObject object);

    }

}
