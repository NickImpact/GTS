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

package me.nickimpact.gts.api.messaging;

import lombok.Synchronized;
import me.nickimpact.gts.api.messaging.message.Message;
import me.nickimpact.gts.api.messaging.message.MessageConsumer;
import me.nickimpact.gts.api.messaging.message.OutgoingMessage;
import me.nickimpact.gts.api.messaging.message.type.MessageType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.SocketAddress;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * Encapsulates the LuckPerms system which accepts incoming {@link Message}s
 * from implementations of {@link Messenger}.
 */
public interface IncomingMessageConsumer {

    /**
     * Registers a message that is being requested by the calling plugin. This is to allow a server
     * to ensure a requested message eventually receives a response intended for itself.
     *
     * @param request The message working as the request message
     */
    void registerRequest(UUID request);

    /**
     * Attempts to locate a registered request. If one is found, this method will indicate such
     * by returning true, otherwise false. When found, this method will also remove said matching
     * request from the cache as preserving the message in that data set past that point is no
     * longer necessary.
     *
     * @param incomingID The ID that was marked on the incoming response message
     * @return True if a matching request is found, false otherwise
     */
    boolean locateAndFilterRequestIfPresent(UUID incomingID);

    /**
     * Caches the ID into the registry of read messages on this instance. This cache will purge out
     * received message IDs once a set amount of time has elapsed.
     *
     * @param id The ID of the message that has been received
     */
    void cacheReceivedID(UUID id);

    /**
     * Consumes a message instance.
     *
     * <p>The boolean returned from this method indicates whether or not the
     * platform accepted the message. Some implementations which have multiple
     * distribution channels may wish to use this result to dispatch the same
     * message back to additional receivers.</p>
     *
     * <p>The implementation will usually return <code>false</code> if a message
     * with the same ping id has already been processed.</p>
     *
     * @param message the message
     * @return true if the message was accepted by the plugin
     */
    boolean consumeIncomingMessage(@NonNull Message message);

    /**
     * Consumes a message in an encoded string format.
     *
     * <p>This method will decode strings obtained by calling
     * {@link OutgoingMessage#asEncodedString()}. This means that basic
     * implementations can successfully implement {@link Messenger} without
     * providing their own serialisation.</p>
     *
     * <p>The boolean returned from this method indicates whether or not the
     * platform accepted the message. Some implementations which have multiple
     * distribution channels may wish to use this result to dispatch the same
     * message back to additional receivers.</p>
     *
     * <p>The implementation will usually return <code>false</code> if a message
     * with the same ping id has already been processed.</p>
     *
     * @param encodedString the encoded string
     * @return true if the message was accepted by the plugin
     */
    boolean consumeIncomingMessageAsString(@NonNull String encodedString);

    <T extends Message, V extends T> void registerInternalConsumer(Class<T> parent, MessageConsumer<V> consumer);

    MessageConsumer<?> getInternalConsumer(Class<?> parent);
}
