package me.nickimpact.gts.sponge.text.placeholders;

import com.google.common.reflect.TypeToken;
import me.nickimpact.gts.api.placeholders.PlaceholderVariables;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public class SpongeKeys {

	public static final PlaceholderVariables.Key<ItemStackSnapshot> ITEM_KEY = new ItemKey();

	private static class ItemKey implements PlaceholderVariables.Key<ItemStackSnapshot> {

		@Override
		public String key() {
			return "item";
		}

		@Override
		public TypeToken<ItemStackSnapshot> getValueClass() {
			return new TypeToken<ItemStackSnapshot>() {};
		}

	}
}
