package me.nickimpact.gts.internal;

import com.google.common.collect.Maps;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.text.Translator;
import me.nickimpact.gts.entries.items.ItemEntry;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class ItemTokens {

	private static Map<String, Translator> tokens = Maps.newHashMap();

	static {
		tokens.put("item_type", (p, v, m) -> {
			ItemStack item = getItemStackFromVariableIfExists(m);
			if(item == null)
				return Optional.empty();

			return Optional.of(Text.of(item.getType().getTranslation().get()));
		});
		tokens.put("item_title", (p, v, m) -> {
			ItemStack item = getItemStackFromVariableIfExists(m);
			if(item == null)
				return Optional.empty();

			if(!item.get(Keys.DISPLAY_NAME).isPresent())
				return Optional.of(Text.of(TextColors.DARK_AQUA, item.getTranslation().get()));

			return Optional.of(item.get(Keys.DISPLAY_NAME).get());
		});
		tokens.put("item_lore", (p, v, m) -> {
			ItemStack item = getItemStackFromVariableIfExists(m);
			if(!item.get(Keys.ITEM_LORE).isPresent())
				return Optional.of(Text.EMPTY);

			Text.Builder tb = Text.builder();
			List<Text> lore = item.get(Keys.ITEM_LORE).get();
			lore.add(0, Text.of(TextColors.GREEN, "Item Lore: "));
			for(Text line : lore) {
				if(!lore.get(lore.size() - 1).equals(line))
					tb.append(line).append(Text.NEW_LINE);
				else
					tb.append(line);
			}

			return Optional.of(tb.build());
		});
	}

	public static Map<String, Translator> getTokens() {
		return tokens;
	}

	private static ItemStack getItemStackFromVariableIfExists(Map<String, Object> m) {
		Optional<Listing> opt = m.values().stream().filter(val -> val instanceof Listing).map(val -> (Listing) val).findAny();
		if(opt.isPresent()) {
			if(opt.get().getEntry() instanceof ItemEntry) {
				if(((ItemEntry) opt.get().getEntry().getEntry()).getItem() == null) {
					return ItemStack.builder().fromContainer((DataContainer) opt.get().getEntry().getEntry()).build();
				}

				return ((ItemEntry) opt.get().getEntry()).getItem();
			}
		}

		return ItemStack.builder().itemType(ItemTypes.BARRIER).add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "ERROR")).build();
	}
}
