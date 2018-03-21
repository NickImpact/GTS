package com.nickimpact.gts.api.listings.entries;

import com.google.common.collect.ArrayListMultimap;
import com.nickimpact.gts.api.listings.pricing.Price;
import lombok.Getter;

import java.util.function.Function;

public class MinPriceMapping {

	@Getter
	private ArrayListMultimap<Class<? extends Entry>, Function<EntryElement, Price>> mapping = ArrayListMultimap.create();
}
