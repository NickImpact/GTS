package me.nickimpact.gts.spigot;

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
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

@Setter
public class SpigotGtsService implements GtsService<CommandSender> {

	private final IGTSPlugin plugin;

	private ListingManager manager;
	private IGtsStorage storage;
	private EntryRegistry registry;
	private BuilderRegistry builders;

	private GsonBuilder gson = new GsonBuilder().setPrettyPrinting();

	public SpigotGtsService(IGTSPlugin plugin) {
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
	public void registerEntry(List<String> identifier, Class<? extends Entry> entry, EntryUI ui, String rep, BiFunction<CommandSender, String[], CommandResults> cmd) {
		try {
			this.registry.getRegistry().register(entry);
			this.registry.getClassifications().add(new SpigotEntryClassification(entry, identifier, rep, ui, cmd));

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
		return null;
	}

	public static class SpigotEntryClassification extends EntryClassification<CommandSender> {
		SpigotEntryClassification(Class<? extends Entry> classification, List<String> identifers, String itemRep, EntryUI ui, BiFunction<CommandSender, String[], CommandResults> cmdHandler) {
			super(classification, identifers, itemRep, ui, cmdHandler);
		}
	}
}
