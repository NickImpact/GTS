package net.impactdev.gts.locale.translations;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.locale.translations.resolvers.MultiLineTranslation;
import net.impactdev.gts.locale.translations.resolvers.SingularTranslation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class TranslationSet {

    private final Map<String, Translation<?>> translations = Maps.newLinkedHashMap();

    public static TranslationSet fromJson(JsonObject json) throws Exception {
        TranslationSet result = new TranslationSet();
        try {
            parse(result, null, json);
        } catch (IllegalStateException e) {
            throw new Exception("");
        }

        return result;
    }

    public <T> Translation<T> translation(String key) {
        return (Translation<T>) this.translations.get(key);
    }

    public void register(String key, Translation<?> translation) {
        this.translations.put(key, translation);
    }

    private static void parse(@NotNull TranslationSet set, @Nullable String path, @NotNull JsonObject parent) {
        parent.entrySet().forEach(entry -> {
            String key = entry.getKey();
            JsonElement element = entry.getValue();

            String target;
            if(path == null) {
                target = key;
            } else {
                target = path + "." + key;
            }

            if(element.isJsonObject()) {
                parse(set, target, element.getAsJsonObject());
            } else if(element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                List<String> template = Lists.newArrayList();
                for(JsonElement value : array) {
                    if(!value.isJsonPrimitive()) {
                        throw new IllegalStateException("Invalid JSON target within array");
                    }

                    template.add(value.getAsString());
                }

                set.register(target, new MultiLineTranslation(template));
            } else if(element.isJsonPrimitive()) {
                set.register(target, new SingularTranslation(element.getAsString()));
            } else {
                throw new IllegalStateException("Reached a null field within JSON");
            }
        });
    }
}
