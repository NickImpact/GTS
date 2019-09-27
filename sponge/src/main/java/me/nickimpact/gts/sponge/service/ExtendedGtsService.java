package me.nickimpact.gts.sponge.service;

import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.sponge.text.TokenHolder;

public interface ExtendedGtsService extends GtsService {
	void registerTokens(TokenHolder holder);
}
