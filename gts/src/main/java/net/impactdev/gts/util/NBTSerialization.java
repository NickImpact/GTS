package net.impactdev.gts.util;

import net.impactdev.json.JElement;
import net.impactdev.json.JObject;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.LongBinaryTag;
import net.kyori.adventure.nbt.ShortBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;

public final class NBTSerialization {

    public static JObject serialize(CompoundBinaryTag nbt) {
        JObject json = new JObject();
        for(String key : nbt.keySet()) {
            write(json, key, nbt.get(key));
        }

        return json;
    }

    private static void write(JObject json, String key, BinaryTag tag) {
        if(tag instanceof CompoundBinaryTag) {
            json.add(key, write$token("compound", serialize((CompoundBinaryTag) tag)));
        } else if(tag instanceof StringBinaryTag) {
            json.add(key, write$token(as(StringBinaryTag.class, tag).value()));
        } else if(tag instanceof ByteBinaryTag) {
            json.add(key, write$token("byte", as(ByteBinaryTag.class, tag).value()));
        } else if(tag instanceof ShortBinaryTag) {
            json.add(key, write$token("short", as(ShortBinaryTag.class, tag).value()));
        } else if(tag instanceof IntBinaryTag) {
            json.add(key, write$token("int", as(IntBinaryTag.class, tag).value()));
        } else if(tag instanceof LongBinaryTag) {
            json.add(key, write$token("long", as(LongBinaryTag.class, tag).value()));
        } else if(tag instanceof FloatBinaryTag) {
            json.add(key, write$token("float", as(FloatBinaryTag.class, tag).value()));
        } else if(tag instanceof DoubleBinaryTag) {
            json.add(key, write$token("double", as(DoubleBinaryTag.class, tag).value()));
//        } else if(tag instanceof )
        }
    }

    private static JObject write$token(String type, JElement element) {
        JObject json = new JObject();
        json.add("type", type);
        json.add("value", element);

        return json;
    }

    private static JObject write$token(String element) {
        JObject json = new JObject();
        json.add("type", "string");
        json.add("value", element);

        return json;
    }

    private static JObject write$token(String type, Number number) {
        JObject json = new JObject();
        json.add("type", type);
        json.add("value", number);

        return json;
    }

    private static JObject write$boolean(boolean value) {
        JObject json = new JObject();
        json.add("type", "boolean");
        json.add("value", value);

        return json;
    }

    private static <T extends BinaryTag> T as(Class<T> type, BinaryTag tag) {
        return type.cast(tag);
    }

}
