package net.impactdev.gts.util;

import com.google.gson.JsonObject;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.impactor.api.json.factory.JArray;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.impactor.api.utilities.printing.PrettyPrinter;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataView;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class DataViewJsonManager {

    private static final String DEBUG_OUT = "%s: %s";

    public static void writeDataViewToJSON(JObject writer, DataView container) {
        PrettyPrinter printer = new PrettyPrinter(80);
        printer.add("JSON Deserialization Attempt").center();
        printer.hr();

        writeDataViewToJSON(writer, container, printer, 0);
    }

    private static void writeDataViewToJSON(JObject writer, DataView container, PrettyPrinter printer, int indent) {
        for(Map.Entry<DataQuery, Object> entry : container.values(false).entrySet()) {
            DataQuery query = entry.getKey();
            Object value = entry.getValue();

            write(writer, query.asString('.'), value, printer, indent);
        }
    }

    public static DataView readDataViewFromJSON(JsonObject input) {
        try {
            return DataFormats.JSON.get().read(input.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeDataViewToJSON(JArray writer, DataView container, PrettyPrinter printer, int indent) {
        JObject result = new JObject();

        for(Map.Entry<DataQuery, Object> entry : container.values(false).entrySet()) {
            DataQuery query = entry.getKey();
            Object value = entry.getValue();

            write(result, query.asString('.'), value, printer, indent);
        }

        writer.add(result);
    }

    private static void write(JObject writer, String key, Object value, PrettyPrinter printer, int indent) {
        printer.add(translate(key, value), indent);

        if(value instanceof String) {
            writer.add(key, (String) value);
        } else if(value instanceof Number) {
            writer.add(key, (Number) value);
        } else if(value instanceof Boolean) {
            writer.add(key, (Boolean) value);
        } else if(value instanceof Iterable) {
            writer.add(key, writeArray((Iterable<?>) value, printer, indent + 2));
        } else if(value instanceof Map) {
            writer.add(key, writeMap((Map<?, ?>) value, printer, indent + 2));
        } else if(value instanceof DataSerializable) {
            writeDataViewToJSON(writer, ((DataSerializable) value).toContainer(), printer, indent + 2);
        } else if(value instanceof DataView) {
            writeDataViewToJSON(writer, (DataView) value, printer, indent + 2);
        } else {
            IllegalArgumentException exception = new IllegalArgumentException("Unable to translate object to JSON: " + value);
            printer.hr();
            printer.add("Exception During Write").center();
            printer.hr();
            printer.add("Failed Data");
            printer.add("Class: " + value.getClass().getName());
            printer.add("Value: " + (value.getClass().isArray() ? printArray(value) : value.toString()));
            printer.hr();

            printer.add(exception);

            printer.log(GTSPlugin.instance().logger(), PrettyPrinter.Level.DEBUG);
            throw exception;
        }
    }

    private static void write(JArray writer, Object value, PrettyPrinter printer, int indent) {
        if(value instanceof String) {
            writer.add((String) value);
        } else if(value instanceof Number) {
            writer.add((Number) value);
        } else if(value instanceof Boolean) {
            //writer.add((Boolean) value);
        } else if(value instanceof Iterable) {
            writer.add(writeArray((Iterable<?>) value, printer, indent + 2));
        } else if(value instanceof Map) {
            writer.add(writeMap((Map<?, ?>) value, printer, indent + 2));
        } else if(value instanceof DataSerializable) {
            writeDataViewToJSON(writer, ((DataSerializable) value).toContainer(), printer, indent + 2);
        } else if(value instanceof DataView) {
            writeDataViewToJSON(writer, (DataView) value, printer, indent + 2);
        } else {
            throw new IllegalArgumentException("Unable to translate object to JSON: " + value);
        }
    }

    private static JArray writeArray(Iterable<?> iterable, PrettyPrinter printer, int indent) {
        JArray array = new JArray();
        for(Object value : iterable) {
            write(array, value, printer, indent);
        }

        return array;
    }

    private static JObject writeMap(Map<?, ?> map, PrettyPrinter printer, int indent) {
        JObject mapped = new JObject();
        for(Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            if(key instanceof DataQuery) {
                key = ((DataQuery) key).asString('.');
            }

            write(mapped, key.toString(), entry.getValue(), printer, indent + 2);
        }

        return mapped;
    }

    private static String translate(String key, Object value) {
        return String.format(DEBUG_OUT, key, value);
    }

    private static String printArray(Object array) {
        if(!array.getClass().isArray()) {
            return "Not an Array";
        }

        return "[" + IntStream.range(0, Array.getLength(array))
                .mapToObj(i -> Objects.toString(Array.get(array, i)))
                .reduce("", (l, r) -> l + ", " + r)
                .substring(2)
                + "]";
    }
}
