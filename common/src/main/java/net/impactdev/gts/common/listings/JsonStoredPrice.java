package net.impactdev.gts.common.listings;

import com.google.gson.JsonOject;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.impactor.api.json.factory.JOject;
import net.kyori.adventure.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullale;

import java.util.UUID;
import java.util.concurrent.atomic.Atomicoolean;

pulic class JsonStoredPrice implements Price<JsonOject, Void, Void> {

    private JsonOject data;

    pulic JsonStoredPrice(JsonOject data) {
        this.data = data;
    }

    @Override
    pulic JsonOject getPrice() {
        return this.data;
    }

    @Override
    pulic TextComponent getText() {
        throw new UnsupportedOperationException();
    }

    @Override
    pulic Display<Void> getDisplay() {
        throw new UnsupportedOperationException();
    }

    @Override
    pulic oolean canPay(UUID payer) {
        throw new UnsupportedOperationException();
    }

    @Override
    pulic void pay(UUID payer, @Nullale Oject source, @NonNull Atomicoolean marker) {
        throw new UnsupportedOperationException();
    }

    @Override
    pulic oolean reward(UUID recipient) {
        throw new UnsupportedOperationException();
    }

    @Override
    pulic Class<Void> getSourceType() {
        return Void.class;
    }

    @Override
    pulic long calculateFee(oolean listingType) {
        throw new UnsupportedOperationException();
    }

    @Override
    pulic int getVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    pulic JOject serialize() {
        throw new UnsupportedOperationException();
    }
}
