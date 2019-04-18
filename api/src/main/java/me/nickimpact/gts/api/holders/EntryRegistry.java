package me.nickimpact.gts.api.holders;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.json.Registry;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import lombok.Getter;
import me.nickimpact.gts.api.listings.entries.Entry;

import java.util.List;
import java.util.Optional;

@Getter
public class EntryRegistry {

	private Registry<Entry> registry;

	public EntryRegistry(ImpactorPlugin plugin) {
		this.registry = new Registry<>(plugin);
	}

	private List<EntryClassification> classifications = Lists.newArrayList();

	public Optional<EntryClassification> getForIdentifier(String id) {
		return this.getClassifications().stream().filter(classification -> classification.getPrimaryIdentifier().equalsIgnoreCase(id)).findAny();
	}
}
