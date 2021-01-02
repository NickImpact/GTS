package net.impactdev.gts.listings.data;

import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.data.Storable;
import net.impactdev.gts.api.data.registry.DeserializerRegistry;
import net.impactdev.gts.api.listings.ui.EntryUI;
import net.impactdev.gts.api.listings.entries.EntryManager;
import net.impactdev.gts.common.data.NBTMapper;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.listings.SpongeItemEntry;
import net.impactdev.gts.listings.ui.SpongeItemUI;
import net.impactdev.gts.util.DataViewJsonManager;
import net.minecraft.nbt.NBTTagCompound;
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
        DeserializerRegistry registry = GTSService.getInstance().getGTSComponentManager().getDeserializerRegistry();

        return json -> {
            int version = 1;
            if(json.has("version")) {
                version = json.get("version").getAsInt();
            }

            return registry.getDeserializer(SpongeItemEntry.class, version)
                    .map(function -> function.apply(json))
                    .orElseGet(() -> {
                        ItemStackSnapshot snapshot = ItemStack.builder()
                                .fromContainer(DataViewJsonManager.readDataViewFromJSON(json.getAsJsonObject("item")))
                                .build()
                                .createSnapshot();
                        return new SpongeItemEntry(snapshot);
                    });
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

    @Override
    public void supplyDeserializers() {
        DeserializerRegistry registry = GTSService.getInstance().getGTSComponentManager().getDeserializerRegistry();
        registry.registerDeserializer(SpongeItemEntry.class, 1, json -> {
            ItemStackSnapshot snapshot = ItemStack.builder()
                    .fromContainer(DataViewJsonManager.readDataViewFromJSON(json.getAsJsonObject("item")))
                    .build()
                    .createSnapshot();
            return new SpongeItemEntry(snapshot);
        });

        registry.registerDeserializer(SpongeItemEntry.class, 2, json -> {
            NBTTagCompound nbt = new NBTMapper().read(json.getAsJsonObject("item"));

            ItemStackSnapshot snapshot = ItemStack.builder()
                    .fromContainer(NBTTranslator.getInstance().translateFrom(nbt))
                    .build()
                    .createSnapshot();
            return new SpongeItemEntry(snapshot);
        });
    }

}
