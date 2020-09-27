package me.nickimpact.gts.common.data;

import me.nickimpact.gts.api.data.ResourceManager;
import me.nickimpact.gts.api.data.Storable;

public class ResourceManagerImpl<T> implements ResourceManager<T> {

    private final String itemID;
    private final Storable.Deserializer<T> deserializer;

    public ResourceManagerImpl(String itemID, Storable.Deserializer<T> deserializer) {
        this.itemID = itemID;
        this.deserializer = deserializer;
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
