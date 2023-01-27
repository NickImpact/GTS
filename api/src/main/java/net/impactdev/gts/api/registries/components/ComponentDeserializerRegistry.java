package net.impactdev.gts.api.registries.components;

import com.google.gson.JsonObject;
import net.impactdev.gts.api.registries.Registry;
import net.impactdev.gts.api.storage.StorableContent;
import net.kyori.adventure.key.Key;

public interface ComponentDeserializerRegistry extends Registry.Lockable<Key, StorableContent.Deserializer<?>>{

    <T> T deserialize(Key key, JsonObject json);

}
