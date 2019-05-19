package me.nickimpact.gts.sponge.service;

import com.nickimpact.impactor.api.registry.BuilderRegistry;
import lombok.Setter;
import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.enums.CommandResults;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.api.holders.EntryRegistry;
import me.nickimpact.gts.api.listings.ListingManager;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import me.nickimpact.gts.api.plugin.IGTSPlugin;
import me.nickimpact.gts.api.storage.IGtsStorage;
import me.nickimpact.gts.api.text.TextService;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.function.BiFunction;

@Setter
public class SpongeGtsService implements GtsService<CommandSource, Text> {

	private final IGTSPlugin plugin;

	private ListingManager manager;
	private IGtsStorage storage;
	private EntryRegistry registry;
	private BuilderRegistry builders;

	public SpongeGtsService(IGTSPlugin plugin) {
		this.plugin = plugin;
	}

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
	public void registerEntry(List<String> identifier, Class<? extends Entry> entry, EntryUI ui, String rep, BiFunction<CommandSource, String[], CommandResults> cmd) {
		try {
			this.registry.getRegistry().register(entry);
			this.registry.getClassifications().add(new SpongeEntryClassification(entry, identifier, rep, ui, cmd));

			plugin.getPluginLogger().info("Loaded element type: " + entry.getSimpleName());
		} catch (Exception e) {
			plugin.getPluginLogger().info("Failed to register type (" + entry.getSimpleName() + ") with reason: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public BuilderRegistry getBuilderRegistry() {
		return this.builders;
	}

	@Override
	public TextService<Text> getTextService() {
		return null;
	}

	public static class SpongeEntryClassification extends EntryClassification<CommandSource> {
		SpongeEntryClassification(Class<? extends Entry> classification, List<String> identifers, String itemRep, EntryUI ui, BiFunction<CommandSource, String[], CommandResults> cmdHandler) {
			super(classification, identifers, itemRep, ui, cmdHandler);
		}
	}
}
