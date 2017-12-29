package com.nickimpact.gts.api.listings.pricing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Getter
@RequiredArgsConstructor
public class PriceHolder {

	private final UUID uuid;
	private final Price price;
}
