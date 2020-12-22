package net.impactdev.gts.common.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.configuration.ConfigKeyHolder;
import net.impactdev.impactor.api.configuration.keys.BaseConfigKey;
import net.impactdev.gts.common.config.types.time.TimeLanguageOptions;
import net.impactdev.gts.common.config.wrappers.SortConfigurationOptions;
import net.impactdev.gts.common.config.wrappers.TitleLorePair;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.impactdev.impactor.api.configuration.ConfigKeyTypes.*;

public class MsgConfigKeys implements ConfigKeyHolder {

	// Plugin chat prefix (replacement option for {{gts_prefix}}
	public static final ConfigKey<String> PREFIX = stringKey("general.gts-prefix", "&eGTS &7\u00bb");
	public static final ConfigKey<String> ERROR_PREFIX = stringKey("general.gts-prefix-error", "&eGTS &7(&cERROR&7)");

	// Generic messages for the program
	// Best to support lists of text here, as a server may decide to go heavy on text formatting
	public static final ConfigKey<List<String>> MAX_LISTINGS = listKey("general.max-listings", Lists.newArrayList(
			"{{gts:prefix}} &cUnfortunately, you can't deposit another listing, since you already have {{gts:max_listings}} deposited..."
	));
	public static final ConfigKey<List<String>> ADD_TEMPLATE = listKey("general.addition-to-seller", Lists.newArrayList(
			"{{gts:prefix}} &7Your &a{{gts:listing_name}} &7has been added to the market!"
	));

	public static final ConfigKey<List<String>> ADD_BROADCAST_BIN = listKey("general.addition-broadcast.buy-it-now", Lists.newArrayList(
			"{{gts:prefix}} {{gts:seller}} &7has added a &a{{gts:listing_details}} &7to the GTS for &a{{gts:bin_price}}&7!"
	));
	public static final ConfigKey<List<String>> ADD_BROADCAST_AUCTION = listKey("general.addition-broadcast.auctions", Lists.newArrayList(
			"{{gts:prefix}} {{gts:seller}} &7has added a &a{{gts:listing_details}} &7to the GTS for auction, starting at &e{{gts:auction_start_price}}&7!"
	));

	public static final ConfigKey<List<String>> PURCHASE_PAY = listKey("general.prices.pay", Lists.newArrayList(
			"{{gts:prefix}} &7You have purchased a &a{{gts:listing_details}} &7for &e{{gts:bin_price}}&7!"
	));
	public static final ConfigKey<List<String>> PURCHASE_RECEIVE = listKey("general.prices.receive", Lists.newArrayList(
			"{{gts:prefix}} &a{{gts:purchaser}} &7purchased your &a{{gts:listing_name}} &7listing for &a{{gts:bin_price}}&7!"
	));
	public static final ConfigKey<List<String>> MIN_PRICE_ERROR = listKey("general.prices.min-price.invalid", Lists.newArrayList(
			"{{gts:error}} &7In order to sell your &a{{gts:listing_name}}&7, you need to list it for the price of &e{{gts:min_price}}&7..."
	));
	public static final ConfigKey<List<String>> MAX_PRICE_ERROR = listKey("general.prices.max-price.invalid", Lists.newArrayList(
			"{{gts:error}} &7In order to sell your &a{{gts:listing_name}}&7, you need to list it for the price at or below &e{{gts:max_price}}&7..."
	));

	// Error messages
	public static final ConfigKey<String> PRICE_NOT_POSITIVE = stringKey("general.errors.non-positive-price", "{{gts:error}} Invalid price! Value must be positive!");
	public static final ConfigKey<String> PRICE_MAX_INVALID = stringKey("general.errors.max-price.invalid", "{{gts:error}} Your request is above the max amount of &e{{gts_max_price}}&7!");

	public static final ConfigKey<List<String>> DISCORD_PUBLISH_TEMPLATE = listKey("discord.templates.publish.buyitnow", Lists.newArrayList(
			"Listing ID: {{discord:listing_id}}",
			"",
			"Publisher: {{discord:publisher}}",
			"Identifier: {{discord:publisher_id}}",
			"",
			"Requested Price: {{discord:price}}",
			"Expiration Time: {{discord:expiration}}"
	));
	public static final ConfigKey<List<String>> DISCORD_PUBLISH_AUCTION_TEMPLATE = listKey("discord.templates.publish.auction", Lists.newArrayList(
			"Listing ID: {{discord:listing_id}}",
			"",
			"Publisher: {{discord:publisher}}",
			"Identifier: {{discord:publisher_id}}",
			"",
			"Starting Bid: {{discord:starting_bid}}",
			"Expiration Time: {{discord:expiration}}"
	));
	public static final ConfigKey<List<String>> DISCORD_PURCHASE_TEMPLATE = listKey("discord.templates.purchase", Lists.newArrayList(
			"Listing ID: {{discord:listing_id}}",
			"",
			"Buyer: {{discord:actor}}",
			"Buyer Identifier: {{discord:actor_id}}",
			"",
			"Seller: {{discord:publisher}}",
			"Seller Identifier: {{discord:publisher_id}}",
			"",
			"Price: {{discord:price}}"
	));
	public static final ConfigKey<List<String>> DISCORD_BID_TEMPLATE = listKey("discord.templates.bid", Lists.newArrayList(
			"Listing ID: {{discord:listing_id}}",
			"",
			"Bidder: {{discord:actor}}",
			"Bidder Identifier: {{discord:actor_id}}",
			"Bid Amount: {{discord:bid}}",
			"",
			"Seller: {{discord:publisher}}",
			"Seller Identifier: {{discord:publisher_id}}"
	));
	public static final ConfigKey<List<String>> DISCORD_REMOVAL_TEMPLATE = listKey("discord.templates.removal", Lists.newArrayList(
			"Listing ID: {{discord:listing_id}}",
			"",
			"Publisher: {{discord:publisher}}",
			"Identifier: {{discord:publisher_id}}"
	));
	public static final ConfigKey<String> LISTING_EVENT_CANCELLED = stringKey("general.listings.event-cancelled", "{{gts:error}} Your listing was blocked by an administrative source...");

	public static final ConfigKey<String> UNABLE_TO_TAKE_LISTING = stringKey("general.listings.unable-to-take", "{{gts:error}} Your listing failed to be taken...");
	public static final ConfigKey<String> CONFIRM_PURCHASE = stringKey("buttons.general.confirm-purchase", "&aConfirm Purchase");

	public static final ConfigKey<String> AWAITING_CREATE_LISTING_TITLE = stringKey("buttons.general.awaiting.create-listing.title", "&cCreate Listing");
	public static final ConfigKey<List<String>> AWAITING_CREATE_LISTING_LORE = listKey("buttons.general.awaiting.create-listing.lore", Lists.newArrayList(
		"&7Select an element you",
			"&7wish to sell/auction away",
			"&7to create a listing!"
	));
	public static final ConfigKey<String> CONFIRM_CREATE_LISTING_TITLE = stringKey("buttons.general.confirm.create-listing.title", "&aCreate Listing");
	public static final ConfigKey<List<String>> CONFIRM_CREATE_LISTING_LORE = listKey("buttons.general.confirm.create-listing.lore", Lists.newArrayList(
			"",
			"&eClick here to create your listing!"
	));

	public static final ConfigKey<String> AWAITING_SELECT_PRICE_TITLE = stringKey("buttons.general.awaiting.create-listing.title", "&cConfirm Price");
	public static final ConfigKey<List<String>> AWAITING_SELECT_PRICE_LORE = listKey("buttons.general.awaiting.create-listing.lore", Lists.newArrayList(
			"&7Please fill out price specifications",
			"&7first to confirm your price!"
	));
	public static final ConfigKey<String> CONFIRM_SELECT_PRICE_TITLE = stringKey("buttons.general.confirm.select-price.title", "&aConfirm Price");
	public static final ConfigKey<List<String>> CONFIRM_SELECT_PRICE_LORE = listKey("buttons.general.confirm.select-price.lore", Lists.newArrayList(
			"",
			"&eClick here to confirm your price!"
	));

	public static final ConfigKey<String> AWAITING_SELECTION_TITLE = stringKey("buttons.general.awaiting.selection.title", "&cConfirm Selection");
	public static final ConfigKey<List<String>> AWAITING_SELECTION_LORE = listKey("buttons.general.awaiting.selection.lore", Lists.newArrayList(
			"&7Please fill out price specifications",
			"&7first to confirm your selection!"
	));
	public static final ConfigKey<String> CONFIRM_SELECTION_TITLE = stringKey("buttons.general.confirm.selection.title", "&aConfirm Selection");
	public static final ConfigKey<List<String>> CONFIRM_SELECTION_LORE = listKey("buttons.general.confirm.selection.lore", Lists.newArrayList(
			"",
			"&eClick here to confirm your selection!"
	));

	// -----------------------------------------------------------------------------
	// Time
	// -----------------------------------------------------------------------------
	public static final ConfigKey<TimeLanguageOptions> SECONDS = customKey(c -> new TimeLanguageOptions(
			c.getString("time.seconds.singular", "Second"),
			c.getString("time.seconds.plural", "Seconds")
	));
	public static final ConfigKey<TimeLanguageOptions> MINUTES = customKey(c -> new TimeLanguageOptions(
			c.getString("time.minutes.singular", "Minute"),
			c.getString("time.minutes.plural", "Minutes")
	));
	public static final ConfigKey<TimeLanguageOptions> HOURS = customKey(c -> new TimeLanguageOptions(
			c.getString("time.hour.singular", "Hour"),
			c.getString("time.hour.plural", "Hours")
	));
	public static final ConfigKey<TimeLanguageOptions> DAYS = customKey(c -> new TimeLanguageOptions(
			c.getString("time.days.singular", "Day"),
			c.getString("time.days.plural", "Days")
	));
	public static final ConfigKey<TimeLanguageOptions> WEEKS = customKey(c -> new TimeLanguageOptions(
			c.getString("time.weeks.singular", "Week"),
			c.getString("time.weeks.plural", "Weeks")
	));

	public static final ConfigKey<String> CUSTOM_TIME_TITLE = stringKey("time.custom.title", "&aCustom Duration");
	public static final ConfigKey<List<String>> CUSTOM_TIME_LORE = listKey("time.custom.lore", Lists.newArrayList(
			"&7Specify how long you want",
			"&7the listing to last.",
			"",
			"&eClick to choose your time!"
	));

	public static final ConfigKey<String> STATUS_PURCHASED = stringKey("status.purchased", "&7Status: &aPurchased");
	public static final ConfigKey<String> STATUS_TIME_EXPIRED = stringKey("status.time.expired", "&7Status: &cConcluded");
	public static final ConfigKey<String> TIME_REMAINING_TRANSLATION = stringKey("status.time.remaining", "&7Ends in: &a{{gts:time_short}}");

	public static final ConfigKey<String> TIME_MOMENTS_TRANSLATION = stringKey("time.moments", "Moments");

	// -----------------------------------------------------------------------------
	// UI Based Configuration Options
	// -----------------------------------------------------------------------------

	// General Items
	public static final ConfigKey<String> UI_GENERAL_BACK = stringKey("ui.general.back", "&cGo Back");

	// Main Menu
	public static final ConfigKey<String> UI_MAIN_TITLE = stringKey("ui.menus.main.title", "&cGTS");
	public static final ConfigKey<TitleLorePair> UI_MAIN_BROWSER = customKey(c -> {
		String title = c.getString("ui.menus.main.browser.title", "&aBrowser");
		List<String> lore = c.getStringList("ui.menus.main.browser.lore", Lists.newArrayList(
				"&7Find items and more for sale",
				"&7by players across the network!",
				"",
				"&7Items offered here can be",
				"&edirectly purchased &7or will",
				"&7be posted for &eauction&7. If",
				"&7the item you wish to purchase is",
				"&7an auction, you must place the",
				"&7top bid by the time it expires",
				"&7to acquire the item!",
				"",
				"&eLeft click to open the quick purchase browser!",
				"&bRight click to open the auction browser!"
		));

		return new TitleLorePair(title, lore);
	});
	public static final ConfigKey<TitleLorePair> UI_MAIN_STASH = customKey(c -> {
		String title = c.getString("ui.menus.main.stash.title", "&aStash");
		List<String> lore = c.getStringList("ui.menus.main.stash.lore", Lists.newArrayList(
			"&7Items that you have &eacquired",
				"&7or &eexpired &7can be found here",
				"&7in order to be claimed!"
		));
		return new TitleLorePair(title, lore);
	});
	public static final ConfigKey<String> UI_MAIN_STASH_CLICK_NOTIF = stringKey("ui.menus.main.stash.click-to-open", "&eClick to open your stash!");
	public static final ConfigKey<TitleLorePair> UI_MAIN_SELL = customKey(c -> {
		String title = c.getString("ui.menus.main.sell.title", "&aSell a Good");
		List<String> lore = c.getStringList("ui.menus.main.sell.lore", Lists.newArrayList(
			"&7Here, you'll be able to directly",
				"&7sell items on the GTS market.",
				"&7Items you list here will be",
				"&7posted for quick purchase by",
				"&7another player, and will expire",
				"&7overtime if nobody ever purchases",
				"&7your listing.",
				"",
				"&eClick to become rich!"
		));

		return new TitleLorePair(title, lore);
	});

	public static final ConfigKey<TitleLorePair> UI_MAIN_VIEW_PERSONAL_LISTINGS = customKey(c -> {
		String title = c.getString("ui.menus.main.view-personal-listings.title", "&aView Your Listings");
		List<String> lore = c.getStringList("ui.menus.main.view-personal-listings.lore", Lists.newArrayList(
				"&7View the listings you've",
				"&7created that are still active",
				"&7on the market. Expired listings",
				"&7can be found in your stash!",
				"",
				"&eClick to view your listings!"
		));
		return new TitleLorePair(title, lore);
	});

	public static final ConfigKey<TitleLorePair> UI_MAIN_CURRENT_BIDS_SINGLE = customKey(c -> {
		String title = c.getString("ui.menus.main.bids.title", "&aView Bids");
		List<String> lore = c.getStringList("ui.menus.main.bids.lore", Lists.newArrayList(
				"&7Items that you have an active",
				"&7bid against can be found here",
				"&7for your convenience",
				"",
				"&bYou have {{gts:active_bids}} active bid",
				"",
				"&eClick to inspect!"
		));

		return new TitleLorePair(title, lore);
	});
	public static final ConfigKey<TitleLorePair> UI_MAIN_CURRENT_BIDS_MULTI = customKey(c -> {
		String title = c.getString("ui.menus.main.bids.title", "&aView Bids");
		List<String> lore = c.getStringList("ui.menus.main.bids.lore", Lists.newArrayList(
				"&7Items that you have an active",
				"&7bid against can be found here",
				"&7for your convenience",
				"",
				"&bYou have {{gts:active_bids}} active bids",
				"",
				"&eClick to inspect!"
		));

		return new TitleLorePair(title, lore);
	});
	public static final ConfigKey<TitleLorePair> UI_MAIN_PLAYER_SETTINGS = customKey(c -> {
		String title = c.getString("ui.menus.main.player-settings.title", "&aCustomize your Settings");
		List<String> lore = c.getStringList("ui.menus.main.player-settings.lore", Lists.newArrayList(
				"&7Control output made by GTS",
				"&7specifically for yourself!",
				"",
				"&7Here, you can set flags that",
				"&7control a specific output",
				"&7type!",
				"",
				"&eClick to begin editing!"
		));

		return new TitleLorePair(title, lore);
	});

	// Listings Menu
	public static final ConfigKey<String> UI_MENU_LISTINGS_TITLE = stringKey("ui.menus.listings.title", "&cGTS &7\u00bb &3Listings");
	public static final ConfigKey<String> UI_MENU_SEARCH_TITLE = stringKey(
			"ui.menus.listings.search.title",
			"&aSearch"
	);
	public static final ConfigKey<List<String>> UI_MENU_SEARCH_LORE_NO_QUERY = listKey(
			"ui.menus.listings.search.lore.no-query",
			Lists.newArrayList(
					"&7Find items by name, type,",
					"&7or any other options that",
					"&7can identify an item.",
					"",
					"&eClick to begin search!"
			)
	);
	public static final ConfigKey<List<String>> UI_MENU_SEARCH_LORE_QUERIED = listKey(
			"ui.menus.listings.search.lore.queried",
			Lists.newArrayList(
					"&7Find items by name, type,",
					"&7or any other options that",
					"&7can identify an item.",
					"",
					"&aCurrent Query:",
					"&3{{gts:search_query}}",
					"",
					"&eClick to edit search!"
			)
	);

	public static final ConfigKey<SortConfigurationOptions> UI_MENU_LISTINGS_SORT = customKey(c -> new SortConfigurationOptions(
			c.getString("ui.menus.listings.sort.title", "&aSort"),
			c.getString("ui.menus.listings.sort.lore.coloring.selected", "&b"),
			c.getString("ui.menus.listings.sort.lore.coloring.not-selected", "&7"),
			c.getString("ui.menus.listings.sort.lore.quick-purchase.most-recent", "Most Recent"),
			c.getString("ui.menus.listings.sort.lore.quick-purchase.ending-soon", "Ending Soon"),
			c.getString("ui.menus.listings.sort.lore.auctions.highest-bid", "Highest Bid"),
			c.getString("ui.menus.listings.sort.lore.auctions.lowest-bid", "Lowest Bid"),
			c.getString("ui.menus.listings.sort.lore.auctions.ending-soon", "Ending Soon"),
			c.getString("ui.menus.listings.sort.lore.auctions.most-bids", "Most Bids")
	));
	public static final ConfigKey<String> UI_MENU_LISTINGS_SPECIAL_LOADING = stringKey("ui.menus.listings.special.loading", "&eFetching Listings...");
	public static final ConfigKey<TitleLorePair> UI_MENU_LISTINGS_SPECIAL_TIMED_OUT = customKey(c -> {
		String title = c.getString("ui.menus.listings.special.timed-out.title", "&cFetch Timed Out");
		List<String> lore = c.getStringList("ui.menus.listings.special.timed-out.lore", Lists.newArrayList(
				"&7GTS failed to lookup the stored",
				"&7listings in a timely manner...",
				"",
				"&7Please retry opening the menu",
				"&7in a few moments!"
		));

		return new TitleLorePair(title, lore);
	});

	// Stash Window
	public static final ConfigKey<String> UI_MENU_STASH_TITLE = stringKey("ui.menus.stash.title", "&cGTS &7\u00bb &3Stash");
	public static final ConfigKey<String> UI_MENU_MAIN_STASH_STATUS = stringKey("ui.menus.main.stash.status", "&b* You have items available for pickup!");
	public static final ConfigKey<String> UI_ICON_STASH_COLLECT_ALL_TITLE = stringKey("ui.icons.stash.collect-all.title", "&aCollect All");
	public static final ConfigKey<List<String>> UI_ICON_STASH_COLLECT_ALL_LORE = listKey("ui.icons.stash.collect-all.lore", Lists.newArrayList(
			"&7Allows you to claim all your stashed",
			"&7listings at once! Note that if you",
			"&7don't have the space for a particular",
			"&7listing, it'll be skipped",
			"",
			"&eClick to begin your claim request!"
	));

	public static final ConfigKey<String> STASH_COLLECT_ALL_RESULTS = stringKey("ui.menus.stash.collect-all.results", "{{gts:prefix}} &7Successfully returned {{gts:stash_returned}} listings!");

	public static final ConfigKey<String> UI_MENU_ENTRY_SELECT_TITLE = stringKey("ui.menus.entry-select.title", "&cGTS &7\u00bb &3Select Entry Type");
	public static final ConfigKey<String> UI_MENU_PRICE_SELECT_TITLE = stringKey("ui.menus.price-select.title", "&cGTS &7\u00bb &3Select Price Type");

