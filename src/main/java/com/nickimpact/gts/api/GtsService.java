package com.nickimpact.gts.api;

import com.nickimpact.gts.api.exceptions.NotMinableException;
import com.nickimpact.gts.api.json.Registry;
import com.nickimpact.gts.api.listings.entries.Entry;
import com.nickimpact.gts.api.listings.pricing.Price;
import com.nickimpact.gts.api.text.Token;
import com.nickimpact.gts.api.text.TokenService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface GtsService {

	/**
	 * Attempts to fetch the current status of a Registry. There are two registries available,
	 * and are represented by the {@link RegistryType} enum.
	 *
	 * @param type The type of registry you wish to fetch
	 * @return The {@link Registry} based upon the request
	 */
	Registry getRegistry(RegistryType type);

	/**
	 * Registers a single element into the GTS Service
	 *
	 * @param entry The class of the element you wish to register
	 */
	void registerEntry(Class<? extends Entry> entry);

	/**
	 * Registers types of entries into the GTS Service.
	 *
	 * @param entries The entries you wish to add into the Service
	 */
	void registerEntries(Collection<Class<? extends Entry>> entries);

	/**
	 * Fetches a list of all types of entries currently registered to GTS.
	 *
	 * @return All the entries currently registered with the GTS Service.
	 */
	Collection<Class<? extends Entry>> getEntries();

	/**
	 * Registers a single type of price into the GTS Sercice
	 *
	 * @param price The class of the price you wish to register
	 */
	void registerPrice(Class<? extends Price> price);

	/**
	 * Registers types of prices into the GTS Service.
	 *
	 * @param prices The prices you wish to add into the Service
	 */
	void registerPrices(Collection<Class<? extends Price>> prices);

	/**
	 * Fetches a list of all types of prices currently registered to GTS.
	 *
	 * @return All the prices currently registered with the GTS Service.
	 */
	Collection<Class<? extends Price>> getPrices();

	/**
	 * Attempts to add a function meant to help decide the level of a minimum price on an {@link Entry}.
	 * Be sure to cast your function variable to the proper object expected for the element class. For instance,
	 * a PokemonEntry should have the Object field of the Function argument filled in with an EntityPixelmon.
	 * Now you can use the Object as is if you don't care about the details to the pokemon, be do ensure that you
	 * cast to an EntityPixelmon during your function parsing if you wish to use some of those methods!
	 *
	 * @param clazz The class representing the type of element you wish to add a min price option to
	 * @param function A function that will apply some sort of change to the calculated min price
	 */
	void addMinPriceOption(Class<? extends Entry> clazz, Function<Object, Price> function) throws NotMinableException;

	/**
	 * Fetches a list of functions built to do some form of manipulation with a calculated min price.
	 *
	 * @param clazz The element class you wish to receive a list of
	 * @return An {@link Optional} holding the list of functions, or {@link Optional#empty()} if none exist
	 */
	Optional<List<Function<Object, Price>>> getMinPriceOptions(Class<? extends Entry> clazz);

	/**
	 * Registers the TokenService for GTS.
	 *
	 * NOTE: This function should truly be called only by GTS. GTS listens to Nucleus for its Token Service
	 * registration. Registering the GTS TokenService early can lead to unexpected consequences.
	 */
	void registerTokenService();

	/**
	 * Fetches the TokenService built into GTS.
	 *
	 * @return The TokenService registered with GTS
	 * @throws Exception If the service is not yet initialized
	 */
	TokenService getTokensService() throws Exception;

	/**
	 * Attempts to register a token that can be parsed by Nucleus.
	 *
	 * @param token The tokens to register
	 */
	void addToken(Token token) throws Exception;

	/**
	 * Attempts to register a series of tokens that can be parsed by Nucleus.
	 *
	 * @param tokens The tokens to register
	 */
	void addTokens(Token... tokens);

	enum RegistryType {
		ENTRY,
		PRICE,
	}
}
