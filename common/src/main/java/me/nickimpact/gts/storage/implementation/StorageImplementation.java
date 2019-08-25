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

package me.nickimpact.gts.storage.implementation;

import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.SoldListing;
import me.nickimpact.gts.api.plugin.IGTSPlugin;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents the basis to an implementation focused on saving and
 * retrieving data from a storage provider.
 */
public interface StorageImplementation {

    /**
     * Fetches the current instance of the plugin handling this storage
     * implementation.
     *
     * @return The current plugin instance held by this implementation
     */
    IGTSPlugin getPlugin();

    /**
     * The name of an implementation. In other words, this would be typically
     * found as JSON, H2, MySQL, etc.
     *
     * @return The name of the storage implementation
     */
    String getName();

    /**
     * Attempts to initialize the storage implementation, throwing an exception
     * if anything causes the implementation to fail to start.
     *
     * @throws Exception if any event causes the storage provider to fail to load
     */
    void init() throws Exception;

    void shutdown() throws Exception;

    default Map<String, String> getMeta() {
        return Collections.emptyMap();
    }

    boolean addListing(Listing listing) throws Exception;

    boolean deleteListing(UUID uuid) throws Exception;

    List<Listing> getListings() throws Exception;

    boolean addIgnorer(UUID uuid) throws Exception;

    boolean removeIgnorer(UUID uuid) throws Exception;

    List<UUID> getAllIgnorers() throws Exception;

    boolean addToSoldListings(UUID owner, SoldListing listing) throws Exception;

    List<SoldListing> getAllSoldListingsForPlayer(UUID uuid) throws Exception;

    boolean deleteSoldListing(UUID id, UUID owner) throws Exception;

    boolean purge() throws Exception;
}
