package net.impactdev.gts.common.data

import com.google.common.collect.ArrayListMultimap
import com.google.gson.JsonObject
import net.impactdev.gts.api.data.registry.DeserializerRegistry
import java.util.*
import java.util.function.Function

class DeserializerRegistryImpl : DeserializerRegistry {
    private val deserializers = ArrayListMultimap.create<Class<*>?, DeserializerMapping<*>>()
    override fun <T> registerDeserializer(type: Class<T>?, version: Int, deserializer: Function<JsonObject?, T>?) {
        deserializers.put(type, DeserializerMapping(version, deserializer))
    }

    override fun <T> getDeserializer(type: Class<T>?, version: Int): Optional<Function<JsonObject?, T>?>? {
        return Optional.ofNullable(deserializers[type])
            .flatMap(Function<List<DeserializerMapping<*>>, Optional<Function<JsonObject, T>>> { mappings: List<DeserializerMapping<*>> ->
                mappings.stream()
                    .filter { dm: DeserializerMapping<*> -> dm.version == version }
                    .map { dm: DeserializerMapping<*> -> dm.deserializer as Function<JsonObject?, T?>? }
                    .findAny()
            })
    }

    private class DeserializerMapping<T>(val version: Int, val deserializer: Function<JsonObject?, T>?)
}