package me.nickimpact.gts.common.api;

import com.google.common.collect.Maps;
import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.common.data.StorableRegistryImpl;
import me.nickimpact.gts.api.searching.Searcher;

import java.util.Map;
import java.util.Optional;

public class GTSAPIProvider implements GTSService {

	private StorableRegistryImpl registry = new StorableRegistryImpl();

	private Map<String, Searcher> searcherMap = Maps.newHashMap();

	@Override
	public StorableRegistryImpl getStorableRegistry() {
		return this.registry;
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
