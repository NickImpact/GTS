package net.impactdev.gts.sponge.stash;

import com.google.common.collect.Lists;
import net.impactdev.gts.api.deliveries.Delivery;
import net.impactdev.gts.api.stashes.StashedContent;
import net.impactdev.gts.sponge.pricing.provided.MonetaryPrice;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.stashes.Stash;
import net.kyori.adventure.util.TriState;

import java.util.List;
import java.util.UUID;

public class SpongeStash implements Stash {

    private final List<StashedContent<?>> stash;

    public SpongeStash(SpongeStashBuilder builder) {
        this.stash = builder.stash;
    }

    @Override
    public List<StashedContent<?>> getStashContents() {
        return this.stash;
    }

    @Override
    public boolean claim(UUID claimer, UUID listing) {
        StashedContent<?> content = this.stash.stream()
                .filter(x -> x.getID().equals(listing))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Stash claim attempt on missing data"));
        if(content instanceof StashedContent.ListingContent) {
            StashedContent.ListingContent data = (StashedContent.ListingContent) content;
            if (data.getContext() == TriState.TRUE) {
                if (data.getContent() instanceof Auction) {
                    Auction auction = (Auction) data.getContent();
                    MonetaryPrice value = new MonetaryPrice(auction.getCurrentPrice());
                    value.reward(claimer);

                    return true;
                }
            } else if (data.getContext() == TriState.FALSE) {
                return data.getContent().getEntry().give(claimer);
            } else {
                if (data.getContent() instanceof Auction) {
                    Auction auction = (Auction) data.getContent();
                    Auction.Bid bid = auction.getCurrentBid(claimer).orElseThrow(() -> new IllegalStateException("Unable to locate bid for user where required"));

                    MonetaryPrice value = new MonetaryPrice(bid.getAmount());
                    value.reward(claimer);

                    return true;
                }
            }
        } else {
            StashedContent.DeliverableContent delivery = (StashedContent.DeliverableContent) content;
            delivery.getContent().getContent().give(delivery.getContent().getRecipient());
            return true;
        }

        return false;
    }

    public static class SpongeStashBuilder implements StashBuilder {

        private final List<StashedContent<?>> stash = Lists.newArrayList();

        @Override
        public StashBuilder append(Listing listing, TriState context) {
            this.stash.add(new StashedContent.ListingContent(listing, context));
            return this;
        }

        @Override
        public StashBuilder append(Delivery delivery) {
            this.stash.add(new StashedContent.DeliverableContent(delivery, TriState.NOT_SET));
            return this;
        }

//        @Override
//        public StashBuilder from(Stash stash) {
//            this.stash.addAll(stash.getStashContents());
//            return this;
//        }

        @Override
        public Stash build() {
            return new SpongeStash(this);
        }
    }

}
