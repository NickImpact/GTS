package me.nickimpact.gts.api.listings.prices;

public interface Minable<T extends Price> {

	T calcMinPrice();

	default boolean isValid(double proposed) {
		return proposed >= this.calcMinPrice().getPrice();
	}

}
