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

package com.nickimpact.gts.storage.dao;

import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.entries.EntryHolder;
import com.nickimpact.gts.api.listings.pricing.PriceHolder;
import com.nickimpact.gts.logs.Log;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractDao {

	@Getter
	protected final GTS plugin;

	@Getter
	public final String name;

	public abstract void init();

	public abstract void shutdown();

	public abstract void addListing(Listing listing) throws Exception;

	public abstract void updateListing(Listing listing) throws Exception;

	public abstract void removeListing(UUID uuid) throws Exception;

	public abstract List<Listing> getListings() throws Exception;

	public abstract void addLog(Log log) throws Exception;

	public abstract void removeLog(int id) throws Exception;

	public abstract List<Log> getLogs() throws Exception;

	public abstract void addHeldElement(EntryHolder holder) throws Exception;

	public abstract void removeHeldElement(EntryHolder holder) throws Exception;

	public abstract List<EntryHolder> getHeldElements() throws Exception;

	public abstract void addHeldPrice(PriceHolder holder) throws Exception;

	public abstract void removeHeldPrice(PriceHolder holder) throws Exception;

	public abstract List<PriceHolder> getHeldPrices() throws Exception;

	public abstract void addIgnorer(UUID uuid) throws Exception;

	public abstract void removeIgnorer(UUID uuid) throws Exception;

	public abstract List<UUID> getIgnorers() throws Exception;

	public abstract void purge(boolean logs) throws Exception;

	public abstract void save() throws Exception;
}
