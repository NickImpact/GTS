package net.impactdev.gts.components;

import com.google.common.collect.TreeMultimap;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.components.content.Content;
import net.impactdev.gts.api.components.content.Price;
import net.impactdev.gts.api.components.listings.models.Auction;
import net.impactdev.gts.api.components.listings.models.Listing;
import net.impactdev.impactor.api.utilities.printing.PrettyPrinter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GTSAuction implements Auction {

    @Override
    public UUID id() {
        return null;
    }

    @Override
    public @NotNull Component asComponent() {
        return null;
    }

    @Override
    public Optional<UUID> lister() {
        return Optional.empty();
    }

    @Override
    public Content<?> content() {
        return null;
    }

    @Override
    public LocalDateTime published() {
        return null;
    }

    @Override
    public Optional<LocalDateTime> expiration() {
        return Optional.empty();
    }

    @Override
    public Price.Incremental starting() {
        return null;
    }

    @Override
    public Price.Incremental next() {
        return null;
    }

    @Override
    public Price.Incremental current() {
        return null;
    }

    @Override
    public TreeMultimap<UUID, Bid> bids() {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> bid(UUID bidder, BigDecimal amount) {
        return null;
    }

    @Override
    public int compareTo(@NotNull Listing o) {
        return 0;
    }

    @Override
    public JsonObject serialize() {
        return null;
    }

    @Override
    public int version() {
        return 0;
    }

    @Override
    public void print(PrettyPrinter printer) {

    }

    private Price.Incremental calculateIncrement() {
        Argument starting = new Argument("starting", this.starting().asNumber().doubleValue());
        Argument current = new Argument("current", this.current().asNumber().doubleValue());
        Expression expression = new Expression(""); // TODO - Access config for expression

        // TODO - Move this to initial config loading key to alert if plugin will have issues and should default
        expression.checkSyntax();

        return this.current().add(BigDecimal.valueOf(expression.calculate()));
    }
}
