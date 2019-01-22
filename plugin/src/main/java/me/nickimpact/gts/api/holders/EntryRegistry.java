package me.nickimpact.gts.api.holders;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import me.nickimpact.gts.api.json.Registry;
import me.nickimpact.gts.api.listings.entries.Entry;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@Getter
public class EntryRegistry {

	private Map<Class<? extends Entry>, String> identifiers = new HashMap<>();
	private Registry<Entry> registry = new Registry<>();
	private Map<Class<? extends Entry>, EntryUI> uis = new HashMap<>();
	private Map<Class<? extends Entry>, String> reps = new HashMap<>();
}
