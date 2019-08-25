package me.nickimpact.gts.sponge;

import lombok.Setter;
import me.nickimpact.gts.api.listings.prices.Price;
import me.nickimpact.gts.api.plugin.PluginInstance;
import me.nickimpact.gts.config.ConfigKeys;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.util.UUID;

public class MoneyPrice implements Price<Text> {

	@Setter private static EconomyService economy;
	private BigDecimal price;

	public MoneyPrice(double price) {
		this.price = new BigDecimal(price);
	}

	@Override
	public Text getText() {
		return economy.getDefaultCurrency().format(price);
	}

	@Override
	public double getPrice() {
		return price.doubleValue();
	}

	@Override
	public boolean canPay(UUID uuid) {
		return economy.getOrCreateAccount(uuid).get().getBalance(economy.getDefaultCurrency()).compareTo(price) >= 0;
	}

	@Override
	public boolean pay(UUID uuid) {
		if(this.canPay(uuid)) {
			economy.getOrCreateAccount(uuid).get().withdraw(economy.getDefaultCurrency(), price, Sponge.getCauseStackManager().getCurrentCause());
		}
		return false;
	}

	@Override
	public void reward(UUID uuid) {
		economy.getOrCreateAccount(uuid).get().deposit(economy.getDefaultCurrency(), price, Sponge.getCauseStackManager().getCurrentCause());
	}

	@Override
	public double calcTax() {
		return price.doubleValue() * PluginInstance.getInstance().getConfiguration().get(ConfigKeys.TAX_MONEY_TAX);
	}
}
