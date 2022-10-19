package net.impactdev.gts.components.listings;

import net.impactdev.gts.api.components.listings.ListingManager;
import net.impactdev.gts.api.components.listings.models.Auction;
import net.impactdev.gts.api.components.listings.models.BuyItNow;
import net.impactdev.gts.api.components.listings.models.Listing;
import net.impactdev.gts.locale.Messages;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.utilities.context.Context;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class GTSListingManager implements ListingManager {
    @Override
    public CompletableFuture<Set<Listing>> listings(Predicate<Listing> filter) {
        return null;
    }

    @Override
    public CompletableFuture<TriState> list(UUID actor, Listing listing) {
        PlatformPlayer lister = PlatformPlayer.getOrCreate(actor);
        Context context = Context.empty();
        context.append(PlatformPlayer.class, lister);

        if(listing instanceof BuyItNow) {
            Messages.PUBLISH_BIN_LISTING.send(lister, context.append(Listing.class, listing));
        } else {

        }

        return null;
    }

    @Override
    public CompletableFuture<TriState> purchase(UUID actor, BuyItNow listing, @Nullable Object selection) {
        return null;
    }

    @Override
    public CompletableFuture<TriState> bid(UUID actor, Auction auction, BigDecimal amount) {
        return null;
    }

    @Override
    public CompletableFuture<TriState> remove(UUID actor, UUID listing) {
        return null;
    }

    @Override
    public CompletableFuture<TriState> hasMax(UUID target) {
        return null;
    }
}
