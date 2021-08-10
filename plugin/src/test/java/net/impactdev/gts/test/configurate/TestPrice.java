package net.impactdev.gts.test.configurate;

import net.impactdev.gts.api.data.registry.GTSKeyMarker;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.impactor.api.json.factory.JObject;
import net.kyori.adventure.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@GTSKeyMarker("test")
public class TestPrice implements Price<String, Void, Void> {

    private final String price;

    public TestPrice(String price) {
        this.price = price;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public JObject serialize() {
        return new JObject().add("version", this.getVersion()).add("price", this.price);
    }

    @Override
    public String getPrice() {
        return this.price;
    }

    @Override
    public TextComponent getText() {
        return null;
    }

    @Override
    public Display<Void> getDisplay() {
        return null;
    }

    @Override
    public boolean canPay(UUID payer) {
        return false;
    }

    @Override
    public void pay(UUID payer, @Nullable Object source, @NonNull AtomicBoolean marker) {

    }

    @Override
    public boolean reward(UUID recipient) {
        return false;
    }

    @Override
    public Class<Void> getSourceType() {
        return Void.class;
    }

    @Override
    public long calculateFee(boolean listingType) {
        return 0;
    }
}
