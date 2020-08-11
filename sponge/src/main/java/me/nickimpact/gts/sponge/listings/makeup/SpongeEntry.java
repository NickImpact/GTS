package me.nickimpact.gts.sponge.listings.makeup;

import com.nickimpact.impactor.api.json.factory.JObject;
import lombok.AllArgsConstructor;
import me.nickimpact.gts.api.listings.entries.Entry;
import org.spongepowered.api.item.inventory.ItemStack;

@AllArgsConstructor
public abstract class SpongeEntry<T> implements Entry<T, ItemStack> {

	private final JObject data;

	@Override
	public JObject getInternalData() {
		return this.data;
	}

}
