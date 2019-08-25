package me.nickimpact.gts.api.listings.prices;

import java.util.UUID;

public interface Price<T> {

	T getText();

	double getPrice();

	boolean canPay(UUID uuid);

	boolean pay(UUID uuid);

	void reward(UUID uuid);

	double calcTax();
}
