package net.impactdev.gts.test.configurate;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.data.Storable;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.api.listings.prices.PriceManager;
import net.impactdev.gts.api.listings.ui.EntryUI;
import net.impactdev.gts.api.util.TriConsumer;
import net.impactdev.impactor.api.gui.UI;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TestPriceDataManager implements PriceManager<TestPrice, Void> {

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
    public TriConsumer<Void, EntryUI<?, ?, ?>, BiConsumer<EntryUI<?, ?, ?>, Price<?, ?, ?>>> process() {
        return (x, y, z) -> {};
    }

    @Override
    public <U extends UI<?, ?, ?, ?>> Optional<PriceSelectorUI<U>> getSelector(Void viewer, Price<?, ?, ?> price, Consumer<Object> callback) {
        return Optional.empty();
    }
}
