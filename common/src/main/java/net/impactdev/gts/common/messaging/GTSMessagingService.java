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

package net.impactdev.gts.common.messaging;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionClaimMessage;
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.BidMessage;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.removal.BuyItNowRemoveRequestMessage;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.gts.api.events.PingEvent;
import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.api.messaging.Messenger;
import net.impactdev.gts.api.messaging.MessengerProvider;
import net.impactdev.gts.api.messaging.message.OutgoingMessage;
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage;
import net.impactdev.gts.common.messaging.messages.utility.GTSPingMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class GTSMessagingService implements InternalMessagingService {

    private final GTSPlugin plugin;

    private final MessengerProvider messengerProvider;
    private final Messenger messenger;

    private Map<String, BiFunction<JsonElement, UUID, ? extends OutgoingMessage>> decoders = Maps.newHashMap();

    public static final Gson NORMAL = new GsonBuilder().disableHtmlEscaping().create();

    public GTSMessagingService(GTSPlugin plugin, MessengerProvider messengerProvider, IncomingMessageConsumer consumer) {
        this.plugin = plugin;

        this.messengerProvider = messengerProvider;
        this.messenger = messengerProvider.obtain(consumer);
        Objects.requireNonNull(this.messenger, "messenger");
    }

    @Override
    public String getName() {
        return this.messengerProvider.getName();
    }

    @Override
    public Messenger getMessenger() {
        return this.messenger;
    }

    @Override
    public MessengerProvider getMessengerProvider() {
        return this.messengerProvider;
    }

    @Override
    public void close() {
        this.messenger.close();
    }

    @Override
    public <T extends OutgoingMessage> void registerDecoder(String type, BiFunction<JsonElement, UUID, T> decoder) {
        this.decoders.put(type, decoder);
    }

    @Override
    public BiFunction<JsonElement, UUID, ? extends OutgoingMessage> getDecoder(String type) {
        return this.decoders.get(type);
    }

    @Override
    public UUID generatePingID() {
        UUID uuid = UUID.randomUUID();
        this.messenger.getMessageConsumer().cacheReceivedID(uuid);
        return uuid;
    }

    @Override
    public void sendPing() {
        Impactor.getInstance().getScheduler().executeAsync(() -> {
            UUID requestID = this.generatePingID();
            this.plugin.getPluginLogger().info("[Messaging] Sending ping with id: " + requestID);
            this.messenger.sendOutgoingMessage(new GTSPingMessage(requestID));
            Impactor.getInstance().getEventBus().postAsync(PingEvent.class, requestID, Instant.now());
        });
    }

    @Override
    public void publishAuctionListing(UUID auction, UUID actor, String broadcast) {

    }

    @Override
    public void publishBid(UUID listing, UUID actor, double bid, Consumer<AuctionMessage.Bid.Response> consumer) {
        Impactor.getInstance().getScheduler().executeAsync(() -> {
            UUID requestID = this.generatePingID();
            this.plugin.getPluginLogger().info("[Messaging] Publishing bid with ID: " + requestID + "...");
            this.messenger.getMessageConsumer().registerRequest(requestID, consumer);
            this.messenger.sendOutgoingMessage(new BidMessage(requestID, listing, actor, bid));
        });
    }

    @Override
    public void requestAuctionCancellation(UUID listing, UUID actor) {

    }

    @Override
    public void requestAuctionClaim(UUID listing, UUID actor, boolean isLister, Consumer<AuctionMessage.Claim.Response> callback) {
        AuctionClaimMessage.ClaimRequest request = new AuctionClaimMessage.ClaimRequest(this.generatePingID(), listing, actor, isLister);
        this.getMessenger().getMessageConsumer().registerRequest(request.getID(), callback);
        this.messenger.sendOutgoingMessage(request);
    }

    @Override
    public void requestBINRemoveRequest(UUID listing, UUID actor, @Nullable UUID receiver, boolean shouldReceive, Consumer<BuyItNowMessage.Remove.Response> consumer) {
        BuyItNowMessage.Remove.Request request = new BuyItNowRemoveRequestMessage(this.generatePingID(), listing, actor, receiver, shouldReceive);
        this.getMessenger().getMessageConsumer().registerRequest(request.getID(), consumer);
        this.messenger.sendOutgoingMessage(request);
    }

    public static String encodeMessageAsString(String type, UUID id, @Nullable JsonElement content) {
        JsonObject json = new JObject()
                .add("id", id.toString())
                .add("type", type)
                .consume(o -> {
                    if (content != null) {
                        o.add("content", content);
                    }
                })
                .toJson();

        return NORMAL.toJson(json);
    }

}