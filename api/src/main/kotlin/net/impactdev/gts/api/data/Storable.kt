package net.impactdev.gts.api.data

import com.google.gson.JsonObject
import net.impactdev.impactor.api.json.factory.JObject

/**
 * Represents an objects that can be translated to and from JSON representable data.
 */
interface Storable {
    val version: Int

    /**
     * Processes the request to serialize a GTS Entry into representable JSON data.
     *
     * @return A JObject that represents which represents the serialized components of the entry
     */
    fun serialize(): JObject?

    @FunctionalInterface
    interface Deserializer<T> {
        fun deserialize(`object`: JsonObject?): T
    }
}