package com.nickimpact.gts.configuration;

import com.google.common.collect.Lists;
import com.nickimpact.gts.api.configuration.ConfigKey;
import com.nickimpact.gts.api.configuration.keys.ListKey;
import com.nickimpact.gts.api.configuration.keys.StringKey;

import java.util.List;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class MsgConfigKeys {

	// Generic messages for the program
	// Best to support lists of text here, as a server may decide to go heavy on text formatting
	public static final ConfigKey<List<String>> MAX_LISTINGS = ListKey.of("max-listings", Lists.newArrayList(
			"{{gts_prefix}} &cUnfortunately, you can't deposit another listing, since you already have {{max_listings}} deposited..."
	));
	public static final ConfigKey<List<String>> ADD_TEMPLATE = ListKey.of("addition-to-seller", Lists.newArrayList(
			"{{gts_prefix}} &7Your &a{{listing_name}} &7has been added to the market!"
	));
	public static final ConfigKey<List<String>> TAX_APPLICATION = ListKey.of("taxes.applied", Lists.newArrayList(
			"&c&l- {{tax}} &7(&aTaxes&7)"
	));
	public static final ConfigKey<List<String>> TAX_INVALID = ListKey.of("taxes.invalid", Lists.newArrayList(
			"{{gts_prefix}} &cUnable to afford the tax of &e{{tax}} &cfor this listing..."
	));
	public static final ConfigKey<List<String>> ADD_BROADCAST = ListKey.of("addition-broadcast", Lists.newArrayList(
			"{{gts_prefix}} &c{{player}} &7has added a &a{{listing_specifics}} &7to the GTS for &a{{price}}&7!"
	));
	public static final ConfigKey<List<String>> PURCHASE_PAY = ListKey.of("prices.pay", Lists.newArrayList(
			"{{gts_prefix}} &7You have purchased a &a{{listing_specifics}} &7for &e{{price}}&7!"
	));
	public static final ConfigKey<List<String>> PURCHASE_RECEIVE = ListKey.of("prices.receive", Lists.newArrayList(
			"{{gts_prefix}} &7You have received your price of &e{{price}} from your &a{{listing_name}} &7listing!"
	));
	public static final ConfigKey<List<String>> AUCTION_BID = ListKey.of("auctions.bid", Lists.newArrayList(
			"{{gts_prefix}} &e{{player}} &7has placed a bid on the &a{{listing_specifics}}!"
	));
	public static final ConfigKey<List<String>> AUCTION_WIN = ListKey.of("auctions.win", Lists.newArrayList(
			"{{gts_prefix}} &e{{player}} &7has won the auction for the &a{{listing_specifics}}!"
	));
	public static final ConfigKey<List<String>> AUCTION_IS_HIGH_BIDDER = ListKey.of("auctions.is-high-bidder", Lists.newArrayList(
			"{{gts_prefix}} &cHold off! You wouldn't want to bid against yourself!"
	));
	public static final ConfigKey<List<String>> REMOVAL_CHOICE = ListKey.of("removal.choice", Lists.newArrayList(
			"{{gts_prefix}} &7Your &a{{listing_name}} &7listing has been returned!"
	));
	public static final ConfigKey<List<String>> REMOVAL_EXPIRES = ListKey.of("removal.expires", Lists.newArrayList(
			"{{gts_prefix}} &7Your &a{{listing_name}} &7listing has expired, and has thus been returned!"
	));

	// Items
	public static final ConfigKey<String> UI_ITEMS_NEXT_PAGE = StringKey.of("next-page", "&a\u2192 Next Page \u2192");
	public static final ConfigKey<String> UI_ITEMS_LAST_PAGE = StringKey.of("last-page", "&c\u2190 Last Page \u2190");
	public static final ConfigKey<String> UI_ITEMS_PLAYER_TITLE = StringKey.of("head.title", "&ePlayer Info");
	public static final ConfigKey<List<String>> UI_ITEMS_PLAYER_LORE = ListKey.of("head.lore", Lists.newArrayList());
	public static final ConfigKey<String> UI_ITEMS_SORT_TITLE = StringKey.of("sort.title", "&eSort Listings");
	public static final ConfigKey<List<String>> UI_ITEMS_SORT_LORE = ListKey.of("sort.lore", Lists.newArrayList());

	// Error messages
	public static final ConfigKey<String> NOT_ENOUGH_FUNDS = StringKey.of("purchase.not-enough-funds", "&cUnfortunately, you were unable to afford the price of {{price}}");
	public static final ConfigKey<String> ALREADY_CLAIMED = StringKey.of("purchase.already-claimed", "&cUnfortunately, this listing has already been claimed...");
}
