package net.impactdev.gts.elements.listings.models;

import com.google.gson.JsonObject;
import net.impactdev.gts.api.elements.content.Price;
import net.impactdev.gts.api.elements.listings.deserialization.DeserializationContext;
import net.impactdev.gts.api.elements.listings.deserialization.DeserializationKeys;
import net.impactdev.gts.api.elements.listings.models.BuyItNow;
import net.impactdev.gts.api.elements.listings.Listing;
import net.impactdev.gts.util.GTSKeys;
import net.impactdev.impactor.api.items.ImpactorItemStack;
import net.impactdev.impactor.api.ui.containers.Icon;
import net.impactdev.impactor.api.utility.printing.PrettyPrinter;
import net.impactdev.json.JObject;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

import java.time.LocalDateTime;
import java.util.UUID;

public class GTSBuyItNow extends GTSListing implements BuyItNow {

    public static final Key KEY = GTSKeys.gts("bin");
    private final Price price;

    protected GTSBuyItNow(GTSBuyItNowBuilder builder) {
        super(builder);
        this.price = builder.price;
    }

    @Override
    public Icon asIcon() {
        return Icon.builder()
                .display(() -> this.content().display())
                .append(Listing.class, this)
                .build();
    }

    @Override
    public int version() {
        return 2;
    }

    @Override
    protected Key serialize$key() {
        return KEY;
    }

    @Override
    protected void serialize$child(JObject json) {
        json.add("price", this.price.serialize());
    }

    @Override
    public void print(PrettyPrinter printer) {
        super.print(printer);
    }

    public static BuyItNow deserialize(JsonObject json) {
        DeserializationContext context = GTSListing.contextualize(json);
        BuyItNow.BuyItNowBuilder builder = BuyItNow.builder()
                .id(context.obtain(DeserializationKeys.UUID))
                .lister(context.obtain(DeserializationKeys.LISTER))
                .content(context.obtain(DeserializationKeys.CONTENT))
                .published(context.obtain(DeserializationKeys.PUBLISHED_TIME))
                .expiration(context.obtain(DeserializationKeys.EXPIRATION_TIME));

        return builder.build();
    }

    public static class GTSBuyItNowBuilder extends GTSListingBuilder<BuyItNow, BuyItNowBuilder> implements BuyItNowBuilder {

        private Price price;

        @Override
        public BuyItNowBuilder price(Price price) {
            this.price = price;
            return this;
        }

        @Override
        public BuyItNow build() {
            return new GTSBuyItNow(this);
        }
    }
}
