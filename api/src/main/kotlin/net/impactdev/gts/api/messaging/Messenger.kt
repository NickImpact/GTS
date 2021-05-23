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

import net.impactdev.gts.api.messaging.message.OutgoingMessage

/**
 * Represents an object which dispatches [OutgoingMessage]s.
 */
interface Messenger : AutoCloseable {
    /**
     * Retrieves the bound [IncomingMessageConsumer] from this messenger.
     * This is the system that will process all incoming messages from another server
     *
     * @return The incoming message consumer
     */
    val messageConsumer: IncomingMessageConsumer?

    /**
     * Performs the necessary action to dispatch the message using the means
     * of the messenger.
     *
     *
     * The outgoing message instance is guaranteed to be an instance of one
     * of the interfaces extending [Message] in the
     * 'api.messaging.message.type' package.
     *
     *
     * 3rd party implementations are encouraged to implement this method with consideration
     * that new types may be added in the future.
     *
     *
     * This call is always made async.
     *
     * @param outgoingMessage the outgoing message
     */
    fun sendOutgoingMessage(outgoingMessage: OutgoingMessage)

    /**
     * Performs the necessary action to gracefully shutdown the messenger.
     */
    override fun close() {}
}