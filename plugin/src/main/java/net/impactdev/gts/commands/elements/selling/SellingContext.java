package net.impactdev.gts.commands.elements.selling;

import net.impactdev.gts.api.commands.CommandGenerator;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.listings.ui.EntrySelection;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;

import java.util.Optional;

public class SellingContext implements CommandGenerator.Context {

    private EntrySelection<?> entry;
    private long time = Long.MIN_VALUE;
    private boolean redirect;

    @Override
    public Class<? extends Listing> type() {
        return BuyItNow.class;
    }

    @Override
    public Optional<EntrySelection<?>> entry() {
        return Optional.ofNullable(this.entry);
    }

    @Override
    public void entry(EntrySelection<?> entry) {
        this.entry = entry;
    }

    @Override
    public long time() {
        return this.time != Long.MIN_VALUE ? this.time : GTSPlugin.instance().configuration().main().get(ConfigKeys.LISTING_TIME_MID).getTime();
    }

    @Override
    public void time(long time) {
        this.time = time;
    }

    @Override
    public boolean redirect() {
        return this.redirect;
    }

    @Override
    public void redirected() {
        this.redirect = true;
    }

    public static class SellingAuctionContext extends SellingContext implements CommandGenerator.Context.AuctionContext {

        private double start;
        private float increment;

        @Override
        public Class<? extends Listing> type() {
            return Auction.class;
        }

        @Override
        public double start() {
            return this.start;
        }

        @Override
        public void start(double start) {
            this.start = start;
        }

        @Override
        public float increment() {
            return this.increment;
        }

        @Override
        public void increment(float increment) {
            this.increment = increment;
        }
    }
}
