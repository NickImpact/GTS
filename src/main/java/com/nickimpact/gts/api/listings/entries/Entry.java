package com.nickimpact.gts.api.listings.entries;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.commands.SpongeSubCommand;
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
 * An entry represents the actual elements we add into the GTS listings. Essentially, they provide
 * the backbone information for each type of listing. For example, one entry type pertains to pokemon,
 * while another pertains to items.
 *
 * <p>
 * An entry is responsible for two things. One, being that it must provide some way for GSON to know
 * how to deserialize it. If the {@link Typing} annotation is found to be missing from the class, the
 * ID for deserialization will default to the class name.
 * </p>
 *
 * <p>
 * The second things an entry must provide is the {@link Price}. Like Entries, Prices are also extendable,
 * allowing for custom price settings. The entry itself holds the Price information, purely due to the
 * fact that the {@link Listing} class really just represents the holder for these elements.
 * </p>
 *
 * @author NickImpact
 */
public abstract class Entry<T> {

	/** The id typing for an entry (used for GSON deserialization) */
	@Getter private final String id;

	/** The element held within the entry */
	protected T element;

	/** The price of the entry */
	protected Price price;

	public Entry() {
		this.id = "";
	}

	public Entry(T element, Price price) {
		if(this.getClass().isAnnotationPresent(Typing.class))
			this.id = this.getClass().getAnnotation(Typing.class).value();
		else
			this.id = this.getClass().getSimpleName();
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
	 * This here declares how "/gts sell" will process your entry type. For example, a pokemon entry
	 * will be formatted as such: "/gts sell pokemon (any additional args)". We handle it like such so
	 * that each entry type has a specific way to add in their options.
	 *
	 * <p>
	 * Note: This method will be deprecated, as future plans will call for a gui rather than specifying a command.
	 * This way, we can offer builders for easy user manipulation. However, due to the release of reforged, GTS
	 * must release ASAP.
	 * </p>
	 *
	 * @return A sub command to be registered into the "/gts sell" command
	 */
	@Deprecated
	public abstract SpongeSubCommand commandSpec();

	public abstract String getSpecsTemplate();

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
		variables.put("dummy", getElement());
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

		Map<String, Object> variables = Maps.newHashMap();
		variables.put("dummy", getElement());
		variables.put("price", price);
		variables.put("dummy2", listing);

		try {
			title = GTS.getInstance().getTextParsingUtils().parse(
					GTS.getInstance().getTextParsingUtils().getTemplate(confirmTitleTemplate()),
					player,
					null,
					variables
			);
		} catch (NucleusException e) {
			title = Text.of();
		}

		try {
			lore = GTS.getInstance().getTextParsingUtils().parse(
					GTS.getInstance().getTextParsingUtils().getTemplates(confirmLoreTemplate()),
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

	public ItemStack getConfirmDisplay(Player player, Listing listing) {
		ItemStack item = confirmItemStack(player);
		applyConfExtensions(player, item, listing);

		return item;
	}

	/**
	 * States whether or not a listing can be handled for an offline player.
	 * By default, this setting is set to true for all listings. However,
	 * things like ItemStacks, since they require an online player to go
	 * to a player's inventory, are unable to support such.
	 *
	 * @return Whether an entry typing supports offline rewarding/give back
	 */
	public abstract boolean supportsOffline();

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
