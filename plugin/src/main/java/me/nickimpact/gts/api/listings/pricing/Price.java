package me.nickimpact.gts.api.listings.pricing;

import me.nickimpact.gts.api.json.Typing;
import lombok.Getter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public abstract class Price<T> {

	@Getter private final String id;

	@Getter protected T price;

	public Price(T price) {
		if(this.getClass().isAnnotationPresent(Typing.class))
			this.id = this.getClass().getAnnotation(Typing.class).value();
		else
			this.id = this.getClass().getSimpleName();

		this.price = price;
	}

	/**
	 * Retrieve the {@link Text} representation of a price. Essentially, this just references
	 * how to format the price when requested for chat representation.
	 *
	 * @return The Textual format of a price
	 */
	public abstract Text getText();

	/**
	 * States whether or not a user can afford this price
	 *
	 * @param user The user to check
	 * @return True in the event they can afford it, false otherwise
	 */
	public abstract boolean canPay(User user);

	/**
	 * Withdraws the current status of the price from the specified user.
	 *
	 * @param user The user to withdraw from
	 */
	public abstract void pay(User user);

	/**
	 * Determines the amount of tax to apply to a listing
	 *
	 * @param player The player imposing the listing
	 * @return The amount of money the tax is
	 */
	public abstract Tuple<T, Boolean> calcTax(Player player);

	/**
	 * The UUID of the user who is to receive the payment
	 *
	 * @param uuid The UUID of the user
	 */
	public abstract void reward(UUID uuid);

	public abstract void openCreateUI(Player player);
}
