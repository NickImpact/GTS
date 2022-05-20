/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package net.impactdev.gts.common.storage.implementation.file;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.TreeMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.leangen.geantyref.TypeToken;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.data.Storable;
import net.impactdev.gts.api.deliveries.Delivery;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.communication.message.errors.ErrorCode;
import net.impactdev.gts.api.communication.message.errors.ErrorCodes;
import net.impactdev.gts.api.communication.message.exceptions.MessagingException;
import net.impactdev.gts.api.communication.message.type.admin.ForceDeleteMessage;
import net.impactdev.gts.api.communication.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.communication.message.type.deliveries.ClaimDelivery;
import net.impactdev.gts.api.communication.message.type.listings.BuyItNowMessage;
import net.impactdev.gts.api.communication.message.type.listings.ClaimMessage;
import net.impactdev.gts.api.player.NotificationSetting;
import net.impactdev.gts.api.player.PlayerSettings;
import net.impactdev.gts.api.stashes.Stash;
import net.impactdev.gts.api.util.groupings.SimilarPair;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.messaging.messages.deliveries.ClaimDeliveryImpl;
import net.impactdev.gts.common.messaging.messages.listings.ClaimMessageImpl;
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionBidMessage;
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionCancelMessage;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.purchase.BINPurchaseMessage;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.removal.BINRemoveMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.storage.implementation.StorageImplementation;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.impactor.api.json.factory.JArray;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.impactor.api.storage.file.loaders.ConfigurateLoader;
import net.kyori.adventure.util.TriState;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class ConfigurateStorage implements StorageImplementation {

    private final GTSPlugin plugin;
    private final String implementationName;

    // The loader responsible for I/O
    private final ConfigurateLoader loader;
    private String extension;
    private String dataDirName;
    private Map<Group, Path> fileGroups;

    private enum Group {
        USERS,
        LISTINGS,
        DELIVERY,
        CLAIMS,
    }

    private final LoadingCache<Path, ReentrantLock> ioLocks;

    public ConfigurateStorage(GTSPlugin plugin, String implementationName, ConfigurateLoader loader, String extension, String dataDirName) {
        this.plugin = plugin;
        this.implementationName = implementationName;
        this.loader = loader;
        this.extension = extension;
        this.dataDirName = dataDirName;

        this.ioLocks = Caffeine.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(key -> new ReentrantLock());
    }

    @Override
    public GTSPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public String getName() {
        return this.implementationName;
    }

    @Override
    public void init() throws Exception {
        Path dataDir = this.getResourcePath();
        this.createDirectoriesIfNotExists(dataDir);

        Path users = dataDir.resolve(this.dataDirName).resolve("users");
        Path deliveries = dataDir.resolve(this.dataDirName).resolve("deliveries");
        Path listings = dataDir.resolve(this.dataDirName).resolve("listings");
        Path claims = dataDir.resolve(this.dataDirName).resolve("claims");

        EnumMap<Group, Path> fileGroups = new EnumMap<>(Group.class);
        fileGroups.put(Group.USERS, users);
        fileGroups.put(Group.LISTINGS, listings);
        fileGroups.put(Group.DELIVERY, deliveries);
        fileGroups.put(Group.CLAIMS, claims);
        this.fileGroups = ImmutableMap.copyOf(fileGroups);
    }

    protected Path getResourcePath() {
        return Paths.get("gts");
    }

    @Override
    public void shutdown() throws Exception {}

    @Override
    public Map<String, String> getMeta() {
        // TODO - File statistics
        return StorageImplementation.super.getMeta();
    }

    @Override
    public boolean addListing(Listing listing) throws Exception {
        ConfigurationNode file = BasicConfigurationNode.root();

        for(Map.Entry<String, JsonElement> entry : listing.serialize().toJson().entrySet()) {
            this.writePath(file, entry.getKey(), entry.getValue());
        }

        this.saveFile(Group.LISTINGS, listing.getID(), file);
        return true;
    }

    @Override
    public boolean deleteListing(UUID uuid) throws Exception {
        this.saveFile(Group.LISTINGS, uuid, null);
        return true;
    }

    @Override
    public Optional<Listing> getListing(UUID id) throws Exception {
        return Optional.ofNullable(this.from(this.readFile(Group.LISTINGS, id)));
    }

    @Override
    public List<Listing> getListings() throws Exception {
        List<Listing> output = Lists.newArrayList();

        Path parent = this.fileGroups.get(Group.LISTINGS);
        File[] categories = parent.toFile().listFiles(((dir, name) -> dir.isDirectory()));
        if(categories == null) {
            return output;
        }

        for(File category : categories) {
            for(File data : category.listFiles(((dir, name) -> name.endsWith(this.extension)))) {
                output.add(this.from(this.readFile(Group.LISTINGS, UUID.fromString(data.getName().split("[.]")[0]))));
            }
        }

        return output;
    }

    @Override
    public boolean hasMaxListings(UUID user) throws Exception {
        return this.getListings().stream().filter(listing -> listing.getLister().equals(user)).count() >=
                GTSPlugin.instance().configuration().main().get(ConfigKeys.MAX_LISTINGS_PER_USER);
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean purge() throws Exception {
        Path root = this.getResourcePath();
        try(Stream<Path> walker = Files.walk(root)) {
            walker.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            return true;
        }
    }

    @Override
    public boolean clean() throws Exception {
        return true;
    }

    @Override
    public boolean sendDelivery(Delivery delivery) throws Exception {
        ConfigurationNode file = BasicConfigurationNode.root();

        for(Map.Entry<String, JsonElement> entry : delivery.serialize().toJson().entrySet()) {
            this.writePath(file, entry.getKey(), entry.getValue());
        }

        UUID target = delivery.getRecipient();
        Path path = this.fileGroups.get(Group.USERS).resolve(target.toString().substring(0, 2)).resolve(target.toString()).resolve("delivery_" + delivery.getID() + this.extension);
        this.saveFile(path, file);
        return true;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public Stash getStash(UUID user) throws Exception {
        Stash.StashBuilder builder = Stash.builder();

        List<Listing> listings = this.getListings();
        for(Listing listing : listings) {
            if(listing.hasExpired() || (listing instanceof BuyItNow && ((BuyItNow) listing).isPurchased())) {
                if (listing instanceof Auction) {
                    Auction auction = (Auction) listing;
                    if(auction.getLister().equals(user) || auction.getHighBid().map(bid -> bid.getFirst().equals(user)).orElse(false)) {
                        boolean state = auction.getLister().equals(user);
                        ConfigurationNode file = this.readFile(Group.CLAIMS, auction.getID());
                        if(file != null) {
                            SimilarPair<Boolean> claimed = new SimilarPair<>(
                                    file.node("lister").getBoolean(),
                                    file.node("winner").getBoolean()
                            );

                            if(state && !claimed.getFirst()) {
                                builder.append(auction, TriState.FALSE);
                            } else if(!state && !claimed.getSecond()) {
                                builder.append(auction, TriState.TRUE);
                            }
                        } else {
                            if(state) {
                                builder.append(auction, TriState.FALSE);
                            } else {
                                builder.append(auction, TriState.TRUE);
                            }
                        }
                    } else if(auction.getBids().containsKey(user)) {
                        Optional.ofNullable(this.readFile(Group.CLAIMS, auction.getID()))
                                .ifPresent(node -> {
                                    try {
                                        node.node("others")
                                                .getList(TypeToken.get(UUID.class))
                                                .stream()
                                                .filter(claimer -> claimer.equals(user))
                                                .findAny()
                                                .ifPresent(x -> {
                                                    builder.append(auction, TriState.NOT_SET);
                                                });
                                    } catch (SerializationException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                    }
                } else {
                    BuyItNow bin = (BuyItNow) listing;
                    if(bin.getLister().equals(user) && !bin.stashedForPurchaser()) {
                        builder.append(bin, TriState.FALSE);
                    } else if(bin.stashedForPurchaser()) {
                        if(bin.purchaser().equals(user)) {
                            builder.append(bin, TriState.TRUE);
                        }
                    }
                }
            }
        }

        File additional = this.fileGroups.get(Group.USERS).resolve(user.toString().substring(0, 2)).resolve(user.toString()).toFile();
        Storable.Deserializer<Delivery> deserializer = GTSService.getInstance().getGTSComponentManager().getDeliveryDeserializer();
        this.createDirectoriesIfNotExists(additional.toPath());
        for(File file : additional.listFiles((d, n) -> n.startsWith("delivery_"))) {
            ConfigurationNode node = this.readFile(file.toPath());

            JObject json = new JObject();
            this.fill(json, node, true);
            JsonObject result = json.toJson();
            builder.append(deserializer.deserialize(result));
        }

        return builder.build();
    }

    @Override
    public Optional<PlayerSettings> getPlayerSettings(UUID user) throws Exception {
        Optional<ConfigurationNode> result = Optional.ofNullable(this.readFile(Group.USERS, user));
        return result.map(node -> {
            try {
                JObject json = new JObject();
                this.fill(json, node, true);
                return json.toJson();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).map(json -> PlayerSettings.builder()
                .set(NotificationSetting.Bid, json.get("bids").getAsBoolean())
                .set(NotificationSetting.Publish, json.get("publish").getAsBoolean())
                .set(NotificationSetting.Sold, json.get("sold").getAsBoolean())
                .set(NotificationSetting.Outbid, json.get("outbid").getAsBoolean())
                .build()
        );
    }

    @Override
    public boolean applyPlayerSettings(UUID user, PlayerSettings updates) throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("bids", updates.getBidListenState());
        json.addProperty("publish", updates.getPublishListenState());
        json.addProperty("sold", updates.getSoldListenState());
        json.addProperty("output", updates.getOutbidListenState());

        ConfigurationNode node = BasicConfigurationNode.root();
        for(Map.Entry<String, JsonElement> entry : json.entrySet()) {
            this.writePath(node, entry.getKey(), entry.getValue());
        }
        this.saveFile(Group.USERS, user, node);
        return false;
    }

    @Override
    public BuyItNowMessage.Purchase.Response processPurchase(BuyItNowMessage.Purchase.Request request) throws Exception {
        return this.getListing(request.getListingID())
                .map(listing -> {
                    if(!(listing instanceof BuyItNow)) {
                        throw new IllegalStateException("Can't purchase a non-purchasable listing!");
                    }

                    BuyItNow bin = (BuyItNow) listing;
                    UUID seller = bin.getLister();
                    boolean successful = !bin.isPurchased();

                    if(successful) {
                        bin.markPurchased();
                        try {
                            this.sendListingUpdate(bin);
                        } catch (Exception e) {
                            throw new MessagingException(ErrorCodes.FATAL_ERROR, e);
                        }
                    }

                    return new BINPurchaseMessage.Response(
                            GTSPlugin.instance().messagingService().generatePingID(),
                            request.getID(),
                            request.getListingID(),
                            request.getActor(),
                            seller,
                            successful,
                            successful ? null : ErrorCodes.ALREADY_PURCHASED
                    );
                })
                .orElseThrow(() -> new MessagingException(ErrorCodes.LISTING_MISSING));
    }

    @Override
    public boolean sendListingUpdate(Listing listing) throws Exception {
        return this.addListing(listing);
    }

    @Override
    public AuctionMessage.Bid.Response processBid(AuctionMessage.Bid.Request request) throws Exception {
        return this.getListing(request.getAuctionID())
                .map(listing -> {
                    if(!(listing instanceof Auction)) {
                        throw new IllegalStateException("Can't bid on a non-auction!");
                    }

                    Auction auction = (Auction) listing;
                    UUID seller = auction.getLister();
                    TriState successful = auction.bid(request.getActor(), request.getAmountBid()) ? TriState.TRUE : TriState.NOT_SET;
                    TreeMultimap<UUID, Auction.Bid> bids = auction.getBids();

                    boolean sniped = false;
                    boolean protect = GTSPlugin.instance().configuration().main().get(ConfigKeys.AUCTIONS_SNIPING_BIDS_ENABLED);
                    long snipingTime = GTSPlugin.instance().configuration().main().get(ConfigKeys.AUCTIONS_MINIMUM_SNIPING_TIME).getTime();
                    long timeDifference = ChronoUnit.SECONDS.between(LocalDateTime.now(), auction.getExpiration());
                    if(protect && snipingTime >= timeDifference) {
                        auction.setExpiration(LocalDateTime.now().plusSeconds(GTSPlugin.instance().configuration().main().get(ConfigKeys.AUCTIONS_SET_TIME).getTime()));
                        sniped = true;
                    }

                    try {
                        if(!this.sendListingUpdate(auction)) {
                            successful = TriState.FALSE;
                        }
                    } catch (Exception e) {
                        throw new MessagingException(ErrorCodes.FATAL_ERROR, e);
                    }

                    return new AuctionBidMessage.Response(
                            GTSPlugin.instance().messagingService().generatePingID(),
                            request.getID(),
                            request.getAuctionID(),
                            request.getActor(),
                            request.getAmountBid(),
                            successful.toBooleanOrElse(false),
                            sniped,
                            seller,
                            bids,
                            successful == TriState.NOT_SET ? ErrorCodes.OUTBID : successful == TriState.FALSE ?
                                    ErrorCodes.FATAL_ERROR : null
                    );
                })
                .orElseThrow(() -> new MessagingException(ErrorCodes.LISTING_MISSING));
    }

    @Override
    public ClaimMessage.Response processClaimRequest(ClaimMessage.Request request) throws Exception {
        Optional<Listing> listing = this.getListing(request.getListingID());
        UUID code = GTSPlugin.instance().messagingService().generatePingID();
        if(!listing.isPresent()) {
            return ClaimMessageImpl.ClaimResponseImpl.builder()
                    .id(code)
                    .request(request.getID())
                    .listing(request.getListingID())
                    .actor(request.getActor())
                    .receiver(request.getReceiver().orElse(null))
                    .error(ErrorCodes.LISTING_MISSING)
                    .build();
        } else {
            boolean owner = request.getActor().equals(listing.get().getLister());
            if(listing.map(l -> l instanceof Auction).orElse(false)) {
                if(!listing.map(l -> (Auction) l).get().hasAnyBidsPlaced()) {
                    ClaimMessageImpl.ClaimResponseImpl.ClaimResponseBuilder builder = ClaimMessageImpl.ClaimResponseImpl.builder()
                            .id(code)
                            .request(request.getID())
                            .listing(request.getListingID())
                            .actor(request.getActor())
                            .successful()
                            .auction()
                            .winner(false)
                            .lister(false)
                            .receiver(request.getReceiver().orElse(null));

                    if(this.deleteListing(request.getListingID())) {
                        builder.successful();
                    }

                    return builder.build();
                }

                Auction auction = listing.map(l -> (Auction) l).get();
                boolean claimer = owner || auction.getHighBid().get().getFirst().equals(request.getActor());

                ConfigurationNode status = this.readFile(Group.CLAIMS, auction.getID());
                if(status != null) {
                    boolean lister = status.node("lister").getBoolean();
                    boolean winner = status.node("winner").getBoolean();
                    List<UUID> others = status.node("others").getList(TypeToken.get(UUID.class));

                    if(claimer) {
                        if(owner) {
                            lister = true;
                        } else {
                            winner = true;
                        }
                    } else {
                        others.add(request.getActor());
                    }

                    boolean all = lister && winner && auction.getBids().keySet().stream()
                            .filter(bidder -> !auction.getHighBid().get().getFirst().equals(bidder))
                            .allMatch(others::contains);

                    if(all) {
                        this.deleteListing(auction.getID());
                        this.saveFile(Group.CLAIMS, auction.getID(), null);
                    } else {
                        status.node("lister").set(lister);
                        status.node("winner").set(winner);
                        status.node("others").set(new TypeToken<List<UUID>>() {}, others);
                        this.saveFile(Group.CLAIMS, auction.getID(), status);
                    }
                } else {
                    status = BasicConfigurationNode.root();
                    List<UUID> others = Lists.newArrayList();
                    if(!claimer) {
                        others.add(request.getActor());
                    }

                    status.node("lister").set(owner);
                    status.node("winner").set(!owner && claimer);
                    status.node("others").set(new TypeToken<List<UUID>>() {}, others);
                }

                ImmutableList<UUID> o = ImmutableList.copyOf(status.node("others").get(new TypeToken<List<UUID>>(){}));
                Map<UUID, Boolean> claimed = Maps.newHashMap();
                auction.getBids().keySet().stream()
                        .filter(bidder -> !auction.getHighBid().get().getFirst().equals(bidder))
                        .forEach(bidder -> {
                            claimed.put(bidder, o.contains(bidder));
                        });

                return ClaimMessageImpl.ClaimResponseImpl.builder()
                        .id(code)
                        .request(request.getID())
                        .listing(request.getListingID())
                        .actor(request.getActor())
                        .receiver(request.getReceiver().orElse(null))
                        .successful()
                        .auction()
                        .lister(status.node("lister").getBoolean())
                        .winner(status.node("winner").getBoolean())
                        .others(claimed)
                        .successful()
                        .build();
            } else {
                ClaimMessageImpl.ClaimResponseImpl.ClaimResponseBuilder builder = ClaimMessageImpl.ClaimResponseImpl.builder()
                        .id(code)
                        .request(request.getID())
                        .listing(request.getListingID())
                        .actor(request.getActor())
                        .receiver(request.getReceiver().orElse(null));

                if(this.deleteListing(request.getListingID())) {
                    builder.successful();
                }

                return builder.build();
            }
        }
    }

    @Override
    public boolean appendOldClaimStatus(UUID auction, boolean lister, boolean winner, List<UUID> others) throws Exception {
        ConfigurationNode node = BasicConfigurationNode.root();
        node.node("lister").set(lister);
        node.node("winner").set(winner);
        node.node("others").set(new TypeToken<List<UUID>>() {}, others);
        this.saveFile(Group.CLAIMS, auction, node);
        return true;
    }

    @Override
    public AuctionMessage.Cancel.Response processAuctionCancelRequest(AuctionMessage.Cancel.Request request) throws Exception {
        Optional<Listing> listing = this.getListing(request.getAuctionID());
        if(listing.isPresent()) {
            Auction auction = listing.filter(l -> l instanceof Auction).map(l -> (Auction) l).orElseThrow(() -> new MessagingException(ErrorCodes.FATAL_ERROR));

            List<UUID> bidders = Lists.newArrayList();
            boolean result = false;
            ErrorCode error = null;
            if(this.plugin.configuration().main().get(ConfigKeys.AUCTIONS_ALLOW_CANCEL_WITH_BIDS)) {
                auction.getBids().keySet().stream().distinct().forEach(bidders::add);

                result = this.deleteListing(auction.getID());

            } else {
                if(auction.hasAnyBidsPlaced()) {
                    error = ErrorCodes.BIDS_PLACED;
                } else {
                    result = this.deleteListing(auction.getID());
                }
            }

            return new AuctionCancelMessage.Response(
                    GTSPlugin.instance().messagingService().generatePingID(),
                    request.getID(),
                    auction,
                    request.getAuctionID(),
                    request.getActor(),
                    ImmutableList.copyOf(bidders),
                    result,
                    error
            );
        }

        throw new MessagingException(ErrorCodes.LISTING_MISSING);
    }

    @Override
    public BuyItNowMessage.Remove.Response processListingRemoveRequest(BuyItNowMessage.Remove.Request request) throws Exception {
        BiFunction<Boolean, ErrorCode, BINRemoveMessage.Response> processor = (success, error) -> new BINRemoveMessage.Response(
                GTSPlugin.instance().messagingService().generatePingID(),
                request.getID(),
                request.getListingID(),
                request.getActor(),
                request.getRecipient().orElse(null),
                request.shouldReturnListing(),
                success,
                success ? null : error
        );

        Optional<Listing> listing = this.getListing(request.getListingID());
        if(!listing.isPresent()) {
            return processor.apply(false, ErrorCodes.LISTING_MISSING);

        }

        Listing result = listing.get();
        if(((BuyItNow) result).isPurchased()) {
            return processor.apply(false, ErrorCodes.ALREADY_PURCHASED);
        }

        return processor.apply(this.deleteListing(request.getListingID()), ErrorCodes.FATAL_ERROR);
    }

    @Override
    public ForceDeleteMessage.Response processForcedDeletion(ForceDeleteMessage.Request request) throws Exception {
        Optional<Listing> listing = this.getListing(request.getListingID());
        if(listing.isPresent()) {
            boolean successful = this.deleteListing(request.getListingID());
            return ForceDeleteMessage.Response.builder()
                    .request(request.getID())
                    .listing(request.getListingID())
                    .actor(request.getActor())
                    .data(listing.get())
                    .give(request.shouldGive())
                    .successful(successful)
                    .error(successful ? null : ErrorCodes.FATAL_ERROR)
                    .build();
        } else {
            return ForceDeleteMessage.Response.builder()
                    .request(request.getID())
                    .listing(request.getListingID())
                    .actor(request.getActor())
                    .successful(false)
                    .give(request.shouldGive())
                    .error(ErrorCodes.LISTING_MISSING)
                    .build();
        }
    }

    @Override
    public ClaimDelivery.Response claimDelivery(ClaimDelivery.Request request) throws Exception {
        UUID target = request.getActor();
        Path path = this.fileGroups.get(Group.USERS).resolve(target.toString().substring(0, 2)).resolve(target.toString()).resolve("delivery_" + request.getDeliveryID() + this.extension);
        File file = path.toFile();

        if(file.exists()) {
            this.saveFile(path, null);
            boolean successful = file.exists();

            ClaimDeliveryImpl.ClaimDeliveryResponseImpl.DeliveryClaimResponseBuilder builder = ClaimDeliveryImpl.ClaimDeliveryResponseImpl.builder()
                    .request(request.getID())
                    .delivery(request.getDeliveryID())
                    .actor(request.getActor());

            if(successful) {
                builder.successful();
            } else {
                builder.error(ErrorCodes.FATAL_ERROR);
            }

            return builder.build();
        }

        return ClaimDeliveryImpl.ClaimDeliveryResponseImpl.builder()
                .request(request.getID())
                .delivery(request.getDeliveryID())
                .actor(request.getActor())
                .error(ErrorCodes.DELIVERY_MISSING)
                .build();
    }

    private ConfigurationNode readFile(Group group, UUID uuid) throws IOException {
        Path target = this.fileGroups.get(group).resolve(uuid.toString().substring(0, 2));
        if(group == Group.USERS) {
            target = target.resolve(uuid.toString());
        }
        return this.readFile(target.resolve(uuid + this.extension));
    }

    private ConfigurationNode readFile(Path target) throws IOException  {
        ReentrantLock lock = Objects.requireNonNull(this.ioLocks.get(target));
        lock.lock();
        try {
            if(!target.toFile().exists()) {
                return null;
            }

            return this.loader.loader(target).load();
        } finally {
            lock.unlock();
        }
    }

    private void saveFile(Group group, UUID name, ConfigurationNode node) throws IOException {
        Path target = this.fileGroups.get(group).resolve(name.toString().substring(0, 2));
        if(group == Group.USERS) {
            target = target.resolve(name.toString());
        }
        this.saveFile(target.resolve(name + this.extension), node);
    }

    private void saveFile(Path target, ConfigurationNode node) throws IOException {
        this.createDirectoriesIfNotExists(target.getParent());
        ReentrantLock lock = Objects.requireNonNull(this.ioLocks.get(target));
        lock.lock();
        try {
            if(node == null) {
                Files.deleteIfExists(target);
                return;
            }

            this.loader.loader(target).save(node);
        } finally {
            lock.unlock();
        }
    }

    private void createDirectoriesIfNotExists(Path path) throws IOException {
        if (Files.exists(path) && (Files.isDirectory(path) || Files.isSymbolicLink(path))) {
            return;
        }

        Files.createDirectories(path);
    }

    private Listing from(ConfigurationNode node) throws Exception {
        JObject json = new JObject();
        this.fill(json, node, true);
        JsonObject result = json.toJson();

        String type = result.get("type").getAsString();
        if(type.equals("bin")) {
            return GTSService.getInstance().getGTSComponentManager()
                    .getListingResourceManager(BuyItNow.class)
                    .get()
                    .getDeserializer()
                    .deserialize(result);
        } else {
            return GTSService.getInstance().getGTSComponentManager()
                    .getListingResourceManager(Auction.class)
                    .get()
                    .getDeserializer()
                    .deserialize(result);
        }
    }

    private void fill(JObject target, ConfigurationNode working, boolean empty) throws Exception {
        if(!working.childrenList().isEmpty()) {
            JArray array = new JArray();
            for(ConfigurationNode child : working.childrenList()) {
                this.fillArray(array, child);
            }
            target.add(working.key().toString(), array);
        } else if(!working.childrenMap().isEmpty()) {
            JObject child = new JObject();
            for(Map.Entry<Object, ? extends ConfigurationNode> entry : working.childrenMap().entrySet()) {
                this.fill(empty ? target : child, entry.getValue(), false);
            }
            if(!empty) {
                target.add(working.key().toString(), child);
            }
        } else {
            String key = working.key().toString();
            Class<?> typing = working.get(Object.class).getClass();
            if(typing.equals(String.class)) {
                target.add(key, working.getString());
            } else if(Number.class.isAssignableFrom(typing)) {
                double value = working.getDouble();
                target.add(key, value);
            } else if(typing.equals(Boolean.class)) {
                target.add(key, working.getBoolean());
            } else {
                throw new IllegalStateException("Invalid value for location: " + typing);
            }
        }
    }

    private void fillArray(JArray array, ConfigurationNode working) throws Exception {
        if(!working.childrenMap().isEmpty()) {
            JObject child = new JObject();
            for(Map.Entry<Object, ? extends ConfigurationNode> entry : working.childrenMap().entrySet()) {
                this.fill(child, entry.getValue(), false);
            }
            array.add(child);
        } else if(!working.childrenList().isEmpty()) {
            JArray aChild = new JArray();
            for(ConfigurationNode child : working.childrenList()) {
                this.fillArray(array, child);
            }
            array.add(aChild);
        } else {
            Class<?> typing = working.get(Object.class).getClass();
            if(typing.equals(String.class)) {
                array.add(working.getString());
            } else if(Number.class.isAssignableFrom(typing)) {
                double value = working.getDouble();
                array.add(value);
            } else {
                throw new IllegalStateException("Invalid value for location: " + typing);
            }
        }
    }

    private void writePath(ConfigurationNode parent, String key, JsonElement value) throws Exception {
        if(value.isJsonObject()) {
            JsonObject object = value.getAsJsonObject();
            ConfigurationNode child = BasicConfigurationNode.root();
            for(Map.Entry<String, JsonElement> path : object.entrySet()) {
                this.writePath(child, path.getKey(), path.getValue());
            }

            if(child.virtual()) {
                child.set(Maps.newHashMap());
            }

            parent.node(key).set(child);
        } else {
            if(value.isJsonPrimitive()) {
                JsonPrimitive primitive = value.getAsJsonPrimitive();
                if(primitive.isNumber()) {
                    Number number = primitive.getAsNumber();
                    parent.node(key).set(number);
                } else if(primitive.isBoolean()) {
                    parent.node(key).set(primitive.getAsBoolean());
                } else {
                    parent.node(key).set(primitive.getAsString());
                }
            } else if(value.isJsonArray()) {
                this.writeArrayToPath(parent, key, value.getAsJsonArray());
            }
        }
    }

    private void writeArrayToPath(ConfigurationNode parent, String key, JsonArray array) throws Exception {
        List<Object> output = Lists.newArrayList();
        for(JsonElement element : array) {
            if(element.isJsonObject()) {
                ConfigurationNode target = BasicConfigurationNode.root();
                JsonObject json = element.getAsJsonObject();
                for(Map.Entry<String, JsonElement> child : json.entrySet()) {
                    this.writePath(target, child.getKey(), child.getValue());
                }
                output.add(target);
            } else if(element.isJsonArray()) {
                ConfigurationNode target = BasicConfigurationNode.root();
                this.writeArrayToPath(target, key, element.getAsJsonArray());
                output.add(target);
            } else {
                if(element.isJsonPrimitive()) {
                    JsonPrimitive primitive = element.getAsJsonPrimitive();
                    if(primitive.isNumber()) {
                        Number number = primitive.getAsNumber();
                        output.add(number);
                    } else if(primitive.isBoolean()) {
                        output.add(primitive.getAsBoolean());
                    } else {
                        output.add(primitive.getAsString());
                    }
                }
            }
        }

        // TODO - Fix this triggering an error for new configuration nodes
        //parent.node(key).set(output);
    }
}
