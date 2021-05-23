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

/**
 * Represents a provider for [Messenger] instances.
 *
 *
 * Users wishing to provide their own implementation for the plugins
 * "Messaging Service" should implement and register this interface.
 */
interface MessengerProvider {
    /**
     * Gets the name of this provider.
     *
     * @return the provider name
     */
    val name: String

    /**
     * Creates and returns a new [Messenger] instance, which passes
     * incoming messages to the provided [IncomingMessageConsumer].
     *
     *
     * As the agent should pass incoming messages to the given consumer,
     * this method should always return a new object.
     *
     * @param incomingMessageConsumer the consumer the new instance should pass
     * incoming messages to
     * @return a new messenger agent instance
     */
    fun obtain(incomingMessageConsumer: IncomingMessageConsumer): Messenger
}