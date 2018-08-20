package com.nickimpact.gts.api.listings.entries;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.json.Typing;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.pricing.Price;
import com.nickimpact.impactor.api.commands.SpongeSubCommand;
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
 * An element represents the actual elements we add into the GTS listings. Essentially, they provide
 * the backbone information for each type of listing. For example, one element type pertains to pokemon,
 * while another pertains to items.
 *
 * <p>
 * An element is responsible for two things. One, being that it must provide some way for GSON to know
 * how to deserialize it. If the {@link Typing} annotation is found to be missing from the class, the
 * ID for deserialization will default to the class name.
 * </p>
 *
 * <p>
 * The second things an element must provide is the {@link Price}. Like Entries, Prices are also extendable,
 * allowing for custom price settings. The element itself holds the Price information, purely due to the
 * fact that the {@link Listing} class really just represents the holder for these elements.
 * </p>
 *
 * @author NickImpact
 */
public abstract class Entry<T> {

	/** The id typing for an element (used for GSON deserialization) */
	@Getter private final String id;

	/** The element held within the element */
	protected T element;

	/** Insurance for the 3.8 data */
	@Deprecated protected T entry;

	/** The price of the element */
	protected Price price;

	public Entry() {
		this.id = "";
	}

	public Entry(T entry, Price price) {
		if(this.getClass().isAnnotationPresent(Typing.class)) {
			this.id = this.getClass().getAnnotation(Typing.class).value();
		} else {
			this.id = this.getClass().getSimpleName();
		}

		this.element = entry;
		this.price = price;
	}

	/**
	 * Fetches the element held by this element.
	 *
	 * @return The elemnt held by this element.
	 */
	public T getEntry() {
		if(this.element != null) {
			return element;
		} else {
			return entry;
		}
	}

	/**
	 * Fetches the price of the element
	 *
	 * @return The price of the element
	 */
	public Price getPrice() {
		return this.price;
	}

	/**
	 * This here declares how "/gts sell" will process your element type. For example, a pokemon element
	 * will be formatted as such: "/gts sell pokemon (any additional args)". We handle it like such so
	 * that each element type has a specific way to add in their options.
	 *
	 * <p>
	 * Note: This method will be deprecated, as future plans will call for a gui rather than specifying a command.
	 * This way, we can offer builders for easy user manipulation. However, due to the release of reforged, GTS
	 * must release ASAP.
	 * </p>
	 *
	 * @return A sub command to be registered into the "/gts sell" command
	 */
	public abstract SpongeSubCommand commandSpec(boolean isAuction);

	public abstract String getSpecsTemplate();

	public abstract List<String> getLogTemplate();

	/**
	 * Retrieves the name of a listing
	 *
	 * @return The name of an element
	 */
	public abstract String getName();

	/**
	 * Represents the ItemStack that will be used to represent the element in the listing display
	 *
	 * @return An ItemStack built to represent an element
	 */
	protected abstract ItemStack baseItemStack();

	protected abstract String baseTitleTemplate();

	protected abstract List<String> baseLoreTemplate(boolean auction);

	public ItemStack getBaseDisplay(Player player, Listing listing) {
		ItemStack item = baseItemStack();
		applyExtensions(player, item, listing);

		return item;
	}

	private void applyExtensions(Player player, ItemStack item, Listing listing) {
		Text title;
		List<Text> lore;

		Map<String, Object> variables = Maps.newHashMap();
		variables.put("dummy", getEntry());
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
					GTS.getInstance().getTextParsingUtils().getTemplates(baseLoreTemplate(listing.getAucData() != null)),
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

	/**
	 * Represents an item stack that will be shown during the confirmation accepting of an element.
	 * This display can often be the same as the original display, but for other times, be used to
	 * help display extra data the original element did not.
	 *
	 * @return An ItemStack built to represent an element during confirmation.
	 */
	protected abstract ItemStack confirmItemStack();

	protected abstract String confirmTitleTemplate(boolean auction);

	protected abstract List<String> confirmLoreTemplate(boolean auction);

	public ItemStack getConfirmDisplay(Player player, Listing listing) {
		ItemStack item = confirmItemStack();
		applyConfExtensions(player, item, listing);

		return item;
	}

	private void applyConfExtensions(Player player, ItemStack item, Listing listing) {
		Text title;
		List<Text> lore;

		Map<String, Object> variables = Maps.newHashMap();
		variables.put("dummy", getEntry());
		variables.put("price", price);
		variables.put("dummy2", listing);

		try {
			title = GTS.getInstance().getTextParsingUtils().parse(
					GTS.getInstance().getTextParsingUtils().getTemplate(confirmTitleTemplate(listing.getAucData() != null)),
					player,
					null,
					variables
			);
		} catch (NucleusException e) {
			title = Text.of();
		}

		try {
			lore = GTS.getInstance().getTextParsingUtils().parse(
					GTS.getInstance().getTextParsingUtils().getTemplates(confirmLoreTemplate(listing.getAucData() != null)),
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

	/**
	 * States whether or not a listing can be handled for an offline player.
	 * By default, this setting is set to true for all listings. However,
	 * things like ItemStacks, since they require an online player to go
	 * to a player's inventory, are unable to support such.
	 *
	 * @return Whether an element typing supports offline rewarding/give back
	 */
	public abstract boolean supportsOffline();

	/**
	 * Attempts to give the contents of a lot element to the passed player.
	 *
	 * @param user The user to receive the packed contents of an element
	 * @return True if the task is successful, false otherwise
	 */
	public abstract boolean giveEntry(User user);

	/**
	 * Attempts to take the element away from the player depositing the listing.
	 *
	 * @param player The player who wants to deposit something
	 * @return True if the task is successful, false otherwise
	 */
	public abstract boolean doTakeAway(Player player);
}
