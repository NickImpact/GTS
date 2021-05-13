package net.impactdev.gts.listings.legacy;

import com.google.gson.JsonObject;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.data.translators.DataTranslator;
import net.impactdev.gts.sponge.data.NBTTranslator;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.gts.api.data.Storable;
import net.impactdev.gts.listings.SpongeItemEntry;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

public class SpongeLegacyItemStorable implements Storable, Storable.Deserializer<SpongeItemEntry> {

    @Override
    public int getVersion() {
        throw new UnsupportedOperationException("Legacy Storable does not use a version marker");
    }

    @Override
    public JObject serialize() {
        throw new UnsupportedOperationException("Legacy Storable instances don't attempt to serialize");
    }

    @Override
    public SpongeItemEntry deserialize(JsonObject json) {
        try {
            DataContainer container = DataFormats.JSON.read(json.get("element").getAsString());
            NBTTagCompound nbt = NBTTranslator.getInstance().translate(container);

            GTSService service = GTSService.getInstance();
            Collection<DataTranslator<NBTTagCompound>> translators = service.getDataTranslatorManager().get(NBTTagCompound.class);
            if(!translators.isEmpty()) {
                AtomicReference<NBTTagCompound> reference = new AtomicReference<>(nbt);
                translators.forEach(translator -> translator.translate(reference.get()).ifPresent(reference::set));
                nbt = reference.get();
            }

            return new SpongeItemEntry(ItemStack.builder()
                    .fromContainer(NBTTranslator.getInstance().translateFrom(nbt))
                    .build()
                    .createSnapshot()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize a legacy item entry", e);
        }
    }
}
