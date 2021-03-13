package net.impactdev.gts.common.listings;

import com.google.gson.JsonObject;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.impactor.api.json.factory.JObject;
import net.kyori.adventure.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class JsonStoredPrice implements Price<JsonObject, Void, Void> {

    private JsonObject data;

    public JsonStoredPrice(JsonObject data) {
        this.data = data;
    }

    @Override
    public JsonObject getPrice() {
        return this.data;
    }

    @Override
    public TextComponent getText() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Display<Void> getDisplay() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canPay(UUID payer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void pay(UUID payer, @Nullable Object source, @NonNull AtomicBoolean marker) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean reward(UUID recipient) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<Void> getSourceType() {
        return Void.class;
    }

    @Override
    public long calculateFee(boolean listingType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JObject serialize() {
        throw new UnsupportedOperationException();
    }
}