	public static final ConfigKey<String> UI_MENU_LISTING_SELECTED_OTHER = stringKey("ui.menus.listing-selected.purchaser", "&cGTS &7\u00bb &3Purchase Listing?");
	public static final ConfigKey<String> UI_MENU_LISTING_SELECTED_OTHER_AUCTION = stringKey("ui.menus.listing-selected.bidder", "&cGTS &7\u00bb &3Bid on Listing?");
	public static final ConfigKey<String> UI_MENU_LISTING_SELECTED_LISTER = stringKey("ui.menus.listing-selected.lister", "&cGTS &7\u00bb &3Remove Listing?");
	public static final ConfigKey<String> UI_MENU_LISTING_SELECTED_CLAIM = stringKey("ui.menus.listing-selected.claim", "&cGTS &7\u00bb &3Claim Rewards?");

	// Icons
	public static final ConfigKey<String> UI_ICON_BIN_CREATE_TITLE = stringKey("ui.icons.bin.creator.title", "&aBIN Mode");
	public static final ConfigKey<List<String>> UI_ICON_BIN_CREATE_LORE = listKey("ui.icons.bin.creator.lore", Lists.newArrayList(
			"&7Set a price, then one player",
			"&7may buy the listing at that",
			"&7price.",
			"",
			"&8(BIN means Buy It Now)",
			"",
			"&eClick to switch to Auction Mode!"
	));

	public static final ConfigKey<String> UI_ICON_AUCTION_CREATE_TITLE = stringKey("ui.icons.auction.creator.title", "&aAuction Mode");
	public static final ConfigKey<List<String>> UI_ICON_AUCTION_CREATE_LORE = listKey("ui.icons.auctions.creator.lore", Lists.newArrayList(
			"&7A listing in which multiple",
			"&7players compete for the listing",
			"&7by bidding against each other",
			"",
			"&eClick to switch to BIN Mode!"
	));

	public static final ConfigKey<String> UI_ICON_SELECTED_REMOVE_TITLE = stringKey("ui.icons.selected.remove.title", "&cRemove Listing?");
	public static final ConfigKey<List<String>> UI_ICON_SELECTED_REMOVE_LORE = listKey("ui.icons.selected.remove.lore", Lists.newArrayList(
			"&7Requests a removal of your",
			"&7listing from the &bGTS&7.",
			"",
			"&7NOTE: If your listing has already",
			"&7been claimed, this request may",
			"&7fail...",
			"",
			"&eClick here to request removal!"
	));

	public static final ConfigKey<String> UI_ICON_SELECTED_CLAIM_TITLE = stringKey("ui.icons.selected.claim.title", "&eClaim your {{gts:claim_item}}?");
	public static final ConfigKey<List<String>> UI_ICON_SELECTED_CLAIM_LORE = listKey("ui.icons.selected.remove.lore", Lists.newArrayList(
			"&7Requests a removal of your",
			"&7listing from the &bGTS&7.",
			"",
			"&7NOTE: If your listing has already",
			"&7been claimed, this request may",
			"&7fail...",
			"",
			"&eClick here to request removal!"
	));

	public static final ConfigKey<String> UI_ICON_PLACE_BID_TITLE = stringKey("ui.icons.auctions.place-bid.title", "&ePlace Bid");
	public static final ConfigKey<List<String>> UI_ICON_PLACE_BID_LORE = listKey("ui.icons.auctions.place-bid.lore", Lists.newArrayList(
			"&7New Bid: &6{{gts:auction_next_required_bid}}"
	));
	public static final ConfigKey<List<String>> UI_ICON_PLACE_BID_WITH_USER_BID_PLACED_LORE = listKey("ui.icons.auctions.place-bid.user-previously-bid.lore", Lists.newArrayList(
			"&7New Bid: &6{{gts:auction_next_required_bid}}",
			"&7Your previous bid: &e{{gts:auction_previous_user_bid}}"
	));
	public static final ConfigKey<List<String>> UI_ICON_PLACE_BID_CAN_AFFORD = listKey("ui.icons.auctions.place-bid.appenders.can-afford", Lists.newArrayList(
			"",
			"&eClick to bid!"
	));
	public static final ConfigKey<List<String>> UI_ICON_PLACE_BID_CANT_AFFORD = listKey("ui.icons.auctions.place-bid.appenders.cant-afford", Lists.newArrayList(
			"",
			"&cCan't afford bid!"
	));
	public static final ConfigKey<List<String>> UI_ICON_PLACE_BID_IS_TOP_BID = listKey("ui.icons.auctions.place-bid.appenders.user-is-top-bidder", Lists.newArrayList(
			"",
			"&cYou already hold the top bid!"
	));

	public static final ConfigKey<String> UI_ICON_PLACE_CUSTOM_BID_TITLE = stringKey("ui.icons.auctions.place-bid.custom.title", "&eCustom Bid");
	public static final ConfigKey<List<String>> UI_ICON_PLACE_CUSTOM_BID_LORE_BASE = listKey("ui.icons.auctions.place-bid.custom.lore.base", Lists.newArrayList(
			"&7With this option, you can",
			"&7specify a custom bid of your",
			"&7desires, so long as it's",
			"&7at least able to meet the",
			"&7current requirement for the",
			"&7next bid!",
			"",
			"&eClick to cast a custom bid!"
	));

	public static final ConfigKey<String> CUSTOM_BID_INVALID = stringKey("ui.icons.auctions.place-bid.custom.actions.bid-invalid", "{{gts:error}} You must bid at least &e{{gts:auction_next_required_bid}}&7!");

