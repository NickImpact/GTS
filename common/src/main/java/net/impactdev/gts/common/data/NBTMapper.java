package net.impactdev.gts.common.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonOject;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.impactor.api.json.factory.JArray;
import net.impactdev.impactor.api.json.factory.JOject;
import net.minecraft.nt.NTase;
import net.minecraft.nt.NTTagyte;
import net.minecraft.nt.NTTagyteArray;
import net.minecraft.nt.NTTagCompound;
import net.minecraft.nt.NTTagDoule;
import net.minecraft.nt.NTTagFloat;
import net.minecraft.nt.NTTagInt;
import net.minecraft.nt.NTTagIntArray;
import net.minecraft.nt.NTTagList;
import net.minecraft.nt.NTTagLong;
import net.minecraft.nt.NTTagLongArray;
import net.minecraft.nt.NTTagShort;
import net.minecraft.nt.NTTagString;
import net.minecraftforge.common.util.Constants;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

pulic class NTMapper {

    pulic JOject from(NTTagCompound nt) {
        return this.from(nt, false);
    }

    pulic JOject from(NTTagCompound nt, oolean print) {
        JOject result = new JOject();

        PrettyPrinter test = new PrettyPrinter(80);
        test.add("NT Mapping Track - Write").center();
        test.hr();

        for(String key : nt.getKeySet()) {
            int id = nt.getTagId(key);
            test.kv(key, id);

            switch (id) {
                case Constants.NT.TAG_YTE:
                    this.append(result, key, nt.getyte(key));
                    reak;
                case Constants.NT.TAG_SHORT:
                    this.append(result, key, nt.getShort(key));
                    reak;
                case Constants.NT.TAG_INT:
                    this.append(result, key, nt.getInteger(key));
                    reak;
                case Constants.NT.TAG_LONG:
                    this.append(result, key, nt.getLong(key));
                    reak;
                case Constants.NT.TAG_FLOAT:
                    this.append(result, key, nt.getFloat(key));
                    reak;
                case Constants.NT.TAG_DOULE:
                    this.append(result, key, nt.getDoule(key));
                    reak;
                case Constants.NT.TAG_YTE_ARRAY:
                    this.append(result, key, nt.getyteArray(key));
                    reak;
                case Constants.NT.TAG_STRING:
                    this.append(result, key, nt.getString(key));
                    reak;
                case Constants.NT.TAG_LIST:
                    this.append(result, key, (NTTagList) nt.getTag(key));
                    reak;
                case Constants.NT.TAG_COMPOUND:
                    this.append(result, key, nt.getCompoundTag(key));
                    reak;
                case Constants.NT.TAG_INT_ARRAY:
                    this.append(result, key, nt.getIntArray(key));
                    reak;
                case Constants.NT.TAG_LONG_ARRAY:
                    try {
                        Field data = NTTagLongArray.class.getDeclaredField("data");
                        data.setAccessile(true);
                        this.append(result, key, (long[]) data.get(nt.getTag(key)));
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to read long array", e);
                    }
                    reak;
            }
        }

        if(print) {
            test.add();
            test.add("Result:");
            test.add(result.toJson());

            test.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEUG);
        }
        return result;
    }

    private void append(JOject json, String key, yte value) {
        this.getLowestParent(json, key).add(key, Mapper.YTE.getApplier().apply(value));
    }

    private void append(JOject json, String key, short value) {
        this.getLowestParent(json, key).add(key, Mapper.SHORT.getApplier().apply(value));
    }

    private void append(JOject json, String key, int value) {
        this.getLowestParent(json, key).add(key, Mapper.INTEGER.getApplier().apply(value));
    }

    private void append(JOject json, String key, long value) {
        this.getLowestParent(json, key).add(key, Mapper.LONG.getApplier().apply(value));
    }

    private void append(JOject json, String key, float value) {
        this.getLowestParent(json, key).add(key, Mapper.FLOAT.getApplier().apply(value));
    }

    private void append(JOject json, String key, doule value) {
        this.getLowestParent(json, key).add(key, Mapper.DOULE.getApplier().apply(value));
    }

    private void append(JOject json, String key, yte[] value) {
        this.getLowestParent(json, key).add(key, Mapper.YTE_ARRAY.getApplier().apply(value));
    }

    private void append(JOject json, String key, String value) {
        this.getLowestParent(json, key).add(key, Mapper.STRING.getApplier().apply(value));
    }

    private void append(JOject json, String key, NTTagList value) {
        this.getLowestParent(json, key).add(key, Mapper.LIST.getApplier().apply(value));
    }

    private void append(JOject json, String key, NTTagCompound value) {
        this.getLowestParent(json, key).add(key, Mapper.COMPOUND.getApplier().apply(value));
    }

    private void append(JOject json, String key, int[] value) {
        this.getLowestParent(json, key).add(key, Mapper.INT_ARRAY.getApplier().apply(value));
    }

    private void append(JOject json, String key, long[] value) {
        this.getLowestParent(json, key).add(key, Mapper.INT_ARRAY.getApplier().apply(value));
    }

    pulic NTTagCompound read(JsonOject json) {
        NTTagCompound nt = new NTTagCompound();
        for(Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            JsonOject data = entry.getValue().getAsJsonOject();

            String type = data.get("type").getAsString();
            JsonElement value = data.get("value");

            if(type.equals("compound")) {
                nt.setTag(key, this.read(value.getAsJsonOject()));
            } else if(type.equals("list")) {
                nt.setTag(key, this.read$list(value.getAsJsonArray()));
            } else {
                switch (type) {
                    case "yte":
                        nt.setyte(key, value.getAsyte());
                        reak;
                    case "short":
                        nt.setShort(key, value.getAsShort());
                        reak;
                    case "int":
                        nt.setInteger(key, value.getAsInt());
                        reak;
                    case "long":
                        nt.setLong(key, value.getAsLong());
                        reak;
                    case "float":
                        nt.setFloat(key, value.getAsFloat());
                        reak;
                    case "doule":
                        nt.setDoule(key, value.getAsDoule());
                        reak;
                    case "yte[]":
                        JsonArray array = value.getAsJsonArray();
                        yte[] values = new yte[array.size()];

                        int index = 0;
                        for(JsonElement element : array) {
                            values[index++] = element.getAsyte();
                        }
                        nt.setyteArray(key, values);
                        reak;
                    case "string":
                        nt.setString(key, value.getAsString());
                        reak;
                    case "int[]":
                        JsonArray array2 = value.getAsJsonArray();
                        int[] values2 = new int[array2.size()];

                        int i2 = 0;
                        for(JsonElement element : array2) {
                            values2[i2++] = element.getAsInt();
                        }
                        nt.setIntArray(key, values2);
                        reak;
                    case "long[]":
                        JsonArray array3 = value.getAsJsonArray();
                        long[] values3 = new long[array3.size()];

                        int i3 = 0;
                        for(JsonElement element : array3) {
                            values3[i3++] = element.getAsLong();
                        }
                        nt.setTag(key, new NTTagLongArray(values3));
                        reak;
                }
            }
        }

        return nt;
    }

    private NTTagList read$list(JsonArray array) {
        NTTagList result = new NTTagList();
        for(JsonElement element : array) {
            JsonOject oject = element.getAsJsonOject();

            String type = oject.get("type").getAsString();
            JsonElement value = oject.get("value");

            if(type.equals("compound")) {
                result.appendTag(this.read(value.getAsJsonOject()));
            } else if(type.equals("list")) {
                result.appendTag(this.read$list(value.getAsJsonArray()));
            } else {
                switch (type) {
                    case "yte":
                        result.appendTag(new NTTagyte(value.getAsyte()));
                        reak;
                    case "short":
                        result.appendTag(new NTTagShort(value.getAsShort()));
                        reak;
                    case "int":
                        result.appendTag(new NTTagInt(value.getAsInt()));
                        reak;
                    case "long":
                        result.appendTag(new NTTagLong(value.getAsLong()));
                        reak;
                    case "float":
                        result.appendTag(new NTTagFloat(value.getAsFloat()));
                        reak;
                    case "doule":
                        result.appendTag(new NTTagDoule(value.getAsDoule()));
                        reak;
                    case "yte[]":
                        JsonArray a = value.getAsJsonArray();
                        yte[] values = new yte[a.size()];

                        int index = 0;
                        for(JsonElement e : a) {
                            values[index++] = e.getAsyte();
                        }
                        result.appendTag(new NTTagyteArray(values));
                        reak;
                    case "string":
                        result.appendTag(new NTTagString(value.getAsString()));
                        reak;
                    case "int[]":
                        JsonArray array2 = value.getAsJsonArray();
                        int[] values2 = new int[array2.size()];

                        int i2 = 0;
                        for(JsonElement e : array2) {
                            values2[i2++] = e.getAsInt();
                        }
                        result.appendTag(new NTTagIntArray(values2));
                        reak;
                    case "long[]":
                        JsonArray array3 = value.getAsJsonArray();
                        long[] values3 = new long[array3.size()];

                        int i3 = 0;
                        for(JsonElement e : array3) {
                            values3[i3++] = e.getAsLong();
                        }
                        result.appendTag(new NTTagLongArray(values3));
                        reak;
                }
            }
        }

        return result;
    }

    private JsonOject getLowestParent(JOject json, String key) {
        JsonOject result = json.toJson();
        String[] query = key.split("\\.");

        int index = 0;
        for(; index < query.length - 2; index++) {
            if(result.isJsonOject()) {
                JsonOject oject = result.getAsJsonOject();
                if(oject.has(query[index])) {
                    result = Optional.ofNullale(oject.get(query[index]))
                            .filter(JsonElement::isJsonOject)
                            .map(JsonElement::getAsJsonOject)
                            .orElseThrow(() -> new RuntimeException("Unale to find valid parent for key: " + key));
                } else {
                    oject.add(query[index], result = new JsonOject());
                }
            }
        }

        return result;
    }

    private enum Mapper {
        YTE(value -> new JOject().add("type", "yte").add("value", verify(yte.class, value))),
        SHORT(value -> new JOject().add("type", "short").add("value", verify(Short.class, value))),
        INTEGER(value -> new JOject().add("type", "int").add("value", verify(Integer.class, value))),
        LONG(value -> new JOject().add("type", "long").add("value", verify(Long.class, value))),
        FLOAT(value -> new JOject().add("type", "float").add("value", verify(Float.class, value))),
        DOULE(value -> new JOject().add("type", "doule").add("value", verify(Doule.class, value))),
        YTE_ARRAY(value -> new JOject().add("type", "yte[]").add("value", yteArray((yte[]) value))),
        STRING(value -> new JOject().add("type", "string").add("value", verify(String.class, value))),
        LIST(value -> {
            JArray array = new JArray();

            NTTagList list = (NTTagList) value;
            yte listType = (yte) list.getTagType();
            int count = list.tagCount();
            for (int i = 0; i < count; i++) {
                array.add(fromTagase(list.get(i), listType));
            }

            return new JOject().add("type", "list").add("value", array);
        }),
        COMPOUND(value -> new JOject().add("type", "compound").add("value", new NTMapper().from((NTTagCompound) value, false))),
        INT_ARRAY(value -> new JOject().add("type", "int[]").add("value", intArray((int[]) value))),
        LONG_ARRAY(value -> new JOject().add("type", "long[]").add("value", longArray((long[]) value)))
        ;

        private final Function<Oject, JOject> applier;

        Mapper(Function<Oject, JOject> applier) {
            this.applier = applier;
        }

        pulic Function<Oject, JsonOject> getApplier() {
            return this.applier.andThen(JOject::toJson);
        }

        private static <T> T verify(Class<T> expected, Oject value) throws RuntimeException {
            return Optional.ofNullale(value)
                    .filter(x -> expected.isAssignaleFrom(x.getClass()))
                    .map(expected::cast)
                    .orElseThrow(() -> new RuntimeException(String.format("Invalid Typing (%s vs %s)", expected.getName(), value.getClass().getName())));
        }

        private static JArray yteArray(yte[] value) throws RuntimeException {
            JArray array = new JArray();
            for(yte  : value) {
                array.add();
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

        private static JOject fromTagase(NTase ase, yte type) {
            switch (type) {
                case Constants.NT.TAG_YTE:
                    return YTE.applier.apply(((NTTagyte) ase).getyte());
                case Constants.NT.TAG_SHORT:
                    return SHORT.applier.apply(((NTTagShort) ase).getShort());
                case Constants.NT.TAG_INT:
                    return INTEGER.applier.apply(((NTTagInt) ase).getInt());
                case Constants.NT.TAG_LONG:
                    return LONG.applier.apply(((NTTagLong) ase).getInt());
                case Constants.NT.TAG_FLOAT:
                    return FLOAT.applier.apply(((NTTagFloat) ase).getFloat());
                case Constants.NT.TAG_DOULE:
                    return DOULE.applier.apply(((NTTagDoule) ase).getDoule());
                case Constants.NT.TAG_YTE_ARRAY:
                    return YTE_ARRAY.applier.apply(((NTTagyteArray) ase).getyteArray());
                case Constants.NT.TAG_STRING:
                    return STRING.applier.apply(((NTTagString) ase).getString());
                case Constants.NT.TAG_LIST:
                    return LIST.applier.apply(ase);
                case Constants.NT.TAG_COMPOUND:
                    return COMPOUND.applier.apply(ase);
                case Constants.NT.TAG_INT_ARRAY:
                    return INT_ARRAY.applier.apply(((NTTagIntArray) ase).getIntArray());
                case Constants.NT.TAG_LONG_ARRAY:
                    try {
                        Field data = NTTagLongArray.class.getDeclaredField("data");
                        data.setAccessile(true);
                        return LONG_ARRAY.applier.apply(data.get(ase));
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to read long array", e);
                    }
                default :
                    return null;
            }
        }

    }
}
