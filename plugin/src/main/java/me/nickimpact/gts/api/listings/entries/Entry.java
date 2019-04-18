package me.nickimpact.gts.api.listings.entries;

import me.nickimpact.gts.api.json.Typing;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.pricing.Price;
import lombok.Getter;
import me.nickimpact.gts.entries.prices.MoneyPrice;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.List;

/**
 * An element represents the actual elements we add into the GTS com.nickimpact.gts.api.listings. Essentially, they provide
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
public abstract class Entry<T, U> {

	/** The id typing for an element (used for GSON deserialization) */
	@Getter private final String id;

	/** The element held within the element */
	protected T element;

	/** The price of the element */
	protected MoneyPrice price;

	public Entry() {
		this.id = "";
	}

	public Entry(T entry, MoneyPrice price) {
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
	public U getEntry() {
		return this.handle();
	}

	protected abstract U handle();

	/**
	 * Fetches the price of the element
	 *
	 * @return The price of the element
	 */
	public MoneyPrice getPrice() {
		return this.price;
	}

	public abstract String getSpecsTemplate();

	/**
	 * Retrieves the name of a listing
	 *
	 * @return The name of an element
	 */
	public abstract String getName();

	/**
	 * Retrieves extended details about the listing
	 *
	 * @return A list of details relating to the item
	 */
	public abstract List<String> getDetails();

	/**
	 * Represents the ItemStack that will be used to represent the element in the listing display
	 *
	 * @return An ItemStack built to represent an element
	 */
	public abstract ItemStack baseItemStack(Player player, Listing listing);

	/**
	 * Represents an item stack that will be shown during the confirmation accepting of an element.
	 * This display can often be the same as the original display, but for other times, be used to
	 * help display extra data the original element did not.
	 *
	 * @return An ItemStack built to represent an element during confirmation.
	 */
	public abstract ItemStack confirmItemStack(Player player, Listing listing);

	/**
	 * States whether or not a listing can be handled for an offline player.
	 * By default, this setting is set to true for all com.nickimpact.gts.api.listings. However,
	 * things like ItemStacks, since they require an online player to go
	 * to a player's inventory, are unable to support such.
	 *
	 * @return Whether an element typing supports offline rewarding/give back
	 */
	public abstract boolean supportsOffline();

	/**
	 * Attempts to give the contents of a lot element to the passed player. This method ignores any attempt
	 * to make note of an entry's amount size, and returns everything as it currently is.
	 *
	 * @param user The user to give the entry element to
	 * @return <code>true</code> if the entry was given successfully, <code>false</code> otherwise
	 */
	public abstract boolean giveEntry(User user);

	/**
	 * Attempts to take the element away from the player depositing the listing.
	 *
	 * @param player The player who wants to deposit something
	 * @return true on success, false otherwise
	 */
	public abstract boolean doTakeAway(Player player);
}
