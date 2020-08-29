package me.nickimpact.gts.listings.data;

import me.nickimpact.gts.api.data.Storable;
import me.nickimpact.gts.api.listings.ui.EntryUI;
import me.nickimpact.gts.api.listings.entries.EntryManager;
import me.nickimpact.gts.listings.SpongeItemEntry;
import me.nickimpact.gts.listings.ui.SpongeItemUI;
import me.nickimpact.gts.util.DataViewJsonManager;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.function.Supplier;

public class SpongeItemManager implements EntryManager<SpongeItemEntry, Player> {

    @Override
    public String getItemID() {
        return "minecraft:diamond";
    }

    @Override
    public Storable.Deserializer<SpongeItemEntry> getDeserializer() {
        return json -> {
            ItemStackSnapshot snapshot = ItemStack.builder()
                    .fromContainer(DataViewJsonManager.readDataViewFromJSON(json.getAsJsonObject("item")))
                    .build()
                    .createSnapshot();
            return new SpongeItemEntry(snapshot);
        };
    }

    @Override
    public Supplier<EntryUI<?, ?, ?>> getSellingUI(Player player) {
        return () -> new SpongeItemUI(player);
    }

}
