package me.nickimpact.gts.text;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.api.placeholder.PlaceholderVariables;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.sponge.text.SpongeTokenService;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;

public class ItemTokens {

	public ItemTokens() {
		SpongeTokenService service = GTS.getInstance().getTokenService();
		ItemStackKey key = new ItemStackKey();
		service.register("item_type", placeholder -> service.getOrDefault(placeholder, key, item -> Text.of(item.getType().getTranslation().get())));
		service.register("item_title", placeholder -> service.getOrDefault(placeholder, SpongeTokenService.listingKey, listing -> Text.of(listing.getEntry().getName())));
		service.register("item_lore", placeholder -> service.getOrDefault(placeholder, key, item -> {
			if(item.get(Keys.ITEM_LORE).isPresent()) {
				Text.Builder tb = Text.builder();
				List<Text> lore = item.get(Keys.ITEM_LORE).get();
				lore.add(0, Text.of(TextColors.GREEN, "Item Lore: "));
				for(Text line : lore) {
					if(!lore.get(lore.size() - 1).equals(line))
						tb.append(line).append(Text.NEW_LINE);
					else
						tb.append(line);
				}
				return tb.build();
			}

			return Text.EMPTY;
		}));
	}

	public static class ItemStackKey implements PlaceholderVariables.Key<ItemStack> {

		@Override
		public String key() {
			return "GTS ItemStack";
		}

		@Override
		public TypeToken<ItemStack> getValueClass() {
			return new TypeToken<ItemStack>(){};
		}

	}

}
