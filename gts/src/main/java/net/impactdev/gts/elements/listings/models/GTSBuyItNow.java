package net.impactdev.gts.elements.listings.models;

import net.impactdev.gts.api.elements.content.Price;
import net.impactdev.gts.api.elements.listings.models.BuyItNow;
import net.impactdev.gts.api.elements.listings.Listing;
import net.impactdev.impactor.api.items.ImpactorItemStack;
import net.impactdev.impactor.api.ui.containers.Icon;
import net.impactdev.impactor.api.utility.printing.PrettyPrinter;
import net.impactdev.json.JObject;
import net.kyori.adventure.text.Component;

public class GTSBuyItNow extends GTSListing implements BuyItNow {

    private final Price price;

    protected GTSBuyItNow(GTSBuyItNowBuilder builder) {
        super(builder);
        this.price = builder.price;
    }

    @Override
    public int version() {
        return 2;
    }

    @Override
    protected void serialize$child(JObject json) {
        json.add("price", this.price.serialize());
    }

    @Override
    public void print(PrettyPrinter printer) {
        super.print(printer);
    }

    @Override
    public Icon asIcon() {
        return Icon.builder()
                .display(() -> this.content().display())
                .append(Listing.class, this)
                .build();
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
