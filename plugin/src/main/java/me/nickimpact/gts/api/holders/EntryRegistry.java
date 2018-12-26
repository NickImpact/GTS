package me.nickimpact.gts.api.holders;

import lombok.Getter;
import me.nickimpact.gts.api.EntryUI;
import me.nickimpact.gts.api.json.Registry;
import me.nickimpact.gts.api.listings.entries.Entry;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@Getter
public class EntryRegistry {

	private Registry<Entry> registry = new Registry<>();
	private Map<Class<? extends Entry>, EntryUI> uis = new HashMap<>();
	private Map<Class<? extends Entry>, ItemStack> reps = new HashMap<>();
}
