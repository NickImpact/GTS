package me.nickimpact.gts.sponge.service;

import co.aikar.commands.CommandIssuer;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gson.GsonBuilder;
import com.nickimpact.impactor.api.registry.BuilderRegistry;
import lombok.Setter;
import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.enums.CommandResults;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.api.holders.EntryRegistry;
import me.nickimpact.gts.api.listings.manager.ListingManager;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.ui.EntryUI;
import me.nickimpact.gts.api.placeholders.PlaceholderParser;
import me.nickimpact.gts.api.registry.GTSRegistry;
import me.nickimpact.gts.api.services.ServiceManager;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.api.searching.Searcher;
import me.nickimpact.gts.api.storage.IGtsStorage;
import me.nickimpact.gts.api.util.TriFunction;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Setter
public class SpongeGtsService implements GtsService {

	private final GTSPlugin plugin;

	private IGtsStorage storage;
	private EntryRegistry registry;
	private BuilderRegistry builders;

	private ServiceManager serviceManager;

	private GsonBuilder gson = new GsonBuilder().setPrettyPrinting();

	private Map<String, Searcher> searcherMap = Maps.newHashMap();
	private Multimap<Class<? extends Entry>, Function<?, Double>> minPriceExtras = ArrayListMultimap.create();

	public SpongeGtsService(GTSPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public ServiceManager getServiceManager() {
		return this.serviceManager;
	}

	@Override
	public GTSRegistry getRegistry() {
		return null;
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
	public void registerEntry(List<String> identifier, Class<? extends Entry> entry, EntryUI ui, String rep, TriFunction<CommandIssuer, List<String>, Boolean, CommandResults> cmd) {
		try {
			this.registry.getRegistry().register(entry);
			//this.registry.getClassifications().add(new SpongeEntryClassification(entry, identifier, rep, ui, cmd));

			plugin.getPluginLogger().info("Loaded element type: " + entry.getSimpleName());
		} catch (Exception e) {
			plugin.getPluginLogger().info("Failed to register type (" + entry.getSimpleName() + ") with reason: " + e.getMessage());
			e.printStackTrace();
		}
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
	public void registerPlaceholder(String token, PlaceholderParser parser) {

	}

	//	@Override
//	public <T> void addMinPriceOption(Class<? extends Entry<?, T, ?, ?, ?>> type, Function<T, Double> function) {
//		this.minPriceExtras.put(type, function);
//	}
//
//	@Override
//	public <T> List<Function<T, Double>> getMinPriceOptionsForEntryType(Class<? extends Entry<?, T, ?, ?, ?>> type) {
//		return this.minPriceExtras.entries().stream()
//				.filter(entry -> entry.getKey().equals(type))
//				.map(Map.Entry::getValue)
//				.map(function -> (Function<T, Double>) function)
//				.collect(Collectors.toList());
//	}
//	public static class SpongeEntryClassification extends EntryClassification<CommandIssuer> {
//		SpongeEntryClassification(Class<? extends Entry> classification, List<String> identifers, String itemRep, EntryUI ui, TriFunction<CommandIssuer, List<String>, Boolean, CommandResults> cmdHandler) {
//			super(classification, identifers, itemRep, ui, cmdHandler);
//		}
//
//		@Override
//		public Class<? extends Entry> getClassType() {
//			return null;
//		}
//
//		@Override
//		public List<String> getIdentifiers() {
//			return null;
//		}
//
//		@Override
//		public Object getMaterial() {
//			return null;
//		}
//
//		@Override
//		public EntryUI getUI() {
//			return null;
//		}
//
//		@Override
//		public CommandProcessor<CommandIssuer> getCommandHandler() {
//			return null;
//		}
//	}
}
