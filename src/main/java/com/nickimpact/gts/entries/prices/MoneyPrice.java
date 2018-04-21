package com.nickimpact.gts.entries.prices;

import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.json.Typing;
import com.nickimpact.gts.api.listings.entries.Maxable;
import com.nickimpact.gts.api.listings.pricing.Auctionable;
import com.nickimpact.gts.api.listings.pricing.Price;
import com.nickimpact.gts.api.listings.pricing.PricingException;
import com.nickimpact.gts.configuration.ConfigKeys;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
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
public class MoneyPrice extends Price<BigDecimal> implements Auctionable<MoneyPrice>, Maxable<BigDecimal, MoneyPrice> {

	private static final BigDecimal max = new BigDecimal(GTS.getInstance().getConfig().get(ConfigKeys.MAX_MONEY_PRICE));

	public MoneyPrice(double price) {
		this(BigDecimal.valueOf(price));
	}

	public MoneyPrice(BigDecimal price) {
		super(price);
	}

	private void isAvailable() throws PricingException {
		if(GTS.getInstance().getEconomy() == null) {
			throw new PricingException("Economy Service could not be found...");
		}
	}

	@Override
	public Text getText() {
		try {
			this.isAvailable();
		} catch (PricingException e) {
			return Text.of("???");
		}
		return GTS.getInstance().getEconomy().getDefaultCurrency().format(price);
	}

	@Override
	public boolean canPay(User user) throws PricingException {
		this.isAvailable();
		UniqueAccount acc = GTS.getInstance().getEconomy().getOrCreateAccount(user.getUniqueId()).orElse(null);

		if(acc == null)
			throw new PricingException(user.getName() + " was unable to afford the price of the lot");

		return acc.getBalance(GTS.getInstance().getEconomy().getDefaultCurrency()).compareTo(price) >= 0;
	}

	@Override
	public void pay(User user) throws PricingException {
		this.isAvailable();
		UniqueAccount acc = GTS.getInstance().getEconomy().getOrCreateAccount(user.getUniqueId()).orElse(null);

		if(acc == null)
			throw new PricingException(user.getName() + "'s economic account was unable to be found...");

		acc.withdraw(
				GTS.getInstance().getEconomy().getDefaultCurrency(),
				this.price,
				Sponge.getCauseStackManager().getCurrentCause()
		);
	}

	@Override
	public BigDecimal calcTax(Player player) throws PricingException {
		this.isAvailable();

		// Return a tax rate of 8% based on the price
		BigDecimal tax = new BigDecimal(price.doubleValue() * GTS.getInstance().getConfig().get(ConfigKeys.TAX_MONEY_TAX));
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
	public void reward(UUID uuid) throws PricingException {
		this.isAvailable();
		UniqueAccount acc = GTS.getInstance().getEconomy().getOrCreateAccount(uuid).orElse(null);

		if(acc == null)
			throw new PricingException(uuid.toString() + "'s economic account was unable to be found...");

		acc.deposit(
				GTS.getInstance().getEconomy().getDefaultCurrency(),
				this.price,
				Sponge.getCauseStackManager().getCurrentCause()
		);
	}

	@Override
	public void openCreateUI(Player player) {}

	@Override
	public MoneyPrice getBase() throws PricingException {
		this.isAvailable();
		return new MoneyPrice(new BigDecimal(0));
	}

	@Override
	public void add(MoneyPrice price) throws PricingException {
		this.isAvailable();
		this.price = this.price.add(price.getPrice());
	}

	@Override
	public MoneyPrice calculate(MoneyPrice price) throws PricingException {
		this.isAvailable();
		return new MoneyPrice(this.price.add(price.getPrice()));
	}

	@Override
	public BigDecimal getMax() {
		return max;
	}

	@Override
	public boolean isLowerOrEqual() {
		return this.price.compareTo(this.getMax()) < 1;
	}
}
