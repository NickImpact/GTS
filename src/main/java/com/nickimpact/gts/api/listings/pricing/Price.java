package com.nickimpact.gts.api.listings.pricing;

import com.nickimpact.gts.api.json.Typing;
import lombok.Getter;
import org.spongepowered.api.entity.living.player.Player;
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

	public abstract Text getText();

	public abstract boolean canPay(Player player) throws Exception;

	public abstract void pay(Player payer) throws Exception;

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
