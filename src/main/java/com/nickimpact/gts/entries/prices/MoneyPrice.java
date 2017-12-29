package com.nickimpact.gts.entries.prices;

import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.json.Typing;
import com.nickimpact.gts.api.listings.pricing.Price;
import com.nickimpact.gts.api.listings.pricing.PricingException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Typing("Money")
public class MoneyPrice extends Price<BigDecimal> {

	public MoneyPrice(double price) {
		this(BigDecimal.valueOf(price));
	}

	public MoneyPrice(BigDecimal price) {
		super(price);
	}

	@Override
	public Text getText() {
		return GTS.getInstance().getEconomy().getDefaultCurrency().format(price);
	}

	@Override
	public boolean canPay(Player player) throws PricingException {
		UniqueAccount acc = GTS.getInstance().getEconomy().getOrCreateAccount(player.getUniqueId()).orElse(null);

		if(acc == null)
			throw new PricingException(player.getName() + " was unable to afford the price of the lot");

		return acc.getBalance(GTS.getInstance().getEconomy().getDefaultCurrency()).compareTo(price) >= 0;
	}

	@Override
	public void pay(Player payer) throws PricingException {
		UniqueAccount acc = GTS.getInstance().getEconomy().getOrCreateAccount(payer.getUniqueId()).orElse(null);

		if(acc == null)
			throw new PricingException(payer.getName() + "'s economic account was unable to be found...");

		acc.withdraw(
				GTS.getInstance().getEconomy().getDefaultCurrency(),
				this.price,
				Cause.builder().append(GTS.getInstance().getPluginContainer()).build(EventContext.empty())
		);
	}

	@Override
	public BigDecimal calcTax(Player player) throws PricingException {
		// Return a tax rate of 8% based on the price
		BigDecimal tax = new BigDecimal(price.doubleValue() * 0.08);
		UniqueAccount acc = GTS.getInstance().getEconomy().getOrCreateAccount(player.getUniqueId()).orElse(null);
		if(acc == null)
			throw new PricingException(player.getName() + "'s economic account was unable to be found...");

		BigDecimal balance = acc.getBalance(GTS.getInstance().getEconomy().getDefaultCurrency());
		if(balance.compareTo(tax) < 0) {
			return new BigDecimal(-1);
		}

		return tax;
	}

	@Override
	public void reward(UUID uuid) throws PricingException{
		UniqueAccount acc = GTS.getInstance().getEconomy().getOrCreateAccount(uuid).orElse(null);

		if(acc == null)
			throw new PricingException(uuid.toString() + "'s economic account was unable to be found...");

		acc.deposit(
				GTS.getInstance().getEconomy().getDefaultCurrency(),
				this.price,
				Cause.builder().append(GTS.getInstance().getPluginContainer()).build(EventContext.empty())
		);
	}

	@Override
	public void openCreateUI(Player player) {

	}
}
