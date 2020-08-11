package me.nickimpact.gts.listings.data;

import com.google.gson.JsonObject;
import com.nickimpact.impactor.api.json.factory.JObject;
import me.nickimpact.gts.api.listings.ui.EntryUI;
import me.nickimpact.gts.api.listings.entries.EntryManager;
import me.nickimpact.gts.listings.SpongeItemEntry;
import me.nickimpact.gts.listings.ui.SpongeItemUI;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import java.io.IOException;
import java.util.function.Supplier;

public class SpongeItemManager implements EntryManager<SpongeItemEntry, Player> {

    @Override
    public String getItemID() {
        return "minecraft:diamond";
    }

    @Override
    public Supplier<EntryUI<?, ?, ?>> getSellingUI(Player player) {
        return () -> new SpongeItemUI(player);
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public JObject serialize(SpongeItemEntry content) {
        return content.getInternalData().add("version", this.getVersion());
    }

    @Override
    public SpongeItemEntry deserialize(JsonObject json) {
        try {
            if(json.has("item")) {
                return new SpongeItemEntry(ItemStack.builder()
                        .fromContainer(DataFormats.JSON.read(json.get("item").getAsString()))
                        .build()
                        .createSnapshot()
                );
            }

            throw new RuntimeException("Incoming JSON data does not contain item information");
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize an item", e);
        }
    }

}
