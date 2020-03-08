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

package me.nickimpact.gts.messaging;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.nickimpact.gts.api.messaging.IncomingMessageConsumer;
import me.nickimpact.gts.api.messaging.Messenger;
import me.nickimpact.gts.api.messaging.MessengerProvider;
import me.nickimpact.gts.api.messaging.message.Message;
import me.nickimpact.gts.api.messaging.message.type.UpdateMessage;
import me.nickimpact.gts.common.cache.BufferedRequest;
import me.nickimpact.gts.messaging.messages.ListingsUpdateMessage;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.common.utils.gson.JObject;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class GTSMessagingService implements InternalMessagingService, IncomingMessageConsumer {

    private final GTSPlugin plugin;
    private final Set<UUID> receivedMessages;
    private final PushUpdateBuffer updateBuffer;

    private final MessengerProvider messengerProvider;
    private final Messenger messenger;

    private static final Gson NORMAL = new GsonBuilder().disableHtmlEscaping().create();

    public GTSMessagingService(GTSPlugin plugin, MessengerProvider messengerProvider) {
        this.plugin = plugin;

        this.messengerProvider = messengerProvider;
        this.messenger = messengerProvider.obtain(this);
        Objects.requireNonNull(this.messenger, "messenger");

        this.receivedMessages = Collections.synchronizedSet(new HashSet<>());
        this.updateBuffer = new PushUpdateBuffer(plugin);
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
    public BufferedRequest<Void> getUpdateBuffer() {
        return this.updateBuffer;
    }

    private UUID generatePingId() {
        UUID uuid = UUID.randomUUID();
        this.receivedMessages.add(uuid);
        return uuid;
    }

    @Override
    public void pushUpdate() {
        this.plugin.getScheduler().executeAsync(() -> {
            UUID requestId = generatePingId();
            this.plugin.getPluginLogger().info("[Messaging] Sending ping with id: " + requestId);
            this.messenger.sendOutgoingMessage(new ListingsUpdateMessage(requestId));
        });
    }

    @Override
    public boolean consumeIncomingMessage(@NonNull Message message) {
        Objects.requireNonNull(message, "message");

        if (!this.receivedMessages.add(message.getID())) {
            return false;
        }

        // determine if the message can be handled by us
        boolean valid = message instanceof UpdateMessage;

        // instead of throwing an exception here, just return false
        // it means an instance of LP can gracefully handle messages it doesn't
        // "understand" yet. (sent from an instance running a newer version, etc)
        if (!valid) {
            return false;
        }

        processIncomingMessage(message);
        return true;
    }

    @Override
    public boolean consumeIncomingMessageAsString(@NonNull String encodedString) {
        Objects.requireNonNull(encodedString, "encodedString");
        JsonObject decodedObject = NORMAL.fromJson(encodedString, JsonObject.class).getAsJsonObject();

        // extract id
        JsonElement idElement = decodedObject.get("id");
        if (idElement == null) {
            throw new IllegalStateException("Incoming message has no id argument: " + encodedString);
        }
        UUID id = UUID.fromString(idElement.getAsString());

        // ensure the message hasn't been received already
        if (!this.receivedMessages.add(id)) {
            return false;
        }

        // extract type
        JsonElement typeElement = decodedObject.get("type");
        if (typeElement == null) {
            throw new IllegalStateException("Incoming message has no type argument: " + encodedString);
        }
        String type = typeElement.getAsString();

        // extract content
        @Nullable JsonElement content = decodedObject.get("content");

        // decode message
        Message decoded;
	    if (ListingsUpdateMessage.TYPE.equals(type)) {
		    decoded = ListingsUpdateMessage.decode(content, id);
	    } else {// gracefully return if we just don't recognise the type
		    return false;
	    }

        // consume the message
        processIncomingMessage(decoded);
        return true;
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

    private void processIncomingMessage(Message message) {
        if (message instanceof UpdateMessage) {
            UpdateMessage msg = (UpdateMessage) message;
            this.plugin.getPluginLogger().info("[Messaging] Received update ping with id: " + msg.getID());
            this.plugin.getSyncTaskBuffer().request();
        } else {
            throw new IllegalArgumentException("Unknown message type: " + message.getClass().getName());
        }
    }

    private final class PushUpdateBuffer extends BufferedRequest<Void> {
        PushUpdateBuffer(GTSPlugin plugin) {
            super(2, TimeUnit.SECONDS, plugin.getScheduler());
        }

        @Override
        protected Void perform() {
            pushUpdate();
            return null;
        }
    }
}
