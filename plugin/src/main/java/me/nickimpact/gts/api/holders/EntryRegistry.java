package me.nickimpact.gts.api.holders;

import lombok.Getter;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import me.nickimpact.gts.api.json.Registry;
import me.nickimpact.gts.api.listings.entries.Entry;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class EntryRegistry {

	private Map<Class<? extends Entry>, String> identifiers = new LinkedHashMap<>();
	private Registry<Entry> registry = new Registry<>();
	private Map<Class<? extends Entry>, EntryUI> uis = new LinkedHashMap<>();
	private Map<Class<? extends Entry>, String> reps = new LinkedHashMap<>();
}
