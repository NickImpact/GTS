package me.nickimpact.gts.api.listings.prices;

public interface Minable<M extends Number & Comparable> {

	Price<M> calcMinPrice();

	default boolean isValid(double proposed) {
		return proposed >= this.calcMinPrice().getPrice();
	}

}
