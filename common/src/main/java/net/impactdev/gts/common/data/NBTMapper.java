package net.impactdev.gts.common.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.impactor.api.json.factory.JArray;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.impactor.api.utilities.printing.PrettyPrinter;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.nbt.ShortNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.util.Constants;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class NBTMapper {

    public static JObject from(CompoundNBT nbt) {
        return from(nbt, true);
    }

    public static JObject from(CompoundNBT nbt, boolean print) {
        JObject result = new JObject();

        PrettyPrinter test = new PrettyPrinter(80);
        test.add("NBT Mapping Track - Write").center();
        test.hr();

        for(String key : nbt.getAllKeys()) {
            int id = nbt.get(key).getId();
            test.kv(key, id);

            switch (id) {
                case Constants.NBT.TAG_BYTE:
                    append(result, key, nbt.getByte(key));
                    break;
                case Constants.NBT.TAG_SHORT:
                    append(result, key, nbt.getShort(key));
                    break;
                case Constants.NBT.TAG_INT:
                    append(result, key, nbt.getInt(key));
                    break;
                case Constants.NBT.TAG_LONG:
                    append(result, key, nbt.getLong(key));
                    break;
                case Constants.NBT.TAG_FLOAT:
                    append(result, key, nbt.getFloat(key));
                    break;
                case Constants.NBT.TAG_DOUBLE:
                    append(result, key, nbt.getDouble(key));
                    break;
                case Constants.NBT.TAG_BYTE_ARRAY:
                    append(result, key, nbt.getByteArray(key));
                    break;
                case Constants.NBT.TAG_STRING:
                    append(result, key, nbt.getString(key));
                    break;
                case Constants.NBT.TAG_LIST:
                    INBT x = nbt.get(key);
                    if(nbt.getTagType(key) == 9) {
                        ListNBT translated = (ListNBT) x;
                        append(result, key, translated);
                    }
                    break;
                case Constants.NBT.TAG_COMPOUND:
                    append(result, key, nbt.getCompound(key));
                    break;
                case Constants.NBT.TAG_INT_ARRAY:
                    append(result, key, nbt.getIntArray(key));
                    break;
                case Constants.NBT.TAG_LONG_ARRAY:
                    append(result, key, nbt.getLongArray(key));
                    break;
            }
        }

        if(print) {
            test.newline();
            test.add("Result:");
            test.add(result.toJson());

            test.log(GTSPlugin.instance().logger(), PrettyPrinter.Level.DEBUG, "NBT Mapping");
        }
        return result;
    }

    private static void append(JObject json, String key, byte value) {
        getLowestParent(json, key).add(key, Mapper.BYTE.getApplier().apply(value));
    }

    private static void append(JObject json, String key, short value) {
        getLowestParent(json, key).add(key, Mapper.SHORT.getApplier().apply(value));
    }

    private static void append(JObject json, String key, int value) {
        getLowestParent(json, key).add(key, Mapper.INTEGER.getApplier().apply(value));
    }

    private static void append(JObject json, String key, long value) {
        getLowestParent(json, key).add(key, Mapper.LONG.getApplier().apply(value));
    }

    private static void append(JObject json, String key, float value) {
        getLowestParent(json, key).add(key, Mapper.FLOAT.getApplier().apply(value));
    }

    private static void append(JObject json, String key, double value) {
        getLowestParent(json, key).add(key, Mapper.DOUBLE.getApplier().apply(value));
    }

    private static void append(JObject json, String key, byte[] value) {
        getLowestParent(json, key).add(key, Mapper.BYTE_ARRAY.getApplier().apply(value));
    }

    private static void append(JObject json, String key, String value) {
        getLowestParent(json, key).add(key, Mapper.STRING.getApplier().apply(value));
    }

    private static void append(JObject json, String key, ListNBT value) {
        getLowestParent(json, key).add(key, Mapper.LIST.getApplier().apply(value));
    }

    private static void append(JObject json, String key, CompoundNBT value) {
        getLowestParent(json, key).add(key, Mapper.COMPOUND.getApplier().apply(value));
    }

    private static void append(JObject json, String key, int[] value) {
        getLowestParent(json, key).add(key, Mapper.INT_ARRAY.getApplier().apply(value));
    }

    private static void append(JObject json, String key, long[] value) {
        getLowestParent(json, key).add(key, Mapper.INT_ARRAY.getApplier().apply(value));
    }

    public static CompoundNBT read(JsonObject json) {
        CompoundNBT nbt = new CompoundNBT();
        for(Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            JsonObject data = entry.getValue().getAsJsonObject();

            String type = data.get("type").getAsString();
            JsonElement value = data.get("value");

            if(type.equals("compound")) {
                nbt.put(key, read(value.getAsJsonObject()));
            } else if(type.equals("list")) {
                nbt.put(key, read$list(value.getAsJsonArray()));
            } else {
                switch (type) {
                    case "byte":
                        nbt.putByte(key, value.getAsByte());
                        break;
                    case "short":
                        nbt.putShort(key, value.getAsShort());
                        break;
                    case "int":
                        nbt.putInt(key, value.getAsInt());
                        break;
                    case "long":
                        nbt.putLong(key, value.getAsLong());
                        break;
                    case "float":
                        nbt.putFloat(key, value.getAsFloat());
                        break;
                    case "double":
                        nbt.putDouble(key, value.getAsDouble());
                        break;
                    case "byte[]":
                        JsonArray array = value.getAsJsonArray();
                        byte[] values = new byte[array.size()];

                        int index = 0;
                        for(JsonElement element : array) {
                            values[index++] = element.getAsByte();
                        }
                        nbt.putByteArray(key, values);
                        break;
                    case "string":
                        nbt.putString(key, value.getAsString());
                        break;
                    case "int[]":
                        JsonArray array2 = value.getAsJsonArray();
                        int[] values2 = new int[array2.size()];

                        int i2 = 0;
                        for(JsonElement element : array2) {
                            values2[i2++] = element.getAsInt();
                        }
                        nbt.putIntArray(key, values2);
                        break;
                    case "long[]":
                        JsonArray array3 = value.getAsJsonArray();
                        long[] values3 = new long[array3.size()];

                        int i3 = 0;
                        for(JsonElement element : array3) {
                            values3[i3++] = element.getAsLong();
                        }
                        nbt.putLongArray(key, values3);
                        break;
                }
            }
        }

        return nbt;
    }

    private static ListNBT read$list(JsonArray array) {
        ListNBT result = new ListNBT();
        for(JsonElement element : array) {
            JsonObject object = element.getAsJsonObject();

            String type = object.get("type").getAsString();
            JsonElement value = object.get("value");

            if(type.equals("compound")) {
                result.add(read(value.getAsJsonObject()));
            } else if(type.equals("list")) {
                result.add(read$list(value.getAsJsonArray()));
            } else {
                switch (type) {
                    case "byte":
                        result.add(ByteNBT.valueOf(value.getAsByte()));
                        break;
                    case "short":
                        result.add(ShortNBT.valueOf(value.getAsShort()));
                        break;
                    case "int":
                        result.add(IntNBT.valueOf(value.getAsInt()));
                        break;
                    case "long":
                        result.add(LongNBT.valueOf(value.getAsLong()));
                        break;
                    case "float":
                        result.add(FloatNBT.valueOf(value.getAsFloat()));
                        break;
                    case "double":
                        result.add(DoubleNBT.valueOf(value.getAsDouble()));
                        break;
                    case "byte[]":
                        JsonArray a = value.getAsJsonArray();
                        byte[] values = new byte[a.size()];

                        int index = 0;
                        for(JsonElement e : a) {
                            values[index++] = e.getAsByte();
                        }
                        result.add(new ByteArrayNBT(values));
                        break;
                    case "string":
                        result.add(StringNBT.valueOf(value.getAsString()));
                        break;
                    case "int[]":
                        JsonArray array2 = value.getAsJsonArray();
                        int[] values2 = new int[array2.size()];

                        int i2 = 0;
                        for(JsonElement e : array2) {
                            values2[i2++] = e.getAsInt();
                        }
                        result.add(new IntArrayNBT(values2));
                        break;
                    case "long[]":
                        JsonArray array3 = value.getAsJsonArray();
                        long[] values3 = new long[array3.size()];

                        int i3 = 0;
                        for(JsonElement e : array3) {
                            values3[i3++] = e.getAsLong();
                        }
                        result.add(new LongArrayNBT(values3));
                        break;
                }
            }
        }

        return result;
    }

    private static JsonObject getLowestParent(JObject json, String key) {
        JsonObject result = json.toJson();
        String[] query = key.split("\\.");

        int index = 0;
        for(; index < query.length - 2; index++) {
            if(result.isJsonObject()) {
                JsonObject object = result.getAsJsonObject();
                if(object.has(query[index])) {
                    result = Optional.ofNullable(object.get(query[index]))
                            .filter(JsonElement::isJsonObject)
                            .map(JsonElement::getAsJsonObject)
                            .orElseThrow(() -> new RuntimeException("Unable to find valid parent for key: " + key));
                } else {
                    object.add(query[index], result = new JsonObject());
                }
            }
        }

        return result;
    }

    private enum Mapper {
        BYTE(value -> new JObject().add("type", "byte").add("value", verify(Byte.class, value))),
        SHORT(value -> new JObject().add("type", "short").add("value", verify(Short.class, value))),
        INTEGER(value -> new JObject().add("type", "int").add("value", verify(Integer.class, value))),
        LONG(value -> new JObject().add("type", "long").add("value", verify(Long.class, value))),
        FLOAT(value -> new JObject().add("type", "float").add("value", verify(Float.class, value))),
        DOUBLE(value -> new JObject().add("type", "double").add("value", verify(Double.class, value))),
        BYTE_ARRAY(value -> new JObject().add("type", "byte[]").add("value", byteArray((byte[]) value))),
        STRING(value -> new JObject().add("type", "string").add("value", verify(String.class, value))),
        LIST(value -> {
            JArray array = new JArray();

            ListNBT list = (ListNBT) value;
            byte listType = list.getElementType();
            int count = list.size();
            for (int i = 0; i < count; i++) {
                array.add(fromINBT(list.get(i), listType));
            }

            return new JObject().add("type", "list").add("value", array);
        }),
        COMPOUND(value -> new JObject().add("type", "compound").add("value", NBTMapper.from((CompoundNBT) value, false))),
        INT_ARRAY(value -> new JObject().add("type", "int[]").add("value", intArray((int[]) value))),
        LONG_ARRAY(value -> new JObject().add("type", "long[]").add("value", longArray((long[]) value)))
        ;

        private final Function<Object, JObject> applier;

        Mapper(Function<Object, JObject> applier) {
            this.applier = applier;
        }

        public Function<Object, JsonObject> getApplier() {
            return this.applier.andThen(JObject::toJson);
        }

        private static <T> T verify(Class<T> expected, Object value) throws RuntimeException {
            return Optional.ofNullable(value)
                    .filter(x -> expected.isAssignableFrom(x.getClass()))
                    .map(expected::cast)
                    .orElseThrow(() -> new RuntimeException(String.format("Invalid Typing (%s vs %s)", expected.getName(), value.getClass().getName())));
        }

        private static JArray byteArray(byte[] value) throws RuntimeException {
            JArray array = new JArray();
            for(byte b : value) {
                array.add(b);
            }

            return array;
        }

        private static JArray intArray(int[] value) throws RuntimeException {
            JArray array = new JArray();
            for(int i : value) {
                array.add(i);
            }

            return array;
        }

        private static JArray longArray(long[] value) throws RuntimeException {
            JArray array = new JArray();
            for(long i : value) {
                array.add(i);
            }

            return array;
        }

        private static JObject fromINBT(INBT base, byte type) {
            switch (type) {
                case Constants.NBT.TAG_BYTE:
                    return BYTE.applier.apply(((ByteNBT) base).getAsByte());
                case Constants.NBT.TAG_SHORT:
                    return SHORT.applier.apply(((ShortNBT) base).getAsShort());
                case Constants.NBT.TAG_INT:
                    return INTEGER.applier.apply(((IntNBT) base).getAsInt());
                case Constants.NBT.TAG_LONG:
                    return LONG.applier.apply(((LongNBT) base).getAsLong());
                case Constants.NBT.TAG_FLOAT:
                    return FLOAT.applier.apply(((FloatNBT) base).getAsFloat());
                case Constants.NBT.TAG_DOUBLE:
                    return DOUBLE.applier.apply(((DoubleNBT) base).getAsDouble());
                case Constants.NBT.TAG_BYTE_ARRAY:
                    return BYTE_ARRAY.applier.apply(((ByteArrayNBT) base).getAsByteArray());
                case Constants.NBT.TAG_STRING:
                    return STRING.applier.apply(base.getAsString());
                case Constants.NBT.TAG_LIST:
                    return LIST.applier.apply(base);
                case Constants.NBT.TAG_COMPOUND:
                    return COMPOUND.applier.apply(base);
                case Constants.NBT.TAG_INT_ARRAY:
                    return INT_ARRAY.applier.apply(((IntArrayNBT) base).getAsIntArray());
                case Constants.NBT.TAG_LONG_ARRAY:
                    return LONG_ARRAY.applier.apply(((LongArrayNBT) base).getAsLongArray());
                default :
                    return null;
            }
        }

    }
}
