package net.impactdev.gts.common.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.data.registry.GTSComponentManager;
import net.impactdev.gts.api.extension.Extension;
import net.impactdev.gts.api.player.PlayerSettingsManager;
import net.impactdev.gts.api.searching.Searcher;
import net.impactdev.gts.common.listings.GTSComponentManagerImpl;
import net.impactdev.gts.common.player.PlayerSettingsManagerImpl;
import net.impactdev.gts.common.plugin.GTSPlugin;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GTSAPIProvider implements GTSService {

	private final GTSComponentManager entryManagerRegistry = new GTSComponentManagerImpl();
	private final PlayerSettingsManager playerSettingsManager = new PlayerSettingsManagerImpl();
	private final List<Searcher> searchers = Lists.newArrayList();

	@Override
	public ImmutableList<Extension> getAllExtensions() {
		return ImmutableList.copyOf(GTSPlugin.getInstance().getExtensionManager().getLoadedExtensions());
	}

	@Override
	public GTSComponentManager getGTSComponentManager() {
		return this.entryManagerRegistry;
	}

	@Override
	public PlayerSettingsManager getPlayerSettingsManager() {
		return this.playerSettingsManager;
	}

	@Override
	public void addSearcher(Searcher searcher) {
		this.searchers.add(searcher);
	}

	@Override
	public List<Searcher> getSearchers() {
		return this.searchers;
	}

}
