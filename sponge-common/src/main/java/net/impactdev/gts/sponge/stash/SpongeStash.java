package net.impactdev.gts.sponge.stash;

import com.google.common.collect.Lists;
import net.impactdev.gts.api.util.TriState;
import net.impactdev.gts.sponge.pricing.provided.MonetaryPrice;
import net.impactdev.impactor.api.utilities.mappings.Tuple;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.stashes.Stash;

import java.util.List;
import java.util.UUID;

public class SpongeStash implements Stash {

    private final List<Tuple<Listing, TriState>> stash;

    public SpongeStash(SpongeStashBuilder builder) {
        this.stash = builder.stash;
    }

    @Override
    public List<Tuple<Listing, TriState>> getStashContents() {
        return this.stash;
    }

    @Override
    public boolean claim(UUID claimer, UUID listing) {
        Tuple<Listing, TriState> data = this.stash.stream().filter(x -> x.getFirst().getID().equals(listing)).findAny().orElseThrow(() -> new IllegalStateException("Stash claim attempt on missing Listing data"));
        if(data.getSecond() == TriState.TRUE) {
            if(data.getFirst() instanceof Auction) {
                Auction auction = (Auction) data.getFirst();
                MonetaryPrice value = new MonetaryPrice(auction.getCurrentPrice());
                value.reward(claimer);

                return true;
            }
        } else if(data.getSecond() == TriState.FALSE) {
            return data.getFirst().getEntry().give(claimer);
        } else {
            if(data.getFirst() instanceof Auction) {
                Auction auction = (Auction) data.getFirst();
                Auction.Bid bid = auction.getCurrentBid(claimer).orElseThrow(() -> new IllegalStateException("Unable to locate bid for user where required"));

                MonetaryPrice value = new MonetaryPrice(bid.getAmount());
                value.reward(claimer);

                return true;
            }
        }

        return false;
    }

    public static class SpongeStashBuilder implements StashBuilder {

        private final List<Tuple<Listing, TriState>> stash = Lists.newArrayList();

        @Override
        public StashBuilder append(Listing listing, TriState context) {
            this.stash.add(new Tuple<>(listing, context));
            return this;
        }

        @Override
        public StashBuilder from(Stash stash) {
            this.stash.addAll(stash.getStashContents());
            return this;
        }

        @Override
        public Stash build() {
            return new SpongeStash(this);
        }
    }

}
