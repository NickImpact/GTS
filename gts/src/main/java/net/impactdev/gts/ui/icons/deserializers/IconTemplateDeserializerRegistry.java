package net.impactdev.gts.ui.icons.deserializers;

import java.util.HashMap;
import java.util.Map;

public final class IconTemplateDeserializerRegistry {

    private static final Map<String, TemplateDeserializer> deserializers = new HashMap<>();

    public static void register(String key, TemplateDeserializer deserializer) {
        deserializers.put(key, deserializer);
    }

    public static TemplateDeserializer deserializer(String key) {
        return deserializers.get(key);
    }

    static {
        register("fallback", new NBTDeserializer());
        register("skull", new SkullDeserializer());
    }
}
