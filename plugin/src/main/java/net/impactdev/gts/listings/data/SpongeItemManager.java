package net.impactdev.gts.listings.data;

import net.impactdev.gts.api.commands.CommandGenerator;
import net.impactdev.gts.api.data.translators.DataTranslator;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.api.listings.ui.EntrySelection;
import net.impactdev.gts.listings.SpongeItemEntry;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.data.Storable;
import net.impactdev.gts.api.data.registry.DeserializerRegistry;
import net.impactdev.gts.api.listings.ui.EntryUI;
import net.impactdev.gts.api.listings.entries.EntryManager;
import net.impactdev.gts.common.data.NBTMapper;
import net.impactdev.gts.listings.ui.SpongeItemUI;
import net.impactdev.gts.sponge.data.NBTTranslator;
import net.impactdev.gts.util.DataViewJsonManager;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class SpongeItemManager implements EntryManager<SpongeItemEntry, ServerPlayer> {

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
                        return new SpongeItemEntry(snapshot, null);
                    });
        };
    }

    @Override
    public Class<?> getBlacklistType() {
        return ItemType.class;
    }

    @Override
    public Supplier<EntryUI<?, ?, ?>> getSellingUI(ServerPlayer player) {
        return () -> new SpongeItemUI(player);
    }

    @Override
    public void supplyDeserializers() {
        DeserializerRegistry registry = GTSService.getInstance().getGTSComponentManager().getDeserializerRegistry();
        registry.registerDeserializer(SpongeItemEntry.class, 1, json -> {
            DataView view = DataViewJsonManager.readDataViewFromJSON(json.getAsJsonObject("item"));
            CompoundNBT nbt = NBTTranslator.getInstance().translateData(view);

            GTSService service = GTSService.getInstance();
            Collection<DataTranslator<CompoundNBT>> translators = service.getDataTranslatorManager().get(CompoundNBT.class);
            if(!translators.isEmpty()) {
                AtomicReference<CompoundNBT> reference = new AtomicReference<>(nbt);
                translators.forEach(translator -> translator.translate(reference.get()).ifPresent(reference::set));
                nbt = reference.get();
            }

            ItemStackSnapshot snapshot = ItemStack.builder()
                    .fromContainer(NBTTranslator.getInstance().translateFrom(nbt))
                    .build()
                    .createSnapshot();
            return new SpongeItemEntry(snapshot, null);
        });

        registry.registerDeserializer(SpongeItemEntry.class, 2, json -> {
            CompoundNBT nbt = NBTMapper.read(json.getAsJsonObject("item"));

            GTSService service = GTSService.getInstance();
            Collection<DataTranslator<CompoundNBT>> translators = service.getDataTranslatorManager().get(CompoundNBT.class);
            if(!translators.isEmpty()) {
                AtomicReference<CompoundNBT> reference = new AtomicReference<>(nbt);
                translators.forEach(translator -> translator.translate(reference.get()).ifPresent(reference::set));
                nbt = reference.get();
            }

            ItemStackSnapshot snapshot = ItemStack.builder()
                    .fromContainer(NBTTranslator.getInstance().translateFrom(nbt))
                    .build()
                    .createSnapshot();
            return new SpongeItemEntry(snapshot, null);
        });
    }

    @Override
    public CommandGenerator.EntryGenerator<? extends EntrySelection<? extends Entry<?, ?>>> getEntryCommandCreator() {
        return new SpongeItemSellExecutor();
    }

}
