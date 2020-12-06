package net.impactdev.gts.sponge.stash;

import com.google.common.collect.Lists;
import net.impactdev.gts.sponge.pricing.provided.MonetaryPrice;
import net.impactdev.impactor.api.utilities.mappings.Tuple;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.stashes.Stash;

import java.util.List;
import java.util.UUID;

public class SpongeStash implements Stash {

    private final List<Tuple<Listing, Boolean>> stash;

    public SpongeStash(SpongeStashBuilder builder) {
        this.stash = builder.stash;
    }

    @Override
    public List<Tuple<Listing, Boolean>> getStashContents() {
        return this.stash;
    }

    @Override
    public boolean claim(UUID claimer, UUID listing) {
        Tuple<Listing, Boolean> data = this.stash.stream().filter(x -> x.getFirst().getID().equals(listing)).findAny().orElseThrow(() -> new IllegalStateException("Stash claim attempt on missing Listing data"));
        if(data.getSecond()) {
            if(data.getFirst() instanceof Auction) {
                Auction auction = (Auction) data.getFirst();
                MonetaryPrice value = new MonetaryPrice(auction.getCurrentPrice());
                value.reward(claimer);

                return true;
            }
        } else {
            return data.getFirst().getEntry().give(claimer);
        }

        return false;
    }

    public static class SpongeStashBuilder implements StashBuilder {

        private final List<Tuple<Listing, Boolean>> stash = Lists.newArrayList();

        @Override
        public StashBuilder append(Listing listing, boolean purchased) {
            this.stash.add(new Tuple<>(listing, purchased));
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
