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
package net.impactdev.gts.api.messaging

import net.impactdev.gts.api.messaging.message.Message
import net.impactdev.gts.api.messaging.message.MessageConsumer
import net.impactdev.gts.api.messaging.message.OutgoingMessage
import net.impactdev.gts.api.messaging.message.type.MessageType
import java.util.*
import java.util.function.Consumer

/**
 * Encapsulates the LuckPerms system which accepts incoming [Message]s
 * from implementations of [Messenger].
 */
interface IncomingMessageConsumer {
    /**
     * Registers a message that is being requested by the calling plugin. This is to allow a server
     * to ensure a requested message eventually receives a response intended for itself.
     *
     * @param request The message working as the request message
     */
    fun <T : MessageType.Response?> registerRequest(request: UUID?, response: Consumer<T>?)
    fun <T : MessageType.Response?> processRequest(request: UUID?, response: T)

    /**
     * Caches the ID into the registry of read messages on this instance. This cache will purge out
     * received message IDs once a set amount of time has elapsed.
     *
     * @param id The ID of the message that has been received
     */
    fun cacheReceivedID(id: UUID?)

    /**
     * Consumes a message instance.
     *
     *
     * The boolean returned from this method indicates whether or not the
     * platform accepted the message. Some implementations which have multiple
     * distribution channels may wish to use this result to dispatch the same
     * message back to additional receivers.
     *
     *
     * The implementation will usually return `false` if a message
     * with the same ping id has already been processed.
     *
     * @param message the message
     * @return true if the message was accepted by the plugin
     */
    fun consumeIncomingMessage(message: Message): Boolean

    /**
     * Consumes a message in an encoded string format.
     *
     *
     * This method will decode strings obtained by calling
     * [OutgoingMessage.asEncodedString]. This means that basic
     * implementations can successfully implement [Messenger] without
     * providing their own serialisation.
     *
     *
     * The boolean returned from this method indicates whether or not the
     * platform accepted the message. Some implementations which have multiple
     * distribution channels may wish to use this result to dispatch the same
     * message back to additional receivers.
     *
     *
     * The implementation will usually return `false` if a message
     * with the same ping id has already been processed.
     *
     * @param encodedString the encoded string
     * @return true if the message was accepted by the plugin
     */
    fun consumeIncomingMessageAsString(encodedString: String): Boolean
    fun <T : Message?, V : T?> registerInternalConsumer(parent: Class<T>?, consumer: MessageConsumer<V>?)
    fun getInternalConsumer(parent: Class<*>?): MessageConsumer<*>?
}