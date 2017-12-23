package com.nickimpact.gts.api.listings.pricing;

import com.nickimpact.gts.api.json.Typing;
import lombok.Getter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public abstract class Price<T> {

	@Getter
	private final String id;

	protected T price;

	public Price(T price) {
		this.id = this.getClass().getAnnotation(Typing.class).value();
		this.price = price;
	}

	public abstract Text getText();

	public abstract boolean canPay(Player player) throws Exception;

	public abstract void pay(Player payer) throws Exception;

	public abstract BigDecimal calcTax(Player player) throws Exception;

	public abstract void reward(Player recipient) throws Exception;

	public abstract void openCreateUI(Player player);
}
