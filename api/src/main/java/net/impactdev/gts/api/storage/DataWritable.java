package net.impactdev.gts.api.storage;

import com.google.gson.JsonObject;

public interface DataWritable {

    JsonObject serialize();

    int version();

    @FunctionalInterface
    interface Deserializer<T> {

        T deserialize(JsonObject json);

    }

}
