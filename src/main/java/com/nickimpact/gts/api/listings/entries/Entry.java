package com.nickimpact.gts.api.listings.entries;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.json.Typing;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.pricing.Price;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import lombok.Getter;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Map;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public abstract class Entry<T> {

	@Getter
	private final String id;

	/** The element held within the entry */
	protected T element;

	/** The price of the entry */
	protected Price price;

	public Entry(T element, Price price) {
		this.id = this.getClass().getAnnotation(Typing.class).value();
		this.element = element;
		this.price = price;
	}

	/**
	 * Fetches the element held by this entry.
	 *
	 * @return The elemnt held by this entry.
	 */
	public T getElement() {
		return element;
	}

	/**
	 * Fetches the price of the entry
	 *
	 * @return The price of the entry
	 */
	public Price getPrice() {
		return this.price;
	}

	/**
	 * Retrieves the name of a listing
	 *
	 * @return The name of an entry
	 */
	public abstract String getName();

	/**
	 * Represents the ItemStack that will be used to represent the entry in the listing display
	 *
	 * @return An ItemStack built to represent an entry
	 */
	protected abstract ItemStack baseItemStack(Player player);

	protected abstract String baseTitleTemplate();

	protected abstract List<String> baseLoreTemplate();

	private void applyExtensions(Player player, ItemStack item, Listing listing) {
		Text title;
		List<Text> lore;

		Map<String, Object> variables = Maps.newHashMap();
		variables.put("dummy", element);
		variables.put("price", price);
		variables.put("dummy2", listing);

		try {
			title = GTS.getInstance().getTextParsingUtils().parse(
					GTS.getInstance().getTextParsingUtils().getTemplate(baseTitleTemplate()),
					player,
					null,
					variables
			);
		} catch (NucleusException e) {
			title = Text.of();
		}

		try {
			lore = GTS.getInstance().getTextParsingUtils().parse(
					GTS.getInstance().getTextParsingUtils().getTemplates(baseLoreTemplate()),
					player,
					null,
					variables
			);
		} catch (NucleusException e) {
			lore = Lists.newArrayList();
		}

		item.offer(Keys.DISPLAY_NAME, title);
		item.offer(Keys.ITEM_LORE, lore);
	}

	public ItemStack getBaseDisplay(Player player, Listing listing) {
		ItemStack item = baseItemStack(player);
		applyExtensions(player, item, listing);

		return item;
	}

	/**
	 * Represents an item stack that will be shown during the confirmation accepting of an entry.
	 * This display can often be the same as the original display, but for other times, be used to
	 * help display extra data the original entry did not.
	 *
	 * @return An ItemStack built to represent an entry during confirmation.
	 */
	protected abstract ItemStack confirmItemStack(Player player);

	protected abstract String confirmTitleTemplate();

	protected abstract List<String> confirmLoreTemplate();

	private void applyConfExtensions(Player player, ItemStack item, Listing listing) {
		Text title;
		List<Text> lore;

		try {
			title = GTS.getInstance().getTextParsingUtils().parse(
					GTS.getInstance().getTextParsingUtils().getTemplate(confirmTitleTemplate()),
					player,
					null,
					null
			);
		} catch (NucleusException e) {
			title = Text.of();
		}

		try {
			lore = GTS.getInstance().getTextParsingUtils().parse(
					GTS.getInstance().getTextParsingUtils().getTemplates(confirmLoreTemplate()),
					player,
					null,
					null
			);
		} catch (NucleusException e) {
			lore = Lists.newArrayList();
		}

		item.offer(Keys.DISPLAY_NAME, title);
		item.offer(Keys.ITEM_LORE, lore);
	}

	public ItemStack getConfirmDisplay(Player player, Listing listing) {
		ItemStack item = confirmItemStack(player);
		applyConfExtensions(player, item, listing);

		return item;
	}

	/**
	 * Attempts to give the contents of a lot entry to the passed player.
	 *
	 * @param user The user to receive the packed contents of an entry
	 * @return True if the task is successful, false otherwise
	 */
	public abstract boolean giveEntry(User user);

	/**
	 * Attempts to take the entry away from the player depositing the listing.
	 *
	 * @param player The player who wants to deposit something
	 * @return True if the task is successful, false otherwise
	 */
	public abstract boolean doTakeAway(Player player);
}
