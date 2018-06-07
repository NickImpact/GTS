package com.nickimpact.gts.logs;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public enum LogAction {

	Addition,   // The action specified when a user inputs something into the listings
	Removal,    // The action specified when a user removes something from the listings
	Expiration, // The action specified when an element expires in the pool of listings
	Receive,    // The action specified when a user receives the contents of a lot element
	Sell,       // The action specified when a user has their listing sold on the market
}
