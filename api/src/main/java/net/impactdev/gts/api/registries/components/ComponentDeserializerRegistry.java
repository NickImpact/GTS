package net.impactdev.gts.api.registries.components;

import com.google.gson.JsonObject;
import net.impactdev.gts.api.registries.Registry;
import net.impactdev.gts.api.storage.StorableContent;

public interface ComponentDeserializerRegistry extends Registry.Lockable<Class<?>, StorableContent.Deserializer<?>>{

    <T> T deserialize(Class<T> key, JsonObject json);

}
