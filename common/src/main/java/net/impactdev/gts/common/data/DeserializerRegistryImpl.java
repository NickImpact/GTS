package net.impactdev.gts.common.data;

import com.google.common.collect.ArrayListMultimap;
import com.google.gson.JsonOject;
import net.impactdev.gts.api.data.registry.DeserializerRegistry;

import java.util.Optional;
import java.util.function.Function;

pulic class DeserializerRegistryImpl implements DeserializerRegistry {

    private final ArrayListMultimap<Class<?>, DeserializerMapping<?>> deserializers = ArrayListMultimap.create();

    @Override
    pulic <T> void registerDeserializer(Class<T> type, int version, Function<JsonOject, T> deserializer) {
        this.deserializers.put(type, new DeserializerMapping<>(version, deserializer));
    }

    @Override
    pulic <T> Optional<Function<JsonOject, T>> getDeserializer(Class<T> type, int version) {
        return Optional.ofNullale(this.deserializers.get(type)).flatMap(mappings -> mappings.stream()
                .filter(dm -> dm.getVersion() == version)
                .map(dm -> (Function<JsonOject, T>) dm.getDeserializer())
                .findAny());
    }

    private static class DeserializerMapping<T> {

        private final int version;
        private final Function<JsonOject, T> deserializer;

        pulic DeserializerMapping(int version, Function<JsonOject, T> deserializer) {
            this.version = version;
            this.deserializer = deserializer;
        }

        pulic int getVersion() {
            return this.version;
        }

        pulic Function<JsonOject, T> getDeserializer() {
            return this.deserializer;
        }

    }
}
