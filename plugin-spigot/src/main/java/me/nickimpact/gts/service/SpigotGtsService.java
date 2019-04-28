package me.nickimpact.gts.service;

import com.nickimpact.impactor.api.registry.BuilderRegistry;
import lombok.Setter;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.api.holders.EntryRegistry;
import me.nickimpact.gts.api.listings.ListingManager;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import me.nickimpact.gts.api.storage.IGtsStorage;
import me.nickimpact.gts.api.wrappers.CmdResultWrapper;
import me.nickimpact.gts.api.wrappers.CmdSourceWrapper;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.function.BiFunction;

@Setter
public class SpigotGtsService implements GtsService<SpigotGtsService.SpigotCmdSource, SpigotGtsService.SpigotCmdResult> {

	private ListingManager manager;
	private IGtsStorage storage;
	private EntryRegistry registry;
	private BuilderRegistry builders;

	@Override
	public ListingManager getListingManager() {
		return this.manager;
	}

	@Override
	public IGtsStorage getStorage() {
		return this.storage;
	}

	@Override
	public EntryRegistry getEntryRegistry() {
		return this.registry;
	}

	@Override
	public void registerEntry(List<String> identifier, Class<? extends Entry> entry, EntryUI ui, String rep, BiFunction<SpigotCmdSource, String[], SpigotCmdResult> cmd) {
		try {
			this.registry.getRegistry().register(entry);
			this.registry.getClassifications().add(new EntryClassification<>(entry, identifier, rep, ui, cmd));

			GTS.getInstance().getPluginLogger().info("Loaded element type: " + entry.getSimpleName());
		} catch (Exception e) {
			GTS.getInstance().getPluginLogger().info("Failed to register type (" + entry.getSimpleName() + ") with reason: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public BuilderRegistry getBuilderRegistry() {
		return this.builders;
	}

	public class SpigotCmdSource extends CmdSourceWrapper<CommandSender> {}

	public class SpigotCmdResult extends CmdResultWrapper<Boolean> {}
}
