package net.impactdev.gts.bungee.listings;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.common.listings.JsonStoredEntry;
import net.impactdev.impactor.api.json.factory.JArray;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.impactor.api.utilities.mappings.Tuple;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class BungeeAuction extends BungeeListing implements Auction {

    /** The starting price of this auction */
    private final double start;

    /** The current price of this auction, representing the start or highest bid */
    private double price;

    /** The base increment percentage required to place a bid */
    private final float increment;

    private final TreeMultimap<UUID, Bid> bids = TreeMultimap.create(
            Comparator.naturalOrder(),
            Collections.reverseOrder(Comparator.comparing(Bid::getAmount))
    );

    public BungeeAuction(BungeeAuctionBuilder builder) {
        super(builder.id, builder.lister, builder.entry, builder.published, builder.expiration);
        this.start = builder.start;
        this.price = Math.max(builder.current, this.start);
        this.increment = builder.increment;
        if(builder.bids != null) {
            builder.bids.forEach(this.bids::put);
        }
    }

    @Override
    public Optional<Bid> getCurrentBid(UUID uuid) {
        return this.getHighBid().map(Tuple::getSecond);
    }

    @Override
    public TreeMultimap<UUID, Bid> getBids() {
        return this.bids;
    }

    @Override
    public Map<UUID, Bid> getUniqueBiddersWithHighestBids() {
        Map<UUID, Bid> unique = Maps.newHashMap();
        for(UUID uuid : new ArrayList<>(this.getBids().keys())) {
            unique.put(uuid, this.getBids().get(uuid).first());
        }

        return unique;
    }

    @Override
    public Optional<Tuple<UUID, Bid>> getHighBid() {
        return this.bids.entries().stream()
                .max(Comparator.comparing(value -> value.getValue().getAmount()))
                .map(e -> new Tuple<>(e.getKey(), e.getValue()));
    }

    @Override
    public double getStartingPrice() {
        return this.start;
    }

    @Override
    public double getCurrentPrice() {
        return this.price;
    }

    @Override
    public float getIncrement() {
        return this.increment;
    }

    @Override
    public double getNextBidRequirement() {
        double result;
        if(this.getBids().size() == 0) {
            result = this.getStartingPrice();
        } else {
            result = this.getCurrentPrice() * (1.0 + this.getIncrement());
        }

        return result;
    }

    @Override
    public boolean bid(UUID user, double amount) {
        if(this.bids.size() == 0 || (amount >= this.getHighBid().get().getSecond().getAmount() * (1.0 + this.getIncrement()))) {
            this.getBids().put(user, new Bid(amount));
            this.price = amount;
            return true;
        }
        return false;
    }

    @Override
    public JObject serialize() {
        JObject json = super.serialize();

        JObject bids = new JObject();

        for(UUID id : this.bids.keys()) {
            JArray array = new JArray();

            for(Bid bid : this.bids.get(id).stream().sorted(Collections.reverseOrder(Comparator.comparing(Bid::getAmount))).collect(Collectors.toList())) {
                array.add(bid.serialize());
            }
            bids.add(id.toString(), array);
        }

        JObject pricing = new JObject()
                .add("start", this.getStartingPrice())
                .add("current", this.getCurrentPrice())
                .add("increment", this.getIncrement());

        json.add("auction", new JObject()
                .add("bids", bids)
                .add("pricing", pricing)
        );
        json.add("type", "auction");

        return json;
    }

    public static BungeeAuction deserialize(JsonObject object) {
        BungeeAuctionBuilder builder = (BungeeAuctionBuilder) Auction.builder()
                .id(UUID.fromString(object.get("id").getAsString()))
                .lister(UUID.fromString(object.get("lister").getAsString()))
                .published(LocalDateTime.parse(object.getAsJsonObject("timings").get("published").getAsString()))
                .expiration(LocalDateTime.parse(object.getAsJsonObject("timings").get("expiration").getAsString()))
                .start(object.getAsJsonObject("auction").getAsJsonObject("pricing").get("start").getAsDouble())
                .current(object.getAsJsonObject("auction").getAsJsonObject("pricing").get("current").getAsDouble())
                .increment(object.getAsJsonObject("auction").getAsJsonObject("pricing").get("increment").getAsFloat());

        JsonObject element = object.getAsJsonObject("entry");
        builder.entry(new JsonStoredEntry(element));

        JsonObject bids = object.getAsJsonObject("auction").getAsJsonObject("bids");
        Multimap<UUID, Bid> mapping = ArrayListMultimap.create();
        for(Map.Entry<String, JsonElement> entry : bids.entrySet()) {
            if(entry.getValue().isJsonArray()) {
                entry.getValue().getAsJsonArray().forEach(e -> {
                    JsonObject data = e.getAsJsonObject();
                    Bid bid = Bid.builder()
                            .amount(data.get("amount").getAsDouble())
                            .timestamp(LocalDateTime.parse(data.get("timestamp").getAsString()))
                            .build();

                    mapping.put(UUID.fromString(entry.getKey()), bid);
                });
            }
        }

        builder.bids(mapping);
        return builder.build();
    }

    public static class BungeeAuctionBuilder implements AuctionBuilder {

        private UUID id = UUID.randomUUID();
        private UUID lister;
        private JsonStoredEntry entry;
        private LocalDateTime published = LocalDateTime.now();
        private LocalDateTime expiration;
        private double start;
        private float increment;
        private double current;
        private Multimap<UUID, Bid> bids;

        @Override
        public AuctionBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        @Override
        public AuctionBuilder lister(UUID lister) {
            this.lister = lister;
            return this;
        }

        @Override
        public AuctionBuilder entry(Entry<?, ?> entry) {
            Preconditions.checkArgument(entry instanceof JsonStoredEntry, "Mixing of invalid types!");
            this.entry = (JsonStoredEntry) entry;
            return this;
        }

        @Override
        public AuctionBuilder published(LocalDateTime published) {
            this.published = published;
            return this;
        }

        @Override
        public AuctionBuilder expiration(LocalDateTime expiration) {
            this.expiration = expiration;
            return this;
        }

        @Override
        public AuctionBuilder start(double amount) {
            this.start = amount;
            return this;
        }

        @Override
        public AuctionBuilder increment(float rate) {
            this.increment = rate;
            return this;
        }

        @Override
        public AuctionBuilder current(double current) {
            this.current = current;
            return this;
        }

        @Override
        public AuctionBuilder bids(Multimap<UUID, Bid> bids) {
            this.bids = bids;
            return this;
        }

        @Override
        public AuctionBuilder from(Auction input) {
            Preconditions.checkArgument(input instanceof BungeeAuction, "Mixing of invalid types!");
            this.id = input.getID();
            this.lister = input.getLister();
            this.entry = (JsonStoredEntry) input.getEntry();
            this.published = input.getPublishTime();
            this.expiration = input.getExpiration();
            this.start = input.getStartingPrice();
            this.increment = input.getIncrement();
            this.current = input.getCurrentPrice();

            return this;
        }

        @Override
        public BungeeAuction build() {
            return new BungeeAuction(this);
        }
    }
}
