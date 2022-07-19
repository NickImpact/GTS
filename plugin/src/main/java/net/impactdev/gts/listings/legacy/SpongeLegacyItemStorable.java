package net.impactdev.gts.listings.legacy;

import com.google.gson.JsonObject;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.data.translators.DataTranslator;
import net.impactdev.gts.sponge.data.NBTTranslator;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.gts.api.data.Storable;
import net.impactdev.gts.listings.SpongeItemEntry;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.item.inventory.ItemStack;

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
            DataContainer container = DataFormats.JSON.get().read(json.get("element").getAsString());
            CompoundNBT nbt = NBTTranslator.getInstance().translate(container);

            GTSService service = GTSService.getInstance();
            Collection<DataTranslator<CompoundNBT>> translators = service.getDataTranslatorManager().get(CompoundNBT.class);
            if(!translators.isEmpty()) {
                AtomicReference<CompoundNBT> reference = new AtomicReference<>(nbt);
                translators.forEach(translator -> translator.translate(reference.get()).ifPresent(reference::set));
                nbt = reference.get();
            }

            return new SpongeItemEntry(ItemStack.builder()
                    .fromContainer(NBTTranslator.getInstance().translateFrom(nbt))
                    .build()
                    .createSnapshot(),
                    null
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize a legacy item entry", e);
        }
    }
}
