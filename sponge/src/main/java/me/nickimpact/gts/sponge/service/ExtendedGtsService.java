package me.nickimpact.gts.sponge.service;

import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.sponge.text.TokenHolder;

public interface ExtendedGtsService<T> extends GtsService<T> {
	void registerTokens(TokenHolder holder);
}
