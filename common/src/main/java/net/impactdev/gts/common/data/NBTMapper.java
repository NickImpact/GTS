package net.impactdev.gts.common.data;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.impactor.api.json.factory.JArray;
import net.impactdev.impactor.api.json.factory.JObject;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class NBTMapper {

    private final JObject result = new JObject();

    public JObject getResult() {
        return this.result;
    }

    public NBTMapper from(NBTTagCompound nbt) {
        return this.from(nbt, true);
    }

    public NBTMapper from(NBTTagCompound nbt, boolean print) {
        PrettyPrinter test = new PrettyPrinter(80);
        if(print) {
            test.add("NBT Mapping Track - Write").center();
            test.hr();
        }

        for(String key : nbt.getKeySet()) {
            int id = nbt.getTagId(key);
            test.kv(key, id);

            switch (id) {
                case Constants.NBT.TAG_BYTE:
                    this.append(key, nbt.getByte(key));
                    break;
                case Constants.NBT.TAG_SHORT:
                    this.append(key, nbt.getShort(key));
                    break;
                case Constants.NBT.TAG_INT:
                    this.append(key, nbt.getInteger(key));
                    break;
                case Constants.NBT.TAG_LONG:
                    this.append(key, nbt.getLong(key));
                    break;
                case Constants.NBT.TAG_FLOAT:
                    this.append(key, nbt.getFloat(key));
                    break;
                case Constants.NBT.TAG_DOUBLE:
                    this.append(key, nbt.getDouble(key));
                    break;
                case Constants.NBT.TAG_BYTE_ARRAY:
                    this.append(key, nbt.getByteArray(key));
                    break;
                case Constants.NBT.TAG_STRING:
                    this.append(key, nbt.getString(key));
                    break;
                case Constants.NBT.TAG_LIST:
                    this.append(key, (NBTTagList) nbt.getTag(key));
                    break;
                case Constants.NBT.TAG_COMPOUND:
                    this.append(key, nbt.getCompoundTag(key));
                    break;
                case Constants.NBT.TAG_INT_ARRAY:
                    this.append(key, nbt.getIntArray(key));
                    break;
                case Constants.NBT.TAG_LONG_ARRAY:
                    try {
                        Field data = NBTTagLongArray.class.getDeclaredField("data");
                        data.setAccessible(true);
                        this.append(key, (long[]) data.get(nbt.getTag(key)));
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to read long array", e);
                    }
                    break;
            }
        }

        if(print) {
            test.hr();
            test.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG);
        }
        return this;
    }

    private void append(String key, byte value) {
        this.getLowestParent(key).add(key, Mapper.BYTE.getApplier().apply(value));
    }

    private void append(String key, short value) {
        this.getLowestParent(key).add(key, Mapper.SHORT.getApplier().apply(value));
    }

    private void append(String key, int value) {
        this.getLowestParent(key).add(key, Mapper.INTEGER.getApplier().apply(value));
    }

    private void append(String key, long value) {
        this.getLowestParent(key).add(key, Mapper.LONG.getApplier().apply(value));
    }

    private void append(String key, float value) {
        this.getLowestParent(key).add(key, Mapper.FLOAT.getApplier().apply(value));
    }

    private void append(String key, double value) {
        this.getLowestParent(key).add(key, Mapper.DOUBLE.getApplier().apply(value));
    }

    private void append(String key, byte[] value) {
        this.getLowestParent(key).add(key, Mapper.BYTE_ARRAY.getApplier().apply(value));
    }

    private void append(String key, String value) {
        this.getLowestParent(key).add(key, Mapper.STRING.getApplier().apply(value));
    }

    private void append(String key, NBTTagList value) {
        this.getLowestParent(key).add(key, Mapper.LIST.getApplier().apply(value));
    }

    private void append(String key, NBTTagCompound value) {
        this.getLowestParent(key).add(key, Mapper.COMPOUND.getApplier().apply(value));
    }

    private void append(String key, int[] value) {
        this.getLowestParent(key).add(key, Mapper.INT_ARRAY.getApplier().apply(value));
    }

    private void append(String key, long[] value) {
        this.getLowestParent(key).add(key, Mapper.INT_ARRAY.getApplier().apply(value));
    }

    public NBTTagCompound read(JsonObject json) {
        NBTTagCompound nbt = new NBTTagCompound();
        for(Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            JsonObject data = entry.getValue().getAsJsonObject();

            String type = data.get("type").getAsString();
            JsonElement value = data.get("value");

            if(type.equals("compound")) {
                nbt.setTag(key, this.read(value.getAsJsonObject()));
            } else if(type.equals("list")) {
                nbt.setTag(key, this.read$list(value.getAsJsonArray()));
            } else {
                switch (type) {
                    case "byte":
                        nbt.setByte(key, value.getAsByte());
                        break;
                    case "short":
                        nbt.setShort(key, value.getAsShort());
                        break;
                    case "int":
                        nbt.setInteger(key, value.getAsInt());
                        break;
                    case "long":
                        nbt.setLong(key, value.getAsLong());
                        break;
                    case "float":
                        nbt.setFloat(key, value.getAsFloat());
                        break;
                    case "double":
                        nbt.setDouble(key, value.getAsDouble());
                        break;
                    case "byte[]":
                        JsonArray array = value.getAsJsonArray();
                        byte[] values = new byte[array.size()];

                        int index = 0;
                        for(JsonElement element : array) {
                            values[index++] = element.getAsByte();
                        }
                        nbt.setByteArray(key, values);
                        break;
                    case "string":
                        nbt.setString(key, value.getAsString());
                        break;
                    case "int[]":
                        JsonArray array2 = value.getAsJsonArray();
                        int[] values2 = new int[array2.size()];

                        int i2 = 0;
                        for(JsonElement element : array2) {
                            values2[i2++] = element.getAsInt();
                        }
                        nbt.setIntArray(key, values2);
                        break;
                    case "long[]":
                        JsonArray array3 = value.getAsJsonArray();
                        long[] values3 = new long[array3.size()];

                        int i3 = 0;
                        for(JsonElement element : array3) {
                            values3[i3++] = element.getAsLong();
                        }
                        nbt.setTag(key, new NBTTagLongArray(values3));
                        break;
                }
            }
        }

        return nbt;
    }

    private NBTTagList read$list(JsonArray array) {
        NBTTagList result = new NBTTagList();
        for(JsonElement element : array) {
            JsonObject object = element.getAsJsonObject();

            String type = object.get("type").getAsString();
            JsonElement value = object.get("value");

            if(type.equals("compound")) {
                result.appendTag(this.read(value.getAsJsonObject()));
            } else if(type.equals("list")) {
                result.appendTag(this.read$list(value.getAsJsonArray()));
            } else {
                switch (type) {
                    case "byte":
                        result.appendTag(new NBTTagByte(value.getAsByte()));
                        break;
                    case "short":
                        result.appendTag(new NBTTagShort(value.getAsShort()));
                        break;
                    case "int":
                        result.appendTag(new NBTTagInt(value.getAsInt()));
                        break;
                    case "long":
                        result.appendTag(new NBTTagLong(value.getAsLong()));
                        break;
                    case "float":
                        result.appendTag(new NBTTagFloat(value.getAsFloat()));
                        break;
                    case "double":
                        result.appendTag(new NBTTagDouble(value.getAsDouble()));
                        break;
                    case "byte[]":
                        JsonArray a = value.getAsJsonArray();
                        byte[] values = new byte[a.size()];

                        int index = 0;
                        for(JsonElement e : a) {
                            values[index++] = e.getAsByte();
                        }
                        result.appendTag(new NBTTagByteArray(values));
                        break;
                    case "string":
                        result.appendTag(new NBTTagString(value.getAsString()));
                        break;
                    case "int[]":
                        JsonArray array2 = value.getAsJsonArray();
                        int[] values2 = new int[array2.size()];

                        int i2 = 0;
                        for(JsonElement e : array2) {
                            values2[i2++] = e.getAsInt();
                        }
                        result.appendTag(new NBTTagIntArray(values2));
                        break;
                    case "long[]":
                        JsonArray array3 = value.getAsJsonArray();
                        long[] values3 = new long[array3.size()];

                        int i3 = 0;
                        for(JsonElement e : array3) {
                            values3[i3++] = e.getAsLong();
                        }
                        result.appendTag(new NBTTagLongArray(values3));
                        break;
                }
            }
        }

        return result;
    }

    private JsonObject getLowestParent(String key) {
        JsonObject result = this.result.toJson();
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

            NBTTagList list = (NBTTagList) value;
            byte listType = (byte) list.getTagType();
            int count = list.tagCount();
            for (int i = 0; i < count; i++) {
                array.add(fromTagBase(list.get(i), listType));
            }

            return new JObject().add("type", "list").add("value", array);
        }),
        COMPOUND(value -> new JObject().add("type", "compound").add("value", new NBTMapper().from((NBTTagCompound) value, false).getResult())),
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

        private static JObject fromTagBase(NBTBase base, byte type) {
            switch (type) {
                case Constants.NBT.TAG_BYTE:
                    return BYTE.applier.apply(((NBTTagByte) base).getByte());
                case Constants.NBT.TAG_SHORT:
                    return SHORT.applier.apply(((NBTTagShort) base).getShort());
                case Constants.NBT.TAG_INT:
                    return INTEGER.applier.apply(((NBTTagInt) base).getInt());
                case Constants.NBT.TAG_LONG:
                    return LONG.applier.apply(((NBTTagLong) base).getInt());
                case Constants.NBT.TAG_FLOAT:
                    return FLOAT.applier.apply(((NBTTagFloat) base).getFloat());
                case Constants.NBT.TAG_DOUBLE:
                    return DOUBLE.applier.apply(((NBTTagDouble) base).getDouble());
                case Constants.NBT.TAG_BYTE_ARRAY:
                    return BYTE_ARRAY.applier.apply(((NBTTagByteArray) base).getByteArray());
                case Constants.NBT.TAG_STRING:
                    return STRING.applier.apply(((NBTTagString) base).getString());
                case Constants.NBT.TAG_LIST:
                    return LIST.applier.apply(base);
                case Constants.NBT.TAG_COMPOUND:
                    return COMPOUND.applier.apply(base);
                case Constants.NBT.TAG_INT_ARRAY:
                    return INT_ARRAY.applier.apply(((NBTTagIntArray) base).getIntArray());
                case Constants.NBT.TAG_LONG_ARRAY:
                    try {
                        Field data = NBTTagLongArray.class.getDeclaredField("data");
                        data.setAccessible(true);
                        return LONG_ARRAY.applier.apply(data.get(base));
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to read long array", e);
                    }
                default :
                    return null;
            }
        }

    }
}
