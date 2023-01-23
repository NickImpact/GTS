package net.impactdev.gts.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public final class JsonUtilities {

    public static final Gson SIMPLE = new GsonBuilder().create();
    public static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().create();

    @NotNull
    public static <T> T require(JsonObject parent, String id, Function<JsonElement, T> translator) {
        T result = optional(parent, id, translator);
        if(result == null) {
            throw new IllegalArgumentException("Json does not hold key: " + id);
        }

        return result;
    }

    @Nullable
    public static <T> T optional(JsonObject parent, String id, Function<JsonElement, T> translator) {
        JsonElement element = parent.get(id);
        if(element != null) {
            return translator.apply(element);
        }

        return null;
    }

}
