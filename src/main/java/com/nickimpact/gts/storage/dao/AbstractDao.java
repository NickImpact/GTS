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

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractDao {

	@Getter
	protected final GTS plugin;

	@Getter
	public final String name;

	public abstract void init();

	public abstract void shutdown();

	public abstract void addListing(Listing listing) throws Exception;

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
