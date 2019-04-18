package me.nickimpact.gts.listings;

import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.listings.prices.Price;
import me.nickimpact.gts.config.ConfigKeys;
import me.xanium.gemseconomy.api.GemsEconomyAPI;
import me.xanium.gemseconomy.economy.Currency;

import java.util.UUID;

public class MoneyPrice implements Price<String> {

	private GemsEconomyAPI api = new GemsEconomyAPI();
	private Currency currency = api.getCurrency("dollars");

	private double price;

	public MoneyPrice(double price) {
		this.price = price;
	}

	@Override
	public String getText() {
		return currency.format(price);
	}

	@Override
	public double getPrice() {
		return this.price;
	}

	@Override
	public boolean canPay(UUID uuid) {
		return api.getBalance(uuid, currency) >= price;
	}

	@Override
	public boolean pay(UUID uuid) {
		if(this.canPay(uuid)) {
			api.withdraw(uuid, price, currency);
			return true;
		}
		return false;
	}

	@Override
	public void reward(UUID uuid) {
		api.deposit(uuid, price, currency);
	}

	@Override
	public double calcTax() {
		return price * GTS.getInstance().getConfiguration().get(ConfigKeys.TAX_MONEY_TAX);
	}
}
