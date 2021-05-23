package net.impactdev.gts.velocity.listings;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.common.listings.JsonStoredEntry;
import net.impactdev.gts.common.listings.JsonStoredPrice;
import net.impactdev.impactor.api.json.factory.JObject;

import java.time.LocalDateTime;
import java.util.UUID;

public class VelocityBIN extends VelocityListing implements BuyItNow {

    private final JsonStoredPrice price;
    private boolean purchased;
    private final UUID purchaser;
    private final boolean stashed;

    public VelocityBIN(VelocityBINBuilder builder) {
        super(builder.id, builder.lister, builder.entry, builder.expiration);
        this.price = builder.price;
        this.purchased = builder.purchased;
        this.stashed = builder.stashed;
        this.purchaser = builder.purchaser;
    }

    @Override
    public Price<?, ?, ?> getPrice() {
        return this.price;
    }

    @Override
    public UUID purchaser() {
        return this.purchaser;
    }

    @Override
    public boolean stashedForPurchaser() {
        return this.stashed;
    }

    @Override
    public boolean isPurchased() {
        return this.purchased;
    }

    @Override
    public void markPurchased() {
        this.purchased = true;
    }

    @Override
    public JObject serialize() {
        JObject json = super.serialize();
        json.add("price", this.price.getPrice());
        json.add("type", "bin");

        return json;
    }

    public static VelocityBIN deserialize(JsonObject json) {
        VelocityBINBuilder builder = (VelocityBINBuilder) BuyItNow.builder()
                .id(UUID.fromString(json.get("id").getAsString()))
                .lister(UUID.fromString(json.get("lister").getAsString()))
                .expiration(LocalDateTime.parse(json.getAsJsonObject("timings").get("expiration").getAsString()));

        JsonObject element = json.getAsJsonObject("entry");
        builder.entry(new JsonStoredEntry(element));

        JsonObject price = json.getAsJsonObject("price");
        builder.price(new JsonStoredPrice(price));
        if(price.has("purchased") && price.get("purchased").getAsBoolean()) {
            builder.purchased();
        }
        if(json.get("stashed").getAsBoolean()) {
            builder.stashedForPurchaser();
            builder.purchaser(UUID.fromString(json.get("purchaser").getAsString()));
        }

        return builder.build();
    }

    public static class VelocityBINBuilder implements BuyItNowBuilder {

        private UUID id = UUID.randomUUID();
        private UUID lister;
        private JsonStoredEntry entry;
        private JsonStoredPrice price;
        private boolean purchased;
        private LocalDateTime expiration;

        private UUID purchaser;
        private boolean stashed;

        @Override
        public BuyItNowBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        @Override
        public BuyItNowBuilder lister(UUID lister) {
            this.lister = lister;
            return this;
        }

        @Override
        public BuyItNowBuilder entry(Entry<?, ?> entry) {
            Preconditions.checkArgument(entry instanceof JsonStoredEntry, "Mixing of incompatible platform types");
            this.entry = (JsonStoredEntry) entry;
            return this;
        }

        @Override
        public BuyItNowBuilder price(Price<?, ?, ?> price) {
            Preconditions.checkArgument(price instanceof JsonStoredPrice, "Mixing of incompatible platform types");
            this.price = (JsonStoredPrice) price;
            return this;
        }

        @Override
        public BuyItNowBuilder purchased() {
            this.purchased = true;
            return this;
        }

        @Override
        public BuyItNowBuilder purchaser(UUID purchaser) {
            this.purchaser = purchaser;
            return this;
        }

        @Override
        public BuyItNowBuilder stashedForPurchaser() {
            this.stashed = true;
            return this;
        }

        @Override
        public BuyItNowBuilder expiration(LocalDateTime expiration) {
            this.expiration = expiration;
            return this;
        }

        @Override
        public BuyItNowBuilder from(BuyItNow input) {
            return this.id(input.getID())
                    .lister(input.getLister())
                    .entry(input.getEntry())
                    .price(input.getPrice())
                    .expiration(input.getExpiration());
        }

        @Override
        public VelocityBIN build() {
            return new VelocityBIN(this);
        }
    }
}
