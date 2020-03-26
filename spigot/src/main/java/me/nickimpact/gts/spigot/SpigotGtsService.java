package me.nickimpact.gts.spigot;

import co.aikar.commands.CommandIssuer;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gson.GsonBuilder;
import com.nickimpact.impactor.api.registry.BuilderRegistry;
import lombok.Setter;
import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.api.enums.CommandResults;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.api.holders.EntryRegistry;
import me.nickimpact.gts.api.listings.manager.ListingManager;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.api.searching.Searcher;
import me.nickimpact.gts.api.storage.GTSStorage;
import me.nickimpact.gts.api.util.TriFunction;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Setter
public class SpigotGtsService implements GTSService {

	private final GTSPlugin plugin;

	private ListingManager manager;
	private GTSStorage storage;
	private EntryRegistry registry;
	private BuilderRegistry builders;

	private GsonBuilder gson = new GsonBuilder().setPrettyPrinting();

	private Map<String, Searcher> searcherMap = Maps.newHashMap();
	private Multimap<Class<? extends Entry>, Function<?, Double>> minPriceExtras = ArrayListMultimap.create();

	public SpigotGtsService(GTSPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public ListingManager getListingManager() {
		return this.manager;
	}

	@Override
	public GTSStorage getStorage() {
		return this.storage;
	}

	@Override
	public EntryRegistry getEntryRegistry() {
		return this.registry;
	}

	@Override
	public void registerEntry(List<String> identifier, Class<? extends Entry> entry, EntryUI ui, String rep, TriFunction<CommandIssuer, List<String>, Boolean, CommandResults> cmd) {
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
	public void addSearcher(String key, Searcher searcher) {
		this.searcherMap.put(key, searcher);
	}

	@Override
	public Optional<Searcher> getSearcher(String key) {
		return Optional.ofNullable(this.searcherMap.get(key));
	}

	@Override
	public <T> void addMinPriceOption(Class<? extends Entry<?, T, ?, ?, ?>> type, Function<T, Double> function) {
		this.minPriceExtras.put(type, function);
	}

	@Override
	public <T> List<Function<T, Double>> getMinPriceOptionsForEntryType(Class<? extends Entry<?, T, ?, ?, ?>> type) {
		return this.minPriceExtras.entries().stream()
				.filter(entry -> entry.getKey().equals(type))
				.map(Map.Entry::getValue)
				.map(function -> (Function<T, Double>) function)
				.collect(Collectors.toList());
	}

	public static class SpigotEntryClassification implements EntryClassification<CommandIssuer, Material> {

		SpigotEntryClassification(SpigotEntryClassificationBuilder builder) {

		}

		@Override
		public Class<? extends Entry> getClassType() {
			return null;
		}

		@Override
		public List<String> getIdentifiers() {
			return null;
		}

		@Override
		public Material getMaterial() {
			return null;
		}

		@Override
		public EntryUI getUI() {
			return null;
		}

		@Override
		public CommandProcessor<CommandIssuer> getCommandHandler() {
			return null;
		}

		public static class SpigotEntryClassificationBuilder implements EntryClassificationBuilder {

			@Override
			public EntryClassificationBuilder classification(Class<? extends Entry> type) {
				return null;
			}

			@Override
			public EntryClassificationBuilder identifiers(String... identifiers) {
				return null;
			}

			@Override
			public EntryClassificationBuilder material(String material) {
				return null;
			}

			@Override
			public EntryClassificationBuilder ui(EntryUI ui) {
				return null;
			}

			@Override
			public EntryClassification build() {
				return null;
			}
		}
	}
}
