package net.impactdev.gts.api.data.registry;

import com.google.gson.JsonObject;

import java.util.Optional;
import java.util.function.Function;

public interface DeserializerRegistry {

    <T> void registerDeserializer(Class<T> type, int version, Function<JsonObject, T> deserializer);

    <T> Optional<Function<JsonObject, T>> getDeserializer(Class<T> type, int version);

}
