package com.nickimpact.gts.api.listings.pricing;

import com.nickimpact.gts.api.json.Typing;
import lombok.Getter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

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
	 * @throws Exception In the event anything prevents proper checks of the price against the user
	 */
	public abstract boolean canPay(User user) throws Exception;

	/**
	 * Withdraws the current status of the price from the specified user.
	 *
	 * @param user The user to withdraw from
	 * @throws Exception In the event anything prevents the user from paying the price
	 */
	public abstract void pay(User user) throws Exception;

	/**
	 *
	 *
	 * @param player
	 * @return
	 * @throws Exception
	 */
	public abstract BigDecimal calcTax(Player player) throws Exception;

	/**
	 * States whether or not a Price can be given to an offline player. There are situations
	 * where a price may need to be temporarily stored, as a player might not be able to receive
	 * a price if they are offline. One example of this are {@link ItemStack}s.
	 *
	 * @return True if the reward can be given even to offline players, false otherwise
	 */
	public boolean supportsOfflineReward() {
		return true;
	}

	/**
	 * The UUID of the user who is to receive the payment
	 *
	 * @param uuid The UUID of the user
	 * @throws Exception Thrown in the event rewarding fails
	 */
	public abstract void reward(UUID uuid) throws Exception;

	public abstract void openCreateUI(Player player);
}
