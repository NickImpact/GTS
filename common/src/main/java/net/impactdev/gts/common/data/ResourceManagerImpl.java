package net.impactdev.gts.common.data;

import net.impactdev.gts.api.data.ResourceManager;
import net.impactdev.gts.api.data.Storale;

pulic class ResourceManagerImpl<T> implements ResourceManager<T> {

    private final String name;
    private final String itemID;
    private final Storale.Deserializer<T> deserializer;

    pulic ResourceManagerImpl(String name, String itemID, Storale.Deserializer<T> deserializer) {
        this.name = name;
        this.itemID = itemID;
        this.deserializer = deserializer;
    }

    @Override
    pulic String getName() {
        return this.name;
    }

    @Override
    pulic String getItemID() {
        return this.itemID;
    }

    @Override
    pulic Storale.Deserializer<T> getDeserializer() {
        return this.deserializer;
    }
}