	public static final ConfigKey<String> UI_ICON_BID_HISTORY_TITLE = stringKey("ui.icons.auctions.bid-history.title", "&eBid History");
	public static final ConfigKey<List<String>> UI_ICON_BID_HISTORY_BASE_INFO = listKey("ui.icons.auctions.bid-history.base-info", Lists.newArrayList(
			"&7Bids Placed: &e{{gts:auction_bids}}"
	));
	public static final ConfigKey<String> UI_ICON_BID_HISTORY_SEPARATOR = stringKey("ui.icons.auctions.bid-history.separator", "&8&m-------------------");
	public static final ConfigKey<List<String>> UI_ICON_BID_HISTORY_BID_INFO = listKey("ui.icons.auctions.bid-history.bid-info", Lists.newArrayList(
			"&7Bid: &e{{gts:auction_bid_amount}}",
			"&7By: &e{{gts:auction_bid_actor}}",
			"&b{{gts:auction_bid_since_placed}} ago"
	));
	public static final ConfigKey<List<String>> UI_ICON_BID_HISTORY_NO_BIDS = listKey("ui.icons.auctions.bid-history.no-bids", Lists.newArrayList(
			"",
			"&7Be the first to place a",
			"&7bid on this auction!"
	));

	// Price Selection
	public static final ConfigKey<String> UI_PRICE_DISPLAY_TITLE = stringKey("ui.components.price.display.title", "&ePrice: {{gts:price_selection}}");
	public static final ConfigKey<List<String>> UI_PRICE_DISPLAY_LORE = listKey("ui.components.price.display.lore.base", Lists.newArrayList(
			"&7How much to list your",
			"&7be listed on the GTS."
	));

	public static final ConfigKey<List<String>> UI_PRICE_DISPLAY_FEES = listKey("ui.components.price.display.lore.fees", Lists.newArrayList(
			"",
			"&7Fee: &6{{gts:price_fee}} &e({{gts:price_fee_rate}})"
	));

	// Time Selection
	public static final ConfigKey<String> UI_TIME_SELECT_TITLE = stringKey("ui.time-select.title", "Select a Time");

	public static final ConfigKey<String> UI_TIME_DISPLAY_TITLE = stringKey("ui.components.time.display.title", "&eDuration: {{gts:time}}");
	public static final ConfigKey<List<String>> UI_TIME_DISPLAY_LORE = listKey("ui.components.time.display.lore.base", Lists.newArrayList(
			"&7How long the listing will",
			"&7be listed on the GTS."
	));

	public static final ConfigKey<List<String>> UI_TIME_DISPLAY_FEES = listKey("ui.components.time.display.lore.fees", Lists.newArrayList(
			"",
			"&7Fee: &6{{gts:time_fee}}"
	));

	public static final ConfigKey<List<String>> UI_COMPONENT_EDIT_LORE = listKey("ui.components.edit-lore", Lists.newArrayList(
			"",
			"&eClick to edit!"
	));

	// Fees
	public static final ConfigKey<List<String>> FEE_APPLICATION = listKey("general.fees.applied", Lists.newArrayList(
			"{{gts:prefix}} &c&l- {{gts:fees}} &7(&aFees&7)"
	));
	public static final ConfigKey<List<String>> FEE_INVALID = listKey("general.fees.invalid", Lists.newArrayList(
			"{{gts:prefix}} &cUnable to afford the tax of &e{{gts:fees}} &cfor this listing..."
	));
	public static final ConfigKey<String> FEE_PRICE_FORMAT = stringKey("general.fees.price-format", "&7Price Selection: {{gts:price_fee}}");
	public static final ConfigKey<String> FEE_TIME_FORMAT = stringKey("general.fees.time-format", "&7Time Selection: {{gts:time_fee}}");

	// Admin Menus
	public static final ConfigKey<String> UI_ADMIN_MAIN_TITLE = stringKey("ui.admin.main.title", "&cGTS &7\u00bb &3Admin Mode");

	public static final ConfigKey<String> UI_ADMIN_MAIN_MANAGER = stringKey("ui.admin.main.icons.manager", "&aGTS Listing Manager");
	public static final ConfigKey<String> UI_ADMIN_MAIN_PRICE_MGMT = stringKey("ui.admin.main.icons.price-management", "&aPricing Management");
	public static final ConfigKey<String> UI_ADMIN_MAIN_DISABLER = stringKey("ui.admin.main.icons.disabler", "&cMaintenance Mode");
	public static final ConfigKey<String> UI_ADMIN_MAIN_INFO_TITLE = stringKey("ui.admin.main.icons.info.title", "&eGTS Admin Mode");
	public static final ConfigKey<List<String>> UI_ADMIN_MAIN_INFO_LORE = listKey("ui.admin.main.icons.info.lore", Lists.newArrayList(
			"&7Welcome to the GTS Admin Interface.",
			"&7All interactions provided are designed",
			"&7for server operators to effectively",
			"&7control the GTS system from in-game.",
			"",
			"&7Here, you can control &bpublished listings&7,",
			"&bprice management&7, and place the system",
			"&7into &bmaintenance mode&7."
	));

