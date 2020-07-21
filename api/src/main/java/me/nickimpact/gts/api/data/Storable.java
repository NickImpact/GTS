package me.nickimpact.gts.api.data;

import com.google.gson.JsonObject;
import com.nickimpact.impactor.api.json.factory.JObject;

/**
 * Represents an objects that can be translated to and from JSON representable data.
 */
public interface Storable<T> {

    int getVersion();

    /**
     * Processes the request to serialize a GTS Entry into representable JSON data.
     *
     * @return A JObject that represents which represents the serialized components of the entry
     */
    JObject serialize(T content);

    /**
     * Responsible for deserializing the json data passed in back to the target
     * entry type.
     *
     * @return The representative Entry that is modeled by this json data
     */
    T deserialize(JsonObject json);

}
