package net.impactdev.gts.test.configurate;

import com.google.gson.GsonBuilder;
import net.impactdev.gts.api.commands.CommandGenerator;
import net.impactdev.gts.api.data.Storable;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.api.listings.prices.PriceManager;
import net.impactdev.gts.api.listings.ui.EntryUI;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.ui.containers.ImpactorUI;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TestPriceDataManager implements PriceManager<TestPrice> {

    @Override
    public String getName() {
        return "Test Price Manager";
    }

    @Override
    public String getItemID() {
        return "";
    }

    @Override
    public Storable.Deserializer<TestPrice> getDeserializer() {
        return json -> new GsonBuilder().setPrettyPrinting().create().fromJson(json, TestPrice.class);
    }

    @Override
    public BiConsumer<EntryUI<?, ?, ?>, BiConsumer<EntryUI<?, ?, ?>, Price<?, ?, ?>>> process() {
        return (y, z) -> {};
    }

    @Override
    public <U extends ImpactorUI> Optional<PriceSelectorUI<U>> getSelector(PlatformPlayer viewer, Price<?, ?, ?> price, Consumer<Object> callback) {
        return Optional.empty();
    }

    @Override
    public CommandGenerator.PriceGenerator<? extends Price<?, ?, ?>> getPriceCommandCreator() {
        return null;
    }
}
