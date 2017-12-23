package com.nickimpact.gts.storage.dao;

import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.logs.Log;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

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

	public abstract void removeListing(int id) throws Exception;

	public abstract List<Listing> getListings() throws Exception;

	public abstract void addLog(Log log) throws Exception;

	public abstract void removeLog(int id) throws Exception;

	public abstract List<Log> getLogs() throws Exception;

	public abstract void purge(boolean logs) throws Exception;

	public abstract void save() throws Exception;
}
