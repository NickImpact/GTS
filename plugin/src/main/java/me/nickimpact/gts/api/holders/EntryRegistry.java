package me.nickimpact.gts.api.holders;

import com.google.common.collect.Lists;
import lombok.Getter;
import me.nickimpact.gts.api.json.Registry;
import me.nickimpact.gts.api.listings.entries.Entry;

import java.util.List;

@Getter
public class EntryRegistry {
	private Registry<Entry> registry = new Registry<>();

	private List<EntryClassification> classifications = Lists.newArrayList();
}
