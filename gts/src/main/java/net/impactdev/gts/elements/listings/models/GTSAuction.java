package net.impactdev.gts.elements.listings.models;

import com.google.common.collect.TreeMultimap;
import net.impactdev.gts.api.elements.content.Price;
import net.impactdev.gts.api.elements.listings.models.Auction;
import net.impactdev.impactor.api.ui.containers.Icon;
import net.impactdev.impactor.api.utility.printing.PrettyPrinter;
import net.impactdev.json.JArray;
import net.impactdev.json.JObject;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GTSAuction extends GTSListing implements Auction {

    protected GTSAuction(GTSListingBuilder<?> builder) {
        super(builder);
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
    public int version() {
        return 2;
    }

    @Override
    protected void serialize$child(JObject json) {
        JObject auction = new JObject();
        auction.add("start", this.starting().asNumber());
        auction.add("current", this.current().asNumber());
        auction.add("bids", new JArray().consume(array -> {
            for(Map.Entry<UUID, Bid> bid : this.bids().entries()) {
                JObject metadata = new JObject();
                metadata.add("amount", bid.getValue().amount().asNumber());
                metadata.add("timestamp", bid.getValue().timestamp().toString());

                array.add(new JObject().add(bid.getKey().toString(), metadata));
            }
        }));
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

    @Override
    public Icon asIcon() {
        return null;
    }
}
