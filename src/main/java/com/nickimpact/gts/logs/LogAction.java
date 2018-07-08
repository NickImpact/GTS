package com.nickimpact.gts.logs;

import com.nickimpact.gts.api.configuration.ConfigKey;
import com.nickimpact.gts.configuration.MsgConfigKeys;
import lombok.Getter;

import java.util.List;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Getter
public enum LogAction {

	Addition(MsgConfigKeys.LOGS_ADD),       // The action specified when a user inputs something into the listings
	Removal(MsgConfigKeys.LOGS_REMOVE),     // The action specified when a user removes something from the listings
	Expiration(MsgConfigKeys.LOGS_EXPIRE),  // The action specified when an element expires in the pool of listings
	Purchase(MsgConfigKeys.LOGS_PURCHASE),  // The action specified when a user receives the contents of a lot element
	Sell(MsgConfigKeys.LOGS_SELL);          // The action specified when a user has their listing sold on the market

	private final ConfigKey<List<String>> template;

	LogAction(ConfigKey<List<String>> template) {
		this.template = template;
	}
}
