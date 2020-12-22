package net.impactdev.gts.common.data;

import com.google.common.collect.ArrayListMultimap;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.data.registry.DeserializerRegistry;

import java.util.Optional;
import java.util.function.Function;

public class DeserializerRegistryImpl implements DeserializerRegistry {

    private final ArrayListMultimap<Class<?>, DeserializerMapping<?>> deserializers = ArrayListMultimap.create();

    @Override
    public <T> void registerDeserializer(Class<T> type, int version, Function<JsonObject, T> deserializer) {
        this.deserializers.put(type, new DeserializerMapping<>(version, deserializer));
    }

    @Override
    public <T> Optional<Function<JsonObject, T>> getDeserializer(Class<T> type, int version) {
        return Optional.ofNullable(this.deserializers.get(type)).flatMap(mappings -> mappings.stream()
                .filter(dm -> dm.getVersion() == version)
                .map(dm -> (Function<JsonObject, T>) dm.getDeserializer())
                .findAny());
    }

    private static class DeserializerMapping<T> {

        private final int version;
        private final Function<JsonObject, T> deserializer;

        public DeserializerMapping(int version, Function<JsonObject, T> deserializer) {
            this.version = version;
            this.deserializer = deserializer;
        }

        public int getVersion() {
            return this.version;
        }

        public Function<JsonObject, T> getDeserializer() {
            return this.deserializer;
        }

    }
}
