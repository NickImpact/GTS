package net.impactdev.gts.listings.data;

import net.impactdev.gts.api.data.Storable;
import net.impactdev.gts.api.listings.ui.EntryUI;
import net.impactdev.gts.api.listings.entries.EntryManager;
import net.impactdev.gts.listings.SpongeItemEntry;
import net.impactdev.gts.listings.ui.SpongeItemUI;
import net.impactdev.gts.util.DataViewJsonManager;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.function.Supplier;

public class SpongeItemManager implements EntryManager<SpongeItemEntry, Player> {

    @Override
    public String getName() {
        return "Items";
    }

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
    public Class<?> getBlacklistType() {
        return ItemType.class;
    }

    @Override
    public Supplier<EntryUI<?, ?, ?>> getSellingUI(Player player) {
        return () -> new SpongeItemUI(player);
    }

}
