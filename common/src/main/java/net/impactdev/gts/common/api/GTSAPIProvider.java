package net.impactdev.gts.common.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.data.registry.GTSComponentManager;
import net.impactdev.gts.api.data.translators.DataTranslatorManager;
import net.impactdev.gts.api.extensions.Extension;
import net.impactdev.gts.api.maintenance.MaintenanceManager;
import net.impactdev.gts.api.communication.message.errors.ErrorCode;
import net.impactdev.gts.api.player.PlayerSettingsManager;
import net.impactdev.gts.api.searching.Searcher;
import net.impactdev.gts.common.data.DataTranslatorManagerImpl;
import net.impactdev.gts.common.listings.GTSComponentManagerImpl;
import net.impactdev.gts.common.player.PlayerSettingsManagerImpl;
import net.impactdev.gts.common.plugin.GTSPlugin;

import java.util.List;

public class GTSAPIProvider implements GTSService {

	private final GTSComponentManager entryManagerRegistry = new GTSComponentManagerImpl();
	private final PlayerSettingsManager playerSettingsManager = new PlayerSettingsManagerImpl();
	private final List<Searcher> searchers = Lists.newArrayList();
	private final DataTranslatorManager dataTranslatorManager = new DataTranslatorManagerImpl();

	private boolean safe = false;
	private ErrorCode reason;

	@Override
	public ImmutableList<Extension> getAllExtensions() {
		if(GTSPlugin.instance().extensionManager() != null) {
			return ImmutableList.copyOf(GTSPlugin.instance().extensionManager().getLoadedExtensions());
		}

		return ImmutableList.of();
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
	public MaintenanceManager getMaintenanceManager() {
		return null;
	}

	@Override
	public DataTranslatorManager getDataTranslatorManager() {
		return this.dataTranslatorManager;
	}

	@Override
	public void addSearcher(Searcher searcher) {
		this.searchers.add(searcher);
	}

	@Override
	public List<Searcher> getSearchers() {
		return this.searchers;
	}

	@Override
	public boolean isInSafeMode() {
		return this.safe;
	}

	@Override
	public ErrorCode getSafeModeReason() {
		return this.reason;
	}

	public void setSafeMode(ErrorCode reason) {
		this.safe = true;
		this.reason = reason;
	}

}
