package me.nickimpact.gts.spigot;

import lombok.Setter;
import me.nickimpact.gts.api.listings.prices.Price;
import me.xanium.gemseconomy.api.GemsEconomyAPI;
import me.xanium.gemseconomy.economy.Currency;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;

import java.util.UUID;

public class MoneyPrice implements Price<String> {

	@Setter
	private static Economy economy;

	private double price;

	public MoneyPrice(double price) {
		this.price = price;
	}

	@Override
	public String getText() {
		return economy.format(price).split(" ")[0];
	}

	@Override
	public double getPrice() {
		return this.price;
	}

	@Override
	public boolean canPay(UUID uuid) {
		return economy.getBalance(Bukkit.getOfflinePlayer(uuid)) >= price;
	}

	@Override
	public boolean pay(UUID uuid) {
		if(this.canPay(uuid)) {
			economy.withdrawPlayer(Bukkit.getOfflinePlayer(uuid), price);
			return true;
		}
		return false;
	}

	@Override
	public void reward(UUID uuid) {
		economy.depositPlayer(Bukkit.getOfflinePlayer(uuid), price);
	}

	@Override
	public double calcTax() {
		return price * 0.08;
	}
}
