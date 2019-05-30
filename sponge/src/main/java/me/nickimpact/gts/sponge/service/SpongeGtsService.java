package me.nickimpact.gts.sponge.service;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import com.nickimpact.impactor.api.registry.BuilderRegistry;
import lombok.Setter;
import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.deprecated.OldAdapter;
import me.nickimpact.gts.api.enums.CommandResults;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.api.holders.EntryRegistry;
import me.nickimpact.gts.api.listings.ListingManager;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import me.nickimpact.gts.api.plugin.IGTSPlugin;
import me.nickimpact.gts.api.storage.IGtsStorage;
import me.nickimpact.gts.sponge.text.TokenHolder;
import me.nickimpact.gts.sponge.text.TokenService;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.function.BiFunction;

@Setter
public class SpongeGtsService implements ExtendedGtsService<CommandSource> {

	private final IGTSPlugin plugin;

	private ListingManager manager;
	private IGtsStorage storage;
	private EntryRegistry registry;
	private BuilderRegistry builders;

	private TokenService tokenService;

	private List<Class<? extends me.nickimpact.gts.api.deprecated.Entry>> types = Lists.newArrayList();
	private GsonBuilder gson = new GsonBuilder().setPrettyPrinting();

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
	public Gson getDeprecatedGson() {
		return gson.create();
	}

	@Override
	public <E> void registerOldTypeAdapter(Class<E> clazz, OldAdapter<E> adapter) {
		gson = gson.registerTypeAdapter(clazz, adapter);
	}

	@Override
	public <E> void registerOldTypeAdapter(Class<E> clazz, JsonSerializer<E> adapter) {
		gson = gson.registerTypeAdapter(clazz, adapter);
	}

	@Override
	public List<Class<? extends me.nickimpact.gts.api.deprecated.Entry>> getAllDeprecatedTypes() {
		return this.types;
	}

	@Override
	public void registerTokens(TokenHolder holder) {
		holder.getTokens().forEach((key, translator) -> tokenService.register(key, translator));
	}

	public static class SpongeEntryClassification extends EntryClassification<CommandSource> {
		SpongeEntryClassification(Class<? extends Entry> classification, List<String> identifers, String itemRep, EntryUI ui, BiFunction<CommandSource, String[], CommandResults> cmdHandler) {
			super(classification, identifers, itemRep, ui, cmdHandler);
		}
	}
}
