package com.nickimpact.gts.api;

import com.google.common.collect.Maps;
import com.nickimpact.gts.api.json.Registry;
import com.nickimpact.gts.api.json.Typing;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.entries.Entry;
import com.nickimpact.gts.api.listings.pricing.Price;
import com.nickimpact.gts.api.text.Tokens;
import com.nickimpact.gts.api.utils.MessageUtils;
import lombok.Getter;
import lombok.Setter;
import org.spongepowered.api.item.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Map;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class GtsAPI {

	/** The registry that holds the typings for all entries */
	private Registry<Entry> entries = new Registry<>();

	private Map<Class<? extends Entry>, ItemStack> entryDisplays = Maps.newHashMap();

	/** The registry that holds the typings for all prices */
	private Registry<Price> prices = new Registry<>();

	private Map<Class<? extends Price>, ItemStack> priceDisplays = Maps.newHashMap();

	@Getter @Setter private Tokens tokens;

	/**
	 * This method returns a Registry which is specified by the incoming parameter class type.
	 * The three valid class data are <code>Listing, Entry, and Price</code>. If any other
	 * class type is passed in, null will be returned instead, which can lead to a series of
	 * not-so-fun NPEs.
	 *
	 * @param clazz The class type of the target registry
	 * @return A registry pertaining to the class type, or null if none exist
	 */
	public Registry getRegistry(Class<?> clazz) {
		if(clazz.equals(Entry.class))
			return entries;
		else
			return prices;
	}

	public Map<Class<? extends Entry>, ItemStack> getEntryDisplays() {
		return entryDisplays;
	}

	public Map<Class<? extends Price>, ItemStack> getPriceDisplays() {
		return priceDisplays;
	}

	/**
	 * Attempts to add a class typing to a registry. The class type must extend from the registry's
	 * declared typing. Following such, the classes attempting to be registered must have the
	 * {@link Typing} annotation declared within its class path. If the class has already been
	 * registered, or the annotation is missing, an exception will be thrown.
	 *
	 * @param registry The registry holding all of the class information
	 * @param classes The classes attempting to be registered
	 * @param <E> The typing of the registry, and the classes that can fill it
	 */
	public <E> void insertIntoRegistry(Registry<E> registry, ArrayList<Class<? extends E>> classes) {
		for(Class<? extends E> clazz : classes) {
			try {
				registry.register(clazz);
			} catch (Exception e) {
				MessageUtils.genAndSendErrorMessage(
						"Registration Failure",
						"Offender: " + clazz.getName(),
						"Reason: " + e.getMessage()
				);
			}
		}
	}

	public <E> void addDisplayOption(Map<Class<? extends E>, ItemStack> mapping, Class<? extends E> clazz, ItemStack item) {
		mapping.putIfAbsent(clazz, item);
	}
}
