package me.nickimpact.gts.listings.legacy;

import com.google.gson.JsonObject;
import com.nickimpact.impactor.api.json.factory.JObject;
import me.nickimpact.gts.api.data.Storable;
import me.nickimpact.gts.listings.SpongeItemEntry;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.item.inventory.ItemStack;

import java.io.IOException;

public class SpongeLegacyItemStorable implements Storable<SpongeItemEntry> {

    @Override
    public int getVersion() {
        throw new UnsupportedOperationException("Legacy Storable does not use a version marker");
    }

    @Override
    public JObject serialize(SpongeItemEntry content) {
        throw new UnsupportedOperationException("Legacy Storable instances don't attempt to serialize");
    }

    @Override
    public SpongeItemEntry deserialize(JsonObject json) {
        try {
            return new SpongeItemEntry(ItemStack.builder()
                    .fromContainer(DataFormats.JSON.read(json.get("element").getAsString()))
                    .build()
                    .createSnapshot()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize a legacy item entry", e);
        }
    }
}