	public static final ConfigKey<List<String>> UI_LISTING_DETAIL_SEPARATOR = listKey("ui.listings.detail-separator", Lists.newArrayList(
			"&8&m-------------------------"
	));
	public static final ConfigKey<List<String>> UI_BIN_DETAILS = listKey("ui.listings.buy-it-now.details", Lists.newArrayList(
			"&7Seller: &e{{gts:seller}}",
			"&7Buy it now: &a{{gts:bin_price}}",
			"",
			"{{gts:listing_status}}",
			"",
			"&eClick to inspect!"
	));
	public static final ConfigKey<List<String>> UI_AUCTION_DETAILS_NO_BIDS = listKey("ui.listings.auctions.details.no-bids", Lists.newArrayList(
			"&7Seller: &e{{gts:seller}}",
			"&7Starting Bid: &e{{gts:auction_start_price}}",
			"",
			"{{gts:listing_status}}",
			"",
			"&eClick to inspect!"
	));
	public static final ConfigKey<List<String>> UI_AUCTION_DETAILS_WITH_SINGLE_BID = listKey("ui.listings.auctions.details.with-single-bid", Lists.newArrayList(
			"&7Seller: &e{{gts:seller}}",
			"&7Bids: &a{{gts:auction_bids}} bid",
			"",
			"&7Top bid: &e{{gts:auction_high_bid}}",
			"&7Bidder: {{gts:auction_high_bidder}}",
			"",
			"{{gts:listing_status}}",
			"",
			"&eClick to inspect!"
	));
	public static final ConfigKey<List<String>> UI_AUCTION_DETAILS_WITH_BIDS = listKey("ui.listings.auctions.details.with-multiple-bids", Lists.newArrayList(
			"&7Seller: &e{{gts:seller}}",
			"&7Bids: &a{{gts:auction_bids}} bids",
			"",
			"&7Top bid: &e{{gts:auction_high_bid}}",
			"&7Bidder: {{gts:auction_high_bidder}}",
			"",
			"{{gts:listing_status}}",
			"",
			"&eClick to inspect!"
	));

	// Player Settings Menu
	public static final ConfigKey<String> UI_PLAYER_SETTINGS_TITLE = stringKey("ui.player-settings.title", "&cGTS &7\u00bb &3User Settings");
	public static final ConfigKey<String> UI_PLAYER_SETTINGS_SETTING_TITLE = stringKey("ui.player-settings.setting-display.title", "&e{{setting}} Notifications");
	public static final ConfigKey<List<String>> UI_PLAYER_SETTINGS_PUBLISH_SETTING_LORE = listKey("ui.player-settings.setting-display.publish", Lists.newArrayList(
			"&7This settings controls whether",
			"&7you'll be informed of new listings",
			"&7that are published to the GTS!"
	));
	public static final ConfigKey<List<String>> UI_PLAYER_SETTINGS_SOLD_SETTING_LORE = listKey("ui.player-settings.setting-display.sold", Lists.newArrayList(
			"&7This settings controls whether",
			"&7you'll be informed when your BIN",
			"&7listings have been purchased!"
	));
	public static final ConfigKey<List<String>> UI_PLAYER_SETTINGS_BID_SETTING_LORE = listKey("ui.player-settings.setting-display.bid", Lists.newArrayList(
			"&7This settings controls whether",
			"&7you'll be informed when your auctions",
			"&7have received a new bid!"
	));
	public static final ConfigKey<List<String>> UI_PLAYER_SETTINGS_OUTBID_SETTING_LORE = listKey("ui.player-settings.setting-display.outbid", Lists.newArrayList(
			"&7This settings controls whether",
			"&7you'll be informed when you've been",
			"&7outbid on an auction you've previously",
			"&7bid on!"
	));
	public static final ConfigKey<String> UI_PLAYER_SETTINGS_SETTING_ENABLED = stringKey("ui.player-settings.setting.enabled", "&aEnabled");
	public static final ConfigKey<String> UI_PLAYER_SETTINGS_SETTING_DISABLED = stringKey("ui.player-settings.setting.disabled", "&cDisabled");
	public static final ConfigKey<String> UI_PLAYER_SETTINGS_SETTING_LOADING = stringKey("ui.player-settings.setting.loading", "&6Loading...");
	public static final ConfigKey<List<String>> UI_PLAYER_SETTINGS_SETTING_TOGGLE_LORE = listKey("ui.player-settings.setting.toggle-lore", Lists.newArrayList(
			"&7Click me to toggle the state",
			"&7of this setting!"
	));

