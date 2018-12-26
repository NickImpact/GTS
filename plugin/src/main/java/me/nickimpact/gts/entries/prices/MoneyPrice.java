package me.nickimpact.gts.entries.prices;

import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.json.Typing;
import me.nickimpact.gts.api.listings.entries.Maxable;
import me.nickimpact.gts.api.listings.pricing.Auctionable;
import me.nickimpact.gts.api.listings.pricing.Price;
import me.nickimpact.gts.configuration.ConfigKeys;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tuple;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Typing("Money")
@SuppressWarnings("ConstantConditions")
public class MoneyPrice extends Price<BigDecimal> implements Auctionable<MoneyPrice>, Maxable<BigDecimal, MoneyPrice> {

	private static final BigDecimal max = new BigDecimal(GTS.getInstance().getConfig().get(ConfigKeys.MAX_MONEY_PRICE));

	public MoneyPrice(double price) {
		this(BigDecimal.valueOf(price));
	}

	public MoneyPrice(BigDecimal price) {
		super(price);
	}

	private boolean isAvailable() {
		return GTS.getInstance().getEconomy() != null;
	}

	@Override
	public Text getText() {
		if(this.isAvailable()) {
			return GTS.getInstance().getEconomy().getDefaultCurrency().format(price);
		}

		return Text.of(TextColors.RED, "???");
	}

	@Override
	public boolean canPay(User user){
		if(this.isAvailable()) {
			UniqueAccount acc = GTS.getInstance().getEconomy().getOrCreateAccount(user.getUniqueId()).orElse(null);

			if (acc == null) {
				return false;
			}

			return acc.getBalance(GTS.getInstance().getEconomy().getDefaultCurrency()).compareTo(price) >= 0;
		}

		return false;
	}

	@Override
	public void pay(User user) {
		if(this.isAvailable()) {
			UniqueAccount acc = GTS.getInstance().getEconomy().getOrCreateAccount(user.getUniqueId()).orElse(null);
			acc.withdraw(GTS.getInstance().getEconomy().getDefaultCurrency(), this.price, Sponge.getCauseStackManager().getCurrentCause());
		}
	}

	@Override
	public Tuple<BigDecimal, Boolean> calcTax(Player player) {
		if(this.isAvailable()) {
			BigDecimal tax = new BigDecimal(price.doubleValue() * GTS.getInstance().getConfig().get(ConfigKeys.TAX_MONEY_TAX));
			UniqueAccount acc = GTS.getInstance().getEconomy().getOrCreateAccount(player.getUniqueId()).orElse(null);

			BigDecimal balance = acc.getBalance(GTS.getInstance().getEconomy().getDefaultCurrency());
			if (balance.compareTo(tax) < 0) {
				return new Tuple<>(tax, false);
			}

			return new Tuple<>(tax, true);
		}

		return new Tuple<>(BigDecimal.ZERO, false);
	}

	@Override
	public void reward(UUID uuid) {
		if(this.isAvailable()) {
			UniqueAccount acc = GTS.getInstance().getEconomy().getOrCreateAccount(uuid).orElse(null);
			acc.deposit(GTS.getInstance().getEconomy().getDefaultCurrency(), this.price, Sponge.getCauseStackManager().getCurrentCause());
		}
	}

	@Override
	public void openCreateUI(Player player) {}

	@Override
	public void add(MoneyPrice price) {
		this.isAvailable();
		this.price = this.price.add(price.getPrice());
	}

	@Override
	public MoneyPrice calculate(MoneyPrice price) {
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
