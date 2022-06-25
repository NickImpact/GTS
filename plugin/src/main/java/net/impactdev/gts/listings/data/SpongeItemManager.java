package net.impactdev.gts.listings.data;

import net.impactdev.gts.api.commands.CommandGenerator;
import net.impactdev.gts.api.data.translators.DataTranslator;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.api.listings.ui.EntrySelection;
import net.impactdev.gts.api.util.Version;
import net.impactdev.gts.common.exceptions.DataContentException;
import net.impactdev.gts.listings.SpongeItemEntry;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.data.Storable;
import net.impactdev.gts.api.data.registry.DeserializerRegistry;
import net.impactdev.gts.api.listings.ui.EntryUI;
import net.impactdev.gts.api.listings.entries.EntryManager;
import net.impactdev.gts.common.data.NBTMapper;
import net.impactdev.gts.listings.ui.SpongeItemUI;
import net.impactdev.gts.util.DataViewJsonManager;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.utilities.printing.PrettyPrinter;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class SpongeItemManager implements EntryManager<SpongeItemEntry> {

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
                    .orElseThrow(() -> new DataContentException(Version.of(Sponge.platform().minecraftVersion().name()), 3));
        };
    }

    @Override
    public Class<?> getBlacklistType() {
        return ItemType.class;
    }

    @Override
    public Supplier<EntryUI<?>> getSellingUI(PlatformPlayer player) {
        return () -> new SpongeItemUI(player);
    }

    @Override
    public void supplyDeserializers() {
        DeserializerRegistry registry = GTSService.getInstance().getGTSComponentManager().getDeserializerRegistry();
        registry.registerDeserializer(SpongeItemEntry.class, 3, json -> {
            CompoundNBT nbt = NBTMapper.read(json.getAsJsonObject("item"));

            GTSService service = GTSService.getInstance();
            Collection<DataTranslator<CompoundNBT>> translators = service.getDataTranslatorManager().get(CompoundNBT.class);
            if(!translators.isEmpty()) {
                AtomicReference<CompoundNBT> reference = new AtomicReference<>(nbt);
                translators.forEach(translator -> translator.translate(reference.get()).ifPresent(reference::set));
                nbt = reference.get();
            }

            net.minecraft.item.ItemStack stack = net.minecraft.item.ItemStack.of(nbt);
            if(stack.equals(net.minecraft.item.ItemStack.EMPTY)) {
                PrettyPrinter logger = new PrettyPrinter(80);
                logger.title("Failed to deserialize an ItemStack");
            }

            return new SpongeItemEntry(((ItemStack) (Object) stack).createSnapshot(), null);
        });
    }

    @Override
    public CommandGenerator.EntryGenerator<? extends EntrySelection<? extends Entry<?, ?>>> getEntryCommandCreator() {
        return new SpongeItemSellExecutor();
    }

    @Override
    public boolean supports(Version game, int content) {
        if(Version.Minecraft.v1_12_2.equals(game)) {
            return content == 1 || content == 2;
        }

        return content == 3;
    }

}
