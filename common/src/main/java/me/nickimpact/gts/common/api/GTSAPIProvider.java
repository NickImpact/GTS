package me.nickimpact.gts.common.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.api.extensions.Extension;
import me.nickimpact.gts.api.listings.entries.registry.EntryManagerRegistry;
import me.nickimpact.gts.api.player.PlayerSettingsManager;
import me.nickimpact.gts.api.searching.Searcher;
import me.nickimpact.gts.common.listings.EntryManagerRegistryImpl;
import me.nickimpact.gts.common.player.PlayerSettingsManagerImpl;

import java.util.Map;
import java.util.Optional;

public class GTSAPIProvider implements GTSService {

	private final EntryManagerRegistry entryManagerRegistry = new EntryManagerRegistryImpl();
	private final PlayerSettingsManager playerSettingsManager = new PlayerSettingsManagerImpl();
	private final Map<String, Searcher> searcherMap = Maps.newHashMap();

	@Override
	public ImmutableList<Extension> getAllExtensions() {
		return null;
	}

	@Override
	public EntryManagerRegistry getEntryManagerRegistry() {
		return this.entryManagerRegistry;
	}

	@Override
	public PlayerSettingsManager getPlayerSettingsManager() {
		return this.playerSettingsManager;
	}

	@Override
	public void addSearcher(String key, Searcher searcher) {
		this.searcherMap.put(key, searcher);
	}

	@Override
	public Optional<Searcher> getSearcher(String key) {
		return Optional.ofNullable(this.searcherMap.get(key));
	}

}