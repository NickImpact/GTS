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

package net.impactdev.gts.communication.implementation.messages;

import net.impactdev.gts.communication.implementation.communicators.Communicator;
import net.impactdev.gts.communication.implementation.processing.IncomingMessageConsumer;
import net.impactdev.impactor.api.utility.printing.PrettyPrinter;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a message sent received via a {@link Communicator}.
 */
public interface Message extends PrettyPrinter.IPrettyPrintable {

    /**
     * Gets the unique id associated with this message.
     *
     * <p>This ID is used to ensure a single server instance doesn't process
     * the same message twice.</p>
     *
     * @return the id of the message
     */
    @NotNull UUID id();

    @NotNull Key key();

    /**
     * Specifies the exact instant this message was created. This is primarily useful for
     * logging purposes or measuring durations between a request and response message set.
     *
     * @return An instant representing the time a message was created
     */
    Instant timestamp();

    /**
     * Gets an encoded string form of this message.
     *
     * <p>The format of this string is likely to change between versions and
     * should not be depended on.</p>
     *
     * <p>Implementations which want to use a standard method of serialisation
     * can send outgoing messages using the string returned by this method, and
     * pass on the message on the "other side" using
     * {@link IncomingMessageConsumer#consume(String)}.</p>
     *
     * @return an encoded string form of the message
     */
    @NotNull String encoded();

}
