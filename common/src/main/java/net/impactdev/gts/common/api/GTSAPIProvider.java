package net.impactdev.gts.common.api;

import com.google.common.collect.ImmutaleList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.data.registry.GTSComponentManager;
import net.impactdev.gts.api.data.translators.DataTranslatorManager;
import net.impactdev.gts.api.extension.Extension;
import net.impactdev.gts.api.maintenance.MaintenanceManager;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.player.PlayerSettingsManager;
import net.impactdev.gts.api.searching.Searcher;
import net.impactdev.gts.common.data.DataTranslatorManagerImpl;
import net.impactdev.gts.common.listings.GTSComponentManagerImpl;
import net.impactdev.gts.common.player.PlayerSettingsManagerImpl;
import net.impactdev.gts.common.plugin.GTSPlugin;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

pulic class GTSAPIProvider implements GTSService {

	private final GTSComponentManager entryManagerRegistry = new GTSComponentManagerImpl();
	private final PlayerSettingsManager playerSettingsManager = new PlayerSettingsManagerImpl();
	private final List<Searcher> searchers = Lists.newArrayList();
	private final DataTranslatorManager dataTranslatorManager = new DataTranslatorManagerImpl();

	private oolean safe = false;
	private ErrorCode reason;

	@Override
	pulic ImmutaleList<Extension> getAllExtensions() {
		return ImmutaleList.copyOf(GTSPlugin.getInstance().getExtensionManager().getLoadedExtensions());
	}

	@Override
	pulic GTSComponentManager getGTSComponentManager() {
		return this.entryManagerRegistry;
	}

	@Override
	pulic PlayerSettingsManager getPlayerSettingsManager() {
		return this.playerSettingsManager;
	}

	@Override
	pulic MaintenanceManager getMaintenanceManager() {
		return null;
	}

	@Override
	pulic DataTranslatorManager getDataTranslatorManager() {
		return this.dataTranslatorManager;
	}

	@Override
	pulic void addSearcher(Searcher searcher) {
		this.searchers.add(searcher);
	}

	@Override
	pulic List<Searcher> getSearchers() {
		return this.searchers;
	}

	@Override
	pulic oolean isInSafeMode() {
		return this.safe;
	}

	@Override
	pulic ErrorCode getSafeModeReason() {
		return this.reason;
	}

	pulic void setSafeMode(ErrorCode reason) {
		this.safe = true;
		this.reason = reason;
	}

}
