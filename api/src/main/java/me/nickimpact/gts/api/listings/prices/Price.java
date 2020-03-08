package me.nickimpact.gts.api.listings.prices;

import me.nickimpact.gts.api.listings.makeup.Display;
import net.kyori.text.TextComponent;

import java.util.UUID;

/**
 * A price is essentially the makeup of a price that a user can request in return for purchasing their listing.
 * A price can realistically be anything, and therefore, this wrapper should ultimately differ any generic usage
 * to its sub-option, {@link Pricable}. This will ensure we provide type safety during compilation, and avoid
 * any compile warnings.
 *
 * @param <T> Represents the option a pricable must employ for display representation
 */
public interface Price<T> {

	/**
	 * Represents the output of the price as it is to be displayed to the user querying the listing. In more general terms,
	 * this would represent the value, so for a dollar based currency, this would be something like "$500"
	 *
	 * @return The textual representation of the price
	 */
	TextComponent getText();

	/**
	 * Represents the look of a price should it be held in reserve for a player to accept or log back in, depending
	 * on the mode selected by the server configuration.
	 *
	 * @return The displayable representation of a price
	 */
	Display<T> getDisplay();

	/**
	 * The instance being setup as the price. So this might be a double or BigDecimal to represent some form of monetary
	 * value, or another instance to represent something more.
	 *
	 * @return The instance being used as the mark for the price
	 */
	Pricable<?> getPrice();

	/**
	 * Determines whether or not the user paying for the listing can actually pay the price. If they can, this call will
	 * return true. Otherwise, this call will return false to mark that any further action should be cancelled.
	 *
	 * @param payer The user paying for the listing
	 * @return True if the user can pay, false otherwise
	 */
	boolean canPay(UUID payer);

	/**
	 * Processes the payment for the user paying for the listing. This call should be made after verifying the user
	 * can actually complete the payment, via {@link #canPay(UUID)}.
	 *
	 * @param payer The user paying for the listing
	 */
	void pay(UUID payer);

	/**
	 * Processes the receiving end of a payment. Sometimes, a price may be unable to be completed due to offline
	 * restrictions, so this call may temporarily store the payment as a later receivable for the user intended
	 * to receive the payment.
	 *
	 * @param recipient The user receiving the payment
	 */
	void reward(UUID recipient);

	/**
	 * Calculates any tax associated with this price. While a tax may not necessarily be applied for a price, where
	 * taxing is enabled, this call will provide information on exactly what to charge.
	 *
	 * <p>Note: Taxes are only monetary, due to the nature of some things really not being really taxable. Currency
	 * is universally taxable, and therefore, should be what is used when actually applying any tax.</p>
	 *
	 * @return The value indicating how much to charge for tax of this price
	 */
	double getTax();

}