	// Generic Messages
	public static final ConfigKey<String> GENERAL_FEEDBACK_BEGIN_PROCESSING_REQUEST = stringKey("general.feedback.begin-feedback-request", "&7Processing your request...");
	public static final ConfigKey<String> GENERAL_FEEDBACK_FEES_COLLECTION = stringKey("general.feedback.fees-collect", "&7Collecting fees...");
	public static final ConfigKey<String> GENERAL_FEEDBACK_COLLECT_LISTING = stringKey("general.feedback.collect-listing", "&7Collecting your listing...");
	public static final ConfigKey<String> GENERAL_FEEDBACK_RETURN_FEES = stringKey("general.feedback.return-fees-from-failure", "&7Returning fees...");
	public static final ConfigKey<String> GENERAL_FEEDBACK_LISTING_RETURNED = stringKey("general.feedback.listing-returned", "{{gts:prefix}} Your listing has been returned!");
	public static final ConfigKey<String> GENERAL_FEEDBACK_ITEM_CLAIMED = stringKey("general.feedback.item-claimed", "{{gts:prefix}} You claimed your &a{{gts:claim_item}}&7!");
	public static final ConfigKey<String> GENERAL_FEEDBACK_LISTING_FAIL_TO_RETURN = stringKey("general.feedback.listing-fail-to-return", "{{gts:error}} We failed to return your listing... We've kept it in your stash for now!");
	public static final ConfigKey<String> GENERAL_FEEDBACK_AUCTIONS_ALREADY_TOP_BIDDER = stringKey("general.feedback.auctions.already-top-bidder", "{{gts:error}} You already hold the top bid on this auction!");
	public static final ConfigKey<String> GENERAL_FEEDBACK_AUCTIONS_CANT_AFFORD_BID = stringKey("general.feedback.auctions.cant-afford-bid", "{{gts:error}} You're unable to afford that bid...");
	public static final ConfigKey<String> GENERAL_FEEDBACK_BLACKLISTED = stringKey("general.feedback.blacklisted", "{{gts:error}} Your selection is &cblacklisted &7from being listed on the GTS...");
	public static final ConfigKey<String> GENERAL_FEEDBACK_AUCTIONS_OUTBID = stringKey("general.feedback.auctions.outbid", "{{gts:prefix}} &a{{gts:auction_bidder}} &7outbid you by &e{{gts:auction_outbid_amount}} &7for &a{{gts:listing_name}}&7!");
	public static final ConfigKey<String> GENERAL_FEEDBACK_AUCTIONS_NEWBID = stringKey("general.feedback.auctions.new-bid", "{{gts:prefix}} &a{{gts:auction_bidder}} &7bid &e{{gts:auction_bid_amount}} &7for your &a{{gts:listing_name}}&7!");
	public static final ConfigKey<String> GENERAL_FEEDBACK_AUCTIONS_CANCELLED = stringKey("general.feedback.auctions.cancelled", "{{gts:prefix}} Heads up! The auction for {{gts:listing_name}} has been cancelled, so you've been refunded your money!");
	public static final ConfigKey<String> GENERAL_FEEDBACK_PROCESSING_BID = stringKey("general.feedback.processing-bid", "&7Processing bid...");
	public static final ConfigKey<String> GENERAL_FEEDBACK_FUNDS_TO_ESCROW = stringKey("general.feedback.funds-to-escrow", "&7Putting funds in escrow...");
	public static final ConfigKey<String> GENERAL_FEEDBACK_FUNDS_FROM_ESCROW = stringKey("general.feedback.funds-from-escrow", "&7Returning your funds from escrow...");
	public static final ConfigKey<String> GENERAL_FEEDBACK_AUCTIONS_BID_PLACED = stringKey("general.feedback.auctions.bid-placed", "{{gts:prefix}} Your bid of {{gts:auction_bid_amount}} has been placed!");

	public static final ConfigKey<String> REQUEST_FAILED = stringKey("general.requests.failure", "{{gts:prefix}} &7Request failed with status code (&c{{gts:error_code}}&7)");

	public static final ConfigKey<String> UPDATE_AVAILABLE = stringKey("general.update-check.available", "{{gts:prefix}} &7A new update is available (&a{{new_version}}&7), and you are running &e{{current_version}}&7! Check Ore or Discord for the update!");
	public static final ConfigKey<String> UPDATE_LATEST = stringKey("general.update-check.latest", "{{gts:prefix}} You're using the latest version!");
	public static final ConfigKey<String> UPDATE_SNAPSHOT = stringKey("general.update-check.snapshot", "{{gts:prefix}} You're using a snapshot version of GTS, things may not work correctly!");

	private static final Map<String, ConfigKey<?>> KEYS;
	private static final int SIZE;

	static {
		Map<String, ConfigKey<?>> keys = new LinkedHashMap<>();
		Field[] values = MsgConfigKeys.class.getFields();
		int i = 0;

		for (Field f : values) {
			// ignore non-static fields
			if (!Modifier.isStatic(f.getModifiers())) {
				continue;
			}

			// ignore fields that aren't configkeys
			if (!ConfigKey.class.equals(f.getType())) {
				continue;
			}

			try {
				// get the key instance
				BaseConfigKey<?> key = (BaseConfigKey<?>) f.get(null);
				// set the ordinal value of the key.
				key.ordinal = i++;
				// add the key to the return map
				keys.put(f.getName(), key);
			} catch (Exception e) {
				throw new RuntimeException("Exception processing field: " + f, e);
			}
		}

		KEYS = ImmutableMap.copyOf(keys);
		SIZE = i;
	}

	@Override
	public Map<String, ConfigKey<?>> getKeys() {
		return KEYS;
	}

	@Override
	public int getSize() {
		return SIZE;
	}
}
