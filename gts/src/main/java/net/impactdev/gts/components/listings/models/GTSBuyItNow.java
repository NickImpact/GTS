package net.impactdev.gts.components.listings.models;

import com.google.gson.JsonObject;
import net.impactdev.gts.api.components.content.Price;
import net.impactdev.gts.api.components.listings.models.BuyItNow;
import net.impactdev.gts.api.components.listings.models.Listing;
import net.impactdev.impactor.api.utilities.printing.PrettyPrinter;
import org.jetbrains.annotations.NotNull;

public class GTSBuyItNow extends GTSListing implements BuyItNow {

    private final Price price;

    protected GTSBuyItNow(GTSBuyItNowBuilder builder) {
        super(builder);
        this.price = builder.price;
    }

    @Override
    public int compareTo(@NotNull Listing o) {
        return 0;
    }

    @Override
    public int version() {
        return 2;
    }

    @Override
    public JsonObject serialize() {
        return null;
    }

    @Override
    public void print(PrettyPrinter printer) {

    }

    public static class GTSBuyItNowBuilder extends GTSListingBuilder<GTSBuyItNowBuilder> {

        private Price price;

        public GTSBuyItNowBuilder price(Price price) {
            this.price = price;
            return this;
        }

        @Override
        public Listing build() {
            return new GTSBuyItNow(this);
        }
    }
}
