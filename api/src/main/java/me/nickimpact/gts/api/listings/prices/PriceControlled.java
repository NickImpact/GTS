package me.nickimpact.gts.api.listings.prices;

/**
 * Represents an Entry that has a set of price controls applied to it. This design is meant to only
 * cover prices that are controlled via a set currency background.
 */
public interface PriceControlled {

    double getMin();

    double getMax();

}
