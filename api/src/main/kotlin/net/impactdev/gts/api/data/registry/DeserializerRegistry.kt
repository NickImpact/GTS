package net.impactdev.gts.api.data.registry

import com.google.gson.JsonObject
import java.util.*
import java.util.function.Function

interface DeserializerRegistry {
    fun <T> registerDeserializer(type: Class<T>?, version: Int, deserializer: Function<JsonObject?, T>?)
    fun <T> getDeserializer(type: Class<T>?, version: Int): Optional<Function<JsonObject?, T>?>?
}