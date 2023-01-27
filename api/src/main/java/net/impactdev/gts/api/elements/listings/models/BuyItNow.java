package net.impactdev.gts.api.elements.listings.models;

import net.impactdev.gts.api.elements.content.Price;
import net.impactdev.gts.api.elements.listings.Listing;
import net.impactdev.impactor.api.Impactor;

public interface BuyItNow extends Listing {

    static BuyItNowBuilder builder() {
        return Impactor.instance().builders().provide(BuyItNowBuilder.class);
    }

    interface BuyItNowBuilder extends ListingBuilder<BuyItNow, BuyItNowBuilder> {
        BuyItNowBuilder price(Price price);
    }

}
