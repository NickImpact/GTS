package me.nickimpact.gts.util;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nickimpact.impactor.api.json.factory.JArray;
import com.nickimpact.impactor.api.json.factory.JObject;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataFormats;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DataViewJsonManager {

    public static void writeDataViewToJSON(JObject writer, DataView container) {
        for(Map.Entry<DataQuery, Object> entry : container.getValues(false).entrySet()) {
            DataQuery query = entry.getKey();
            Object value = entry.getValue();

            write(writer, query.asString('.'), value);
        }
    }

    public static DataView readDataViewFromJSON(JsonObject input) {
        try {
            return DataFormats.JSON.read(input.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeDataViewToJSON(JArray writer, DataView container) {
        JObject result = new JObject();

        for(Map.Entry<DataQuery, Object> entry : container.getValues(false).entrySet()) {
            DataQuery query = entry.getKey();
            Object value = entry.getValue();

            write(result, query.asString('.'), value);
        }

        writer.add(result);
    }

    private static void write(JObject writer, String key, Object value) {
        if(value instanceof String) {
            writer.add(key, (String) value);
        } else if(value instanceof Number) {
            writer.add(key, (Number) value);
        } else if(value instanceof Boolean) {
            writer.add(key, (Boolean) value);
        } else if(value instanceof Iterable) {
            writer.add(key, writeArray((Iterable<?>) value));
        } else if(value instanceof Map) {
            writer.add(key, writeMap((Map<?, ?>) value));
        } else if(value instanceof DataSerializable) {
            writeDataViewToJSON(writer, ((DataSerializable) value).toContainer());
        } else if(value instanceof DataView) {
            writeDataViewToJSON(writer, (DataView) value);
        } else {
            throw new IllegalArgumentException("Unable to translate object to JSON: " + value);
        }
    }

    private static void write(JArray writer, Object value) {
        if(value instanceof String) {
            writer.add((String) value);
        } else if(value instanceof Number) {
            writer.add((Number) value);
        } else if(value instanceof Boolean) {
            //writer.add((Boolean) value);
        } else if(value instanceof Iterable) {
            writer.add(writeArray((Iterable<?>) value));
        } else if(value instanceof Map) {
            writer.add(writeMap((Map<?, ?>) value));
        } else if(value instanceof DataSerializable) {
            writeDataViewToJSON(writer, ((DataSerializable) value).toContainer());
        } else if(value instanceof DataView) {
            writeDataViewToJSON(writer, (DataView) value);
        } else {
            throw new IllegalArgumentException("Unable to translate object to JSON: " + value);
        }
    }

    private static JArray writeArray(Iterable<?> iterable) {
        JArray array = new JArray();
        for(Object value : iterable) {
            write(array, value);
        }

        return array;
    }

    private static JObject writeMap(Map<?, ?> map) {
        JObject mapped = new JObject();
        for(Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            if(key instanceof DataQuery) {
                key = ((DataQuery) key).asString('.');
            }

            write(mapped, key.toString(), entry.getValue());
        }

        return mapped;
    }

    private static List<?> readArray(JsonArray array) {
        List<Object> results = Lists.newArrayList();
        for(int i = 0; i < array.size(); i++) {
            results.add(array.get(i));
        }

        return results;
    }
}
