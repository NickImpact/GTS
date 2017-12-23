package com.nickimpact.gts.internal;

import com.google.common.collect.Maps;
import com.nickimpact.gts.api.text.Translator;
import org.spongepowered.api.data.key.Keys;
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
		tokens.put("item_title", (p, v, m) -> {
			ItemStack item = getItemStackFromVariableIfExists(m);
			if(!item.get(Keys.DISPLAY_NAME).isPresent())
				return Optional.of(Text.of(item.getType().getName()));

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
		Optional<Object> opt = m.values().stream().filter(val -> val instanceof ItemStack).findAny();
		return (ItemStack) opt.orElse(null);
	}
}
