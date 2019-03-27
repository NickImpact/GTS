package me.nickimpact.gts.api.holders;

import com.google.common.collect.Lists;
import lombok.Getter;
import me.nickimpact.gts.api.json.Registry;
import me.nickimpact.gts.api.listings.entries.Entry;

import java.util.List;
import java.util.Optional;

@Getter
public class EntryRegistry {
	private Registry<Entry> registry = new Registry<>();

	private List<EntryClassification> classifications = Lists.newArrayList();

	public Optional<EntryClassification> getForIdentifier(String id) {
		return this.getClassifications().stream().filter(classification -> classification.getPrimaryIdentifier().equalsIgnoreCase(id)).findAny();
	}
}
