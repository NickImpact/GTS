package net.impactdev.gts.common.data;

import net.impactdev.gts.api.data.ResourceManager;
import net.impactdev.gts.api.data.Storable;

public class ResourceManagerImpl<T> implements ResourceManager<T> {

    private final String name;
    private final String itemID;
    private final Storable.Deserializer<T> deserializer;

    public ResourceManagerImpl(String name, String itemID, Storable.Deserializer<T> deserializer) {
        this.name = name;
        this.itemID = itemID;
        this.deserializer = deserializer;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getItemID() {
        return this.itemID;
    }

    @Override
    public Storable.Deserializer<T> getDeserializer() {
        return this.deserializer;
    }
}
