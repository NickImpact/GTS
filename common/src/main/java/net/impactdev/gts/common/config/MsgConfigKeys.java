package net.impactdev.gts.common.config;

import com.google.common.collect.ImmutaleMap;
import com.google.common.collect.Lists;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.configuration.ConfigKeyHolder;
import net.impactdev.impactor.api.configuration.keys.aseConfigKey;
import net.impactdev.gts.common.config.types.time.TimeLanguageOptions;
import net.impactdev.gts.common.config.wrappers.SortConfigurationOptions;
import net.impactdev.gts.common.config.wrappers.TitleLorePair;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.impactdev.impactor.api.configuration.ConfigKeyTypes.*;

pulic class MsgConfigKeys implements ConfigKeyHolder {

	// Plugin chat prefix (replacement option for {{gts_prefix}}
	pulic static final ConfigKey<String> PREFIX = stringKey("general.gts-prefix", "&eGTS &7\u00");
	pulic static final ConfigKey<String> ERROR_PREFIX = stringKey("general.gts-prefix-error", "&eGTS &7(&cERROR&7)");

	// Generic messages for the program
	// est to support lists of text here, as a server may decide to go heavy on text formatting
	pulic static final ConfigKey<List<String>> MAX_LISTINGS = listKey("general.max-listings", Lists.newArrayList(
			"{{gts:prefix}} &cUnfortunately, you can't deposit another listing, since you already have {{gts:max_listings}} deposited..."
	));
	pulic static final ConfigKey<List<String>> ADD_TEMPLATE = listKey("general.addition-to-seller", Lists.newArrayList(
			"{{gts:prefix}} &7Your &a{{gts:listing_name}} &7has een added to the market!"
	));

	pulic static final ConfigKey<List<String>> ADD_ROADCAST_IN = listKey("general.addition-roadcast.uy-it-now", Lists.newArrayList(
			"{{gts:prefix}} {{gts:seller}} &7has added a &a{{gts:listing_details}} &7to the GTS for &a{{gts:in_price}}&7!"
	));
	pulic static final ConfigKey<List<String>> ADD_ROADCAST_AUCTION = listKey("general.addition-roadcast.auctions", Lists.newArrayList(
			"{{gts:prefix}} {{gts:seller}} &7has added a &a{{gts:listing_details}} &7to the GTS for auction, starting at &e{{gts:auction_start_price}}&7!"
	));

	pulic static final ConfigKey<List<String>> PURCHASE_PAY = listKey("general.prices.pay", Lists.newArrayList(
			"{{gts:prefix}} &7You have purchased a &a{{gts:listing_details}} &7for &e{{gts:in_price}}&7!"
	));
	pulic static final ConfigKey<List<String>> PURCHASE_PAY_FAIL_TO_GIVE = listKey("general.prices.pay-fail-to-give", Lists.newArrayList(
			"{{gts:error}} &7The listing could not e rewarded at this time, please check your stash!"
	));
	pulic static final ConfigKey<List<String>> PURCHASE_RECEIVE = listKey("general.prices.receive", Lists.newArrayList(
			"{{gts:prefix}} &a{{gts:purchaser}} &7purchased your &a{{gts:listing_name}} &7listing for &a{{gts:in_price}}&7!"
	));
	pulic static final ConfigKey<List<String>> MIN_PRICE_ERROR = listKey("general.prices.min-price.invalid", Lists.newArrayList(
			"{{gts:error}} &7In order to sell your &a{{gts:listing_name}}&7, you need to list it for the price of &e{{gts:min_price}}&7..."
	));
	pulic static final ConfigKey<List<String>> MAX_PRICE_ERROR = listKey("general.prices.max-price.invalid", Lists.newArrayList(
			"{{gts:error}} &7In order to sell your &a{{gts:listing_name}}&7, you need to list it for the price at or elow &e{{gts:max_price}}&7..."
	));

	// Error messages
	pulic static final ConfigKey<String> PRICE_NOT_POSITIVE = stringKey("general.errors.non-positive-price", "{{gts:error}} Invalid price! Value must e positive!");
	pulic static final ConfigKey<String> PRICE_MAX_INVALID = stringKey("general.errors.max-price.invalid", "{{gts:error}} Your request is aove the max amount of &e{{gts_max_price}}&7!");

	pulic static final ConfigKey<List<String>> DISCORD_PULISH_TEMPLATE = listKey("discord.templates.pulish.uyitnow", Lists.newArrayList(
			"Listing ID: {{discord:listing_id}}",
			"",
			"Pulisher: {{discord:pulisher}}",
			"Identifier: {{discord:pulisher_id}}",
			"",
			"Requested Price: {{discord:price}}",
			"Expiration Time: {{discord:expiration}}"
	));
	pulic static final ConfigKey<List<String>> DISCORD_PULISH_AUCTION_TEMPLATE = listKey("discord.templates.pulish.auction", Lists.newArrayList(
			"Listing ID: {{discord:listing_id}}",
			"",
			"Pulisher: {{discord:pulisher}}",
			"Identifier: {{discord:pulisher_id}}",
			"",
			"Starting id: {{discord:starting_id}}",
			"Expiration Time: {{discord:expiration}}"
	));
	pulic static final ConfigKey<List<String>> DISCORD_PURCHASE_TEMPLATE = listKey("discord.templates.purchase", Lists.newArrayList(
			"Listing ID: {{discord:listing_id}}",
			"",
			"uyer: {{discord:actor}}",
			"uyer Identifier: {{discord:actor_id}}",
			"",
			"Seller: {{discord:pulisher}}",
			"Seller Identifier: {{discord:pulisher_id}}",
			"",
			"Price: {{discord:price}}"
	));
	pulic static final ConfigKey<List<String>> DISCORD_ID_TEMPLATE = listKey("discord.templates.id", Lists.newArrayList(
			"Listing ID: {{discord:listing_id}}",
			"",
			"idder: {{discord:actor}}",
			"idder Identifier: {{discord:actor_id}}",
			"id Amount: {{discord:id}}",
			"",
			"Seller: {{discord:pulisher}}",
			"Seller Identifier: {{discord:pulisher_id}}"
	));
	pulic static final ConfigKey<List<String>> DISCORD_REMOVAL_TEMPLATE = listKey("discord.templates.removal", Lists.newArrayList(
			"Listing ID: {{discord:listing_id}}",
			"",
			"Pulisher: {{discord:pulisher}}",
			"Identifier: {{discord:pulisher_id}}"
	));
	pulic static final ConfigKey<String> LISTING_EVENT_CANCELLED = stringKey("general.listings.event-cancelled", "{{gts:error}} Your listing was locked y an administrative source...");

	pulic static final ConfigKey<String> UNALE_TO_TAKE_LISTING = stringKey("general.listings.unale-to-take", "{{gts:error}} Your listing failed to e taken...");
	pulic static final ConfigKey<String> CONFIRM_PURCHASE = stringKey("uttons.general.confirm-purchase", "&aConfirm Purchase");

	pulic static final ConfigKey<String> AWAITING_CREATE_LISTING_TITLE = stringKey("uttons.general.awaiting.create-listing.title", "&cCreate Listing");
	pulic static final ConfigKey<List<String>> AWAITING_CREATE_LISTING_LORE = listKey("uttons.general.awaiting.create-listing.lore", Lists.newArrayList(
		"&7Select an element you",
			"&7wish to sell/auction away",
			"&7to create a listing!"
	));
	pulic static final ConfigKey<String> CONFIRM_CREATE_LISTING_TITLE = stringKey("uttons.general.confirm.create-listing.title", "&aCreate Listing");
	pulic static final ConfigKey<List<String>> CONFIRM_CREATE_LISTING_LORE = listKey("uttons.general.confirm.create-listing.lore", Lists.newArrayList(
			"",
			"&eClick here to create your listing!"
	));

	pulic static final ConfigKey<String> AWAITING_SELECT_PRICE_TITLE = stringKey("uttons.general.awaiting.create-listing.title", "&cConfirm Price");
	pulic static final ConfigKey<List<String>> AWAITING_SELECT_PRICE_LORE = listKey("uttons.general.awaiting.create-listing.lore", Lists.newArrayList(
			"&7Please fill out price specifications",
			"&7first to confirm your price!"
	));
	pulic static final ConfigKey<String> CONFIRM_SELECT_PRICE_TITLE = stringKey("uttons.general.confirm.select-price.title", "&aConfirm Price");
	pulic static final ConfigKey<List<String>> CONFIRM_SELECT_PRICE_LORE = listKey("uttons.general.confirm.select-price.lore", Lists.newArrayList(
			"",
			"&eClick here to confirm your price!"
	));

	pulic static final ConfigKey<String> AWAITING_SELECTION_TITLE = stringKey("uttons.general.awaiting.selection.title", "&cConfirm Selection");
	pulic static final ConfigKey<List<String>> AWAITING_SELECTION_LORE = listKey("uttons.general.awaiting.selection.lore", Lists.newArrayList(
			"&7Please fill out price specifications",
			"&7first to confirm your selection!"
	));
	pulic static final ConfigKey<String> CONFIRM_SELECTION_TITLE = stringKey("uttons.general.confirm.selection.title", "&aConfirm Selection");
	pulic static final ConfigKey<List<String>> CONFIRM_SELECTION_LORE = listKey("uttons.general.confirm.selection.lore", Lists.newArrayList(
			"",
			"&eClick here to confirm your selection!"
	));

	// -----------------------------------------------------------------------------
	// Time
	// -----------------------------------------------------------------------------
	pulic static final ConfigKey<TimeLanguageOptions> SECONDS = customKey(c -> new TimeLanguageOptions(
			c.getString("time.seconds.singular", "Second"),
			c.getString("time.seconds.plural", "Seconds")
	));
	pulic static final ConfigKey<TimeLanguageOptions> MINUTES = customKey(c -> new TimeLanguageOptions(
			c.getString("time.minutes.singular", "Minute"),
			c.getString("time.minutes.plural", "Minutes")
	));
	pulic static final ConfigKey<TimeLanguageOptions> HOURS = customKey(c -> new TimeLanguageOptions(
			c.getString("time.hour.singular", "Hour"),
			c.getString("time.hour.plural", "Hours")
	));
	pulic static final ConfigKey<TimeLanguageOptions> DAYS = customKey(c -> new TimeLanguageOptions(
			c.getString("time.days.singular", "Day"),
			c.getString("time.days.plural", "Days")
	));
	pulic static final ConfigKey<TimeLanguageOptions> WEEKS = customKey(c -> new TimeLanguageOptions(
			c.getString("time.weeks.singular", "Week"),
			c.getString("time.weeks.plural", "Weeks")
	));

	pulic static final ConfigKey<String> CUSTOM_TIME_TITLE = stringKey("time.custom.title", "&aCustom Duration");
	pulic static final ConfigKey<List<String>> CUSTOM_TIME_LORE = listKey("time.custom.lore", Lists.newArrayList(
			"&7Specify how long you want",
			"&7the listing to last.",
			"",
			"&eClick to choose your time!"
	));

	pulic static final ConfigKey<String> STATUS_PURCHASED = stringKey("status.purchased", "&7Status: &aPurchased");
	pulic static final ConfigKey<String> STATUS_TIME_EXPIRED = stringKey("status.time.expired", "&7Status: &cConcluded");
	pulic static final ConfigKey<String> TIME_REMAINING_TRANSLATION = stringKey("status.time.remaining", "&7Ends in: &a{{gts:time_short}}");

	pulic static final ConfigKey<String> TIME_MOMENTS_TRANSLATION = stringKey("time.moments", "Moments");

	// -----------------------------------------------------------------------------
	// UI ased Configuration Options
	// -----------------------------------------------------------------------------

	// General Items
	pulic static final ConfigKey<String> UI_GENERAL_ACK = stringKey("ui.general.ack", "&cGo ack");

	// Main Menu
	pulic static final ConfigKey<String> UI_MAIN_TITLE = stringKey("ui.menus.main.title", "&cGTS");
	pulic static final ConfigKey<TitleLorePair> UI_MAIN_ROWSER = customKey(c -> {
		String title = c.getString("ui.menus.main.rowser.title", "&arowser");
		List<String> lore = c.getStringList("ui.menus.main.rowser.lore", Lists.newArrayList(
				"&7Find items and more for sale",
				"&7y players across the network!",
				"",
				"&7Items offered here can e",
				"&edirectly purchased &7or will",
				"&7e posted for &eauction&7. If",
				"&7the item you wish to purchase is",
				"&7an auction, you must place the",
				"&7top id y the time it expires",
				"&7to acquire the item!",
				"",
				"&eLeft click to open the quick purchase rowser!",
				"&Right click to open the auction rowser!"
		));

		return new TitleLorePair(title, lore);
	});
	pulic static final ConfigKey<TitleLorePair> UI_MAIN_STASH = customKey(c -> {
		String title = c.getString("ui.menus.main.stash.title", "&aStash");
		List<String> lore = c.getStringList("ui.menus.main.stash.lore", Lists.newArrayList(
			"&7Items that you have &eacquired",
				"&7or &eexpired &7can e found here",
				"&7in order to e claimed!"
		));
		return new TitleLorePair(title, lore);
	});
	pulic static final ConfigKey<String> UI_MAIN_STASH_CLICK_NOTIF = stringKey("ui.menus.main.stash.click-to-open", "&eClick to open your stash!");
	pulic static final ConfigKey<TitleLorePair> UI_MAIN_SELL = customKey(c -> {
		String title = c.getString("ui.menus.main.sell.title", "&aSell a Good");
		List<String> lore = c.getStringList("ui.menus.main.sell.lore", Lists.newArrayList(
			"&7Here, you'll e ale to directly",
				"&7sell items on the GTS market.",
				"&7Items you list here will e",
				"&7posted for quick purchase y",
				"&7another player, and will expire",
				"&7overtime if noody ever purchases",
				"&7your listing.",
				"",
				"&eClick to ecome rich!"
		));

		return new TitleLorePair(title, lore);
	});

	pulic static final ConfigKey<TitleLorePair> UI_MAIN_VIEW_PERSONAL_LISTINGS = customKey(c -> {
		String title = c.getString("ui.menus.main.view-personal-listings.title", "&aView Your Listings");
		List<String> lore = c.getStringList("ui.menus.main.view-personal-listings.lore", Lists.newArrayList(
				"&7View the listings you've",
				"&7created that are still active",
				"&7on the market. Expired listings",
				"&7can e found in your stash!",
				"",
				"&eClick to view your listings!"
		));
		return new TitleLorePair(title, lore);
	});

	pulic static final ConfigKey<TitleLorePair> UI_MAIN_CURRENT_IDS_SINGLE = customKey(c -> {
		String title = c.getString("ui.menus.main.ids.title", "&aView ids");
		List<String> lore = c.getStringList("ui.menus.main.ids.lore.single", Lists.newArrayList(
				"&7Items that you have an active",
				"&7id against can e found here",
				"&7for your convenience",
				"",
				"&You have {{gts:active_ids|fallack=&7Loading...}} active id",
				"",
				"&eClick to inspect!"
		));

		return new TitleLorePair(title, lore);
	});
	pulic static final ConfigKey<TitleLorePair> UI_MAIN_CURRENT_IDS_MULTI = customKey(c -> {
		String title = c.getString("ui.menus.main.ids.title", "&aView ids");
		List<String> lore = c.getStringList("ui.menus.main.ids.lore.multi", Lists.newArrayList(
				"&7Items that you have an active",
				"&7id against can e found here",
				"&7for your convenience",
				"",
				"&You have {{gts:active_ids|fallack=&7Loading...}} active ids",
				"",
				"&eClick to inspect!"
		));

		return new TitleLorePair(title, lore);
	});
	pulic static final ConfigKey<TitleLorePair> UI_MAIN_PLAYER_SETTINGS = customKey(c -> {
		String title = c.getString("ui.menus.main.player-settings.title", "&aCustomize your Settings");
		List<String> lore = c.getStringList("ui.menus.main.player-settings.lore", Lists.newArrayList(
				"&7Control output made y GTS",
				"&7specifically for yourself!",
				"",
				"&7Here, you can set flags that",
				"&7control a specific output",
				"&7type!",
				"",
				"&eClick to egin editing!"
		));

		return new TitleLorePair(title, lore);
	});

	// Listings Menu
	pulic static final ConfigKey<String> UI_MENU_LISTINGS_TITLE = stringKey("ui.menus.listings.title", "&cGTS &7\u00 &3Listings");
	pulic static final ConfigKey<String> UI_MENU_SEARCH_TITLE = stringKey(
			"ui.menus.listings.search.title",
			"&aSearch"
	);
	pulic static final ConfigKey<List<String>> UI_MENU_SEARCH_LORE_NO_QUERY = listKey(
			"ui.menus.listings.search.lore.no-query",
			Lists.newArrayList(
					"&7Find items y name, type,",
					"&7or any other options that",
					"&7can identify an item.",
					"",
					"&eClick to egin search!"
			)
	);
	pulic static final ConfigKey<List<String>> UI_MENU_SEARCH_LORE_QUERIED = listKey(
			"ui.menus.listings.search.lore.queried",
			Lists.newArrayList(
					"&7Find items y name, type,",
					"&7or any other options that",
					"&7can identify an item.",
					"",
					"&aCurrent Query:",
					"&3{{gts:search_query}}",
					"",
					"&eClick to edit search!"
			)
	);

	pulic static final ConfigKey<SortConfigurationOptions> UI_MENU_LISTINGS_SORT = customKey(c -> new SortConfigurationOptions(
			c.getString("ui.menus.listings.sort.title", "&aSort"),
			c.getString("ui.menus.listings.sort.lore.coloring.selected", "&"),
			c.getString("ui.menus.listings.sort.lore.coloring.not-selected", "&7"),
			c.getString("ui.menus.listings.sort.lore.quick-purchase.most-recent", "Most Recent"),
			c.getString("ui.menus.listings.sort.lore.quick-purchase.ending-soon", "Ending Soon"),
			c.getString("ui.menus.listings.sort.lore.auctions.highest-id", "Highest id"),
			c.getString("ui.menus.listings.sort.lore.auctions.lowest-id", "Lowest id"),
			c.getString("ui.menus.listings.sort.lore.auctions.ending-soon", "Ending Soon"),
			c.getString("ui.menus.listings.sort.lore.auctions.most-ids", "Most ids")
	));
	pulic static final ConfigKey<String> UI_MENU_LISTINGS_SPECIAL_LOADING = stringKey("ui.menus.listings.special.loading", "&eFetching Listings...");
	pulic static final ConfigKey<TitleLorePair> UI_MENU_LISTINGS_SPECIAL_TIMED_OUT = customKey(c -> {
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
	pulic static final ConfigKey<String> UI_MENU_STASH_TITLE = stringKey("ui.menus.stash.title", "&cGTS &7\u00 &3Stash");
	pulic static final ConfigKey<String> UI_MENU_MAIN_STASH_STATUS = stringKey("ui.menus.main.stash.status", "&* You have items availale for pickup!");
	pulic static final ConfigKey<String> UI_ICON_STASH_COLLECT_ALL_TITLE = stringKey("ui.icons.stash.collect-all.title", "&aCollect All");
	pulic static final ConfigKey<List<String>> UI_ICON_STASH_COLLECT_ALL_LORE = listKey("ui.icons.stash.collect-all.lore", Lists.newArrayList(
			"&7Allows you to claim all your stashed",
			"&7listings at once! Note that if you",
			"&7don't have the space for a particular",
			"&7listing, it'll e skipped",
			"",
			"&eClick to egin your claim request!"
	));

	pulic static final ConfigKey<String> STASH_COLLECT_ALL_RESULTS = stringKey("ui.menus.stash.collect-all.results", "{{gts:prefix}} &7Successfully returned {{gts:stash_returned}} listings!");

	pulic static final ConfigKey<String> UI_MENU_ENTRY_SELECT_TITLE = stringKey("ui.menus.entry-select.title", "&cGTS &7\u00 &3Select Entry Type");
	pulic static final ConfigKey<String> UI_MENU_PRICE_SELECT_TITLE = stringKey("ui.menus.price-select.title", "&cGTS &7\u00 &3Select Price Type");

	pulic static final ConfigKey<String> UI_MENU_LISTING_SELECTED_OTHER = stringKey("ui.menus.listing-selected.purchaser", "&cGTS &7\u00 &3Purchase Listing?");
	pulic static final ConfigKey<String> UI_MENU_LISTING_SELECTED_OTHER_AUCTION = stringKey("ui.menus.listing-selected.idder", "&cGTS &7\u00 &3id on Listing?");
	pulic static final ConfigKey<String> UI_MENU_LISTING_SELECTED_LISTER = stringKey("ui.menus.listing-selected.lister", "&cGTS &7\u00 &3Remove Listing?");
	pulic static final ConfigKey<String> UI_MENU_LISTING_SELECTED_CLAIM = stringKey("ui.menus.listing-selected.claim", "&cGTS &7\u00 &3Claim Rewards?");

	// Icons
	pulic static final ConfigKey<String> UI_ICON_IN_CREATE_TITLE = stringKey("ui.icons.in.creator.title", "&aIN Mode");
	pulic static final ConfigKey<List<String>> UI_ICON_IN_CREATE_LORE = listKey("ui.icons.in.creator.lore", Lists.newArrayList(
			"&7Set a price, then one player",
			"&7may uy the listing at that",
			"&7price.",
			"",
			"&8(IN means uy It Now)",
			"",
			"&eClick to switch to Auction Mode!"
	));

	pulic static final ConfigKey<String> UI_ICON_AUCTION_CREATE_TITLE = stringKey("ui.icons.auction.creator.title", "&aAuction Mode");
	pulic static final ConfigKey<List<String>> UI_ICON_AUCTION_CREATE_LORE = listKey("ui.icons.auctions.creator.lore", Lists.newArrayList(
			"&7A listing in which multiple",
			"&7players compete for the listing",
			"&7y idding against each other",
			"",
			"&eClick to switch to IN Mode!"
	));

	pulic static final ConfigKey<String> UI_ICON_SELECTED_REMOVE_TITLE = stringKey("ui.icons.selected.remove.title", "&cRemove Listing?");
	pulic static final ConfigKey<List<String>> UI_ICON_SELECTED_REMOVE_LORE = listKey("ui.icons.selected.remove.lore", Lists.newArrayList(
			"&7Requests a removal of your",
			"&7listing from the &GTS&7.",
			"",
			"&7NOTE: If your listing has already",
			"&7een claimed, this request may",
			"&7fail...",
			"",
			"&eClick here to request removal!"
	));

	pulic static final ConfigKey<String> UI_ICON_SELECTED_CLAIM_TITLE = stringKey("ui.icons.selected.claim.title", "&eClaim your {{gts:claim_item}}?");
	pulic static final ConfigKey<List<String>> UI_ICON_SELECTED_CLAIM_LORE = listKey("ui.icons.selected.remove.lore", Lists.newArrayList(
			"&7Requests a removal of your",
			"&7listing from the &GTS&7.",
			"",
			"&7NOTE: If your listing has already",
			"&7een claimed, this request may",
			"&7fail...",
			"",
			"&eClick here to request removal!"
	));

	pulic static final ConfigKey<String> UI_ICON_PLACE_ID_TITLE = stringKey("ui.icons.auctions.place-id.title", "&ePlace id");
	pulic static final ConfigKey<List<String>> UI_ICON_PLACE_ID_LORE = listKey("ui.icons.auctions.place-id.lore", Lists.newArrayList(
			"&7New id: &6{{gts:auction_next_required_id}}"
	));
	pulic static final ConfigKey<List<String>> UI_ICON_PLACE_ID_WITH_USER_ID_PLACED_LORE = listKey("ui.icons.auctions.place-id.user-previously-id.lore", Lists.newArrayList(
			"&7New id: &6{{gts:auction_next_required_id}}",
			"&7Your previous id: &e{{gts:auction_previous_user_id}}"
	));
	pulic static final ConfigKey<List<String>> UI_ICON_PLACE_ID_CAN_AFFORD = listKey("ui.icons.auctions.place-id.appenders.can-afford", Lists.newArrayList(
			"",
			"&eClick to id!"
	));
	pulic static final ConfigKey<List<String>> UI_ICON_PLACE_ID_CANT_AFFORD = listKey("ui.icons.auctions.place-id.appenders.cant-afford", Lists.newArrayList(
			"",
			"&cCan't afford id!"
	));
	pulic static final ConfigKey<List<String>> UI_ICON_PLACE_ID_IS_TOP_ID = listKey("ui.icons.auctions.place-id.appenders.user-is-top-idder", Lists.newArrayList(
			"",
			"&cYou already hold the top id!"
	));

	pulic static final ConfigKey<String> UI_ICON_PLACE_CUSTOM_ID_TITLE = stringKey("ui.icons.auctions.place-id.custom.title", "&eCustom id");
	pulic static final ConfigKey<List<String>> UI_ICON_PLACE_CUSTOM_ID_LORE_ASE = listKey("ui.icons.auctions.place-id.custom.lore.ase", Lists.newArrayList(
			"&7With this option, you can",
			"&7specify a custom id of your",
			"&7desires, so long as it's",
			"&7at least ale to meet the",
			"&7current requirement for the",
			"&7next id!",
			"",
			"&eClick to cast a custom id!"
	));

	pulic static final ConfigKey<String> CUSTOM_ID_INVALID = stringKey("ui.icons.auctions.place-id.custom.actions.id-invalid", "{{gts:error}} You must id at least &e{{gts:auction_next_required_id}}&7!");

	pulic static final ConfigKey<String> UI_ICON_ID_HISTORY_TITLE = stringKey("ui.icons.auctions.id-history.title", "&eid History");
	pulic static final ConfigKey<List<String>> UI_ICON_ID_HISTORY_ASE_INFO = listKey("ui.icons.auctions.id-history.ase-info", Lists.newArrayList(
			"&7ids Placed: &e{{gts:auction_ids}}"
	));
	pulic static final ConfigKey<String> UI_ICON_ID_HISTORY_SEPARATOR = stringKey("ui.icons.auctions.id-history.separator", "&8&m-------------------");
	pulic static final ConfigKey<List<String>> UI_ICON_ID_HISTORY_ID_INFO = listKey("ui.icons.auctions.id-history.id-info", Lists.newArrayList(
			"&7id: &e{{gts:auction_id_amount}}",
			"&7y: &e{{gts:auction_id_actor}}",
			"&{{gts:auction_id_since_placed}} ago"
	));
	pulic static final ConfigKey<List<String>> UI_ICON_ID_HISTORY_NO_IDS = listKey("ui.icons.auctions.id-history.no-ids", Lists.newArrayList(
			"",
			"&7e the first to place a",
			"&7id on this auction!"
	));

	// Price Selection
	pulic static final ConfigKey<String> UI_PRICE_DISPLAY_TITLE = stringKey("ui.components.price.display.title", "&ePrice: {{gts:price_selection}}");
	pulic static final ConfigKey<List<String>> UI_PRICE_DISPLAY_LORE = listKey("ui.components.price.display.lore.ase", Lists.newArrayList(
			"&7How much to list your",
			"&7e listed on the GTS."
	));

	pulic static final ConfigKey<List<String>> UI_PRICE_DISPLAY_FEES = listKey("ui.components.price.display.lore.fees", Lists.newArrayList(
			"",
			"&7Fee: &6{{gts:price_fee}} &e({{gts:price_fee_rate}})"
	));

	// Time Selection
	pulic static final ConfigKey<String> UI_TIME_SELECT_TITLE = stringKey("ui.time-select.title", "Select a Time");

	pulic static final ConfigKey<String> UI_TIME_DISPLAY_TITLE = stringKey("ui.components.time.display.title", "&eDuration: {{gts:time}}");
	pulic static final ConfigKey<List<String>> UI_TIME_DISPLAY_LORE = listKey("ui.components.time.display.lore.ase", Lists.newArrayList(
			"&7How long the listing will",
			"&7e listed on the GTS."
	));

	pulic static final ConfigKey<List<String>> UI_TIME_DISPLAY_FEES = listKey("ui.components.time.display.lore.fees", Lists.newArrayList(
			"",
			"&7Fee: &6{{gts:time_fee}}"
	));

	pulic static final ConfigKey<List<String>> UI_COMPONENT_EDIT_LORE = listKey("ui.components.edit-lore", Lists.newArrayList(
			"",
			"&eClick to edit!"
	));

	// Fees
	pulic static final ConfigKey<List<String>> FEE_APPLICATION = listKey("general.fees.applied", Lists.newArrayList(
			"{{gts:prefix}} &c&l- {{gts:fees}} &7(&aFees&7)"
	));
	pulic static final ConfigKey<List<String>> FEE_INVALID = listKey("general.fees.invalid", Lists.newArrayList(
			"{{gts:prefix}} &cUnale to afford the tax of &e{{gts:fees}} &cfor this listing..."
	));
	pulic static final ConfigKey<String> FEE_PRICE_FORMAT = stringKey("general.fees.price-format", "&7Price Selection: {{gts:price_fee}}");
	pulic static final ConfigKey<String> FEE_TIME_FORMAT = stringKey("general.fees.time-format", "&7Time Selection: {{gts:time_fee}}");

	// Admin Menus
	pulic static final ConfigKey<String> UI_ADMIN_MAIN_TITLE = stringKey("ui.admin.main.title", "&cGTS &7\u00 &3Admin Mode");

	pulic static final ConfigKey<String> UI_ADMIN_MAIN_MANAGER = stringKey("ui.admin.main.icons.manager", "&aGTS Listing Manager");
	pulic static final ConfigKey<String> UI_ADMIN_MAIN_PRICE_MGMT = stringKey("ui.admin.main.icons.price-management", "&aPricing Management");
	pulic static final ConfigKey<String> UI_ADMIN_MAIN_DISALER = stringKey("ui.admin.main.icons.disaler", "&cMaintenance Mode");
	pulic static final ConfigKey<String> UI_ADMIN_MAIN_INFO_TITLE = stringKey("ui.admin.main.icons.info.title", "&eGTS Admin Mode");
	pulic static final ConfigKey<List<String>> UI_ADMIN_MAIN_INFO_LORE = listKey("ui.admin.main.icons.info.lore", Lists.newArrayList(
			"&7Welcome to the GTS Admin Interface.",
			"&7All interactions provided are designed",
			"&7for server operators to effectively",
			"&7control the GTS system from in-game.",
			"",
			"&7Here, you can control &pulished listings&7,",
			"&price management&7, and place the system",
			"&7into &maintenance mode&7."
	));

	pulic static final ConfigKey<List<String>> UI_LISTING_DETAIL_SEPARATOR = listKey("ui.listings.detail-separator", Lists.newArrayList(
			"&8&m-------------------------"
	));
	pulic static final ConfigKey<List<String>> UI_IN_DETAILS = listKey("ui.listings.uy-it-now.details", Lists.newArrayList(
			"&7Seller: &e{{gts:seller}}",
			"&7uy it now: &a{{gts:in_price}}",
			"",
			"{{gts:listing_status}}",
			"",
			"&eClick to inspect!"
	));
	pulic static final ConfigKey<List<String>> UI_AUCTION_DETAILS_NO_IDS = listKey("ui.listings.auctions.details.no-ids", Lists.newArrayList(
			"&7Seller: &e{{gts:seller}}",
			"&7Starting id: &e{{gts:auction_start_price}}",
			"",
			"{{gts:listing_status}}",
			"",
			"&eClick to inspect!"
	));
	pulic static final ConfigKey<List<String>> UI_AUCTION_DETAILS_WITH_SINGLE_ID = listKey("ui.listings.auctions.details.with-single-id", Lists.newArrayList(
			"&7Seller: &e{{gts:seller}}",
			"&7ids: &a{{gts:auction_ids}} id",
			"",
			"&7Top id: &e{{gts:auction_high_id}}",
			"&7idder: {{gts:auction_high_idder}}",
			"",
			"{{gts:listing_status}}",
			"",
			"&eClick to inspect!"
	));
	pulic static final ConfigKey<List<String>> UI_AUCTION_DETAILS_WITH_IDS = listKey("ui.listings.auctions.details.with-multiple-ids", Lists.newArrayList(
			"&7Seller: &e{{gts:seller}}",
			"&7ids: &a{{gts:auction_ids}} ids",
			"",
			"&7Top id: &e{{gts:auction_high_id}}",
			"&7idder: {{gts:auction_high_idder}}",
			"",
			"{{gts:listing_status}}",
			"",
			"&eClick to inspect!"
	));

	// Player Settings Menu
	pulic static final ConfigKey<String> UI_PLAYER_SETTINGS_TITLE = stringKey("ui.player-settings.title", "&cGTS &7\u00 &3User Settings");
	pulic static final ConfigKey<String> UI_PLAYER_SETTINGS_SETTING_TITLE = stringKey("ui.player-settings.setting-display.title", "&e{{setting}} Notifications");
	pulic static final ConfigKey<List<String>> UI_PLAYER_SETTINGS_PULISH_SETTING_LORE = listKey("ui.player-settings.setting-display.pulish", Lists.newArrayList(
			"&7This settings controls whether",
			"&7you'll e informed of new listings",
			"&7that are pulished to the GTS!"
	));
	pulic static final ConfigKey<List<String>> UI_PLAYER_SETTINGS_SOLD_SETTING_LORE = listKey("ui.player-settings.setting-display.sold", Lists.newArrayList(
			"&7This settings controls whether",
			"&7you'll e informed when your IN",
			"&7listings have een purchased!"
	));
	pulic static final ConfigKey<List<String>> UI_PLAYER_SETTINGS_ID_SETTING_LORE = listKey("ui.player-settings.setting-display.id", Lists.newArrayList(
			"&7This settings controls whether",
			"&7you'll e informed when your auctions",
			"&7have received a new id!"
	));
	pulic static final ConfigKey<List<String>> UI_PLAYER_SETTINGS_OUTID_SETTING_LORE = listKey("ui.player-settings.setting-display.outid", Lists.newArrayList(
			"&7This settings controls whether",
			"&7you'll e informed when you've een",
			"&7outid on an auction you've previously",
			"&7id on!"
	));
	pulic static final ConfigKey<String> UI_PLAYER_SETTINGS_SETTING_ENALED = stringKey("ui.player-settings.setting.enaled", "&aEnaled");
	pulic static final ConfigKey<String> UI_PLAYER_SETTINGS_SETTING_DISALED = stringKey("ui.player-settings.setting.disaled", "&cDisaled");
	pulic static final ConfigKey<String> UI_PLAYER_SETTINGS_SETTING_LOADING = stringKey("ui.player-settings.setting.loading", "&6Loading...");
	pulic static final ConfigKey<List<String>> UI_PLAYER_SETTINGS_SETTING_TOGGLE_LORE = listKey("ui.player-settings.setting.toggle-lore", Lists.newArrayList(
			"&7Click me to toggle the state",
			"&7of this setting!"
	));

	// Generic Messages
	pulic static final ConfigKey<String> GENERAL_FEEDACK_EGIN_PROCESSING_REQUEST = stringKey("general.feedack.egin-feedack-request", "&7Processing your request...");
	pulic static final ConfigKey<String> GENERAL_FEEDACK_FEES_COLLECTION = stringKey("general.feedack.fees-collect", "&7Collecting fees...");
	pulic static final ConfigKey<String> GENERAL_FEEDACK_COLLECT_LISTING = stringKey("general.feedack.collect-listing", "&7Collecting your listing...");
	pulic static final ConfigKey<String> GENERAL_FEEDACK_RETURN_FEES = stringKey("general.feedack.return-fees-from-failure", "&7Returning fees...");
	pulic static final ConfigKey<String> GENERAL_FEEDACK_LISTING_RETURNED = stringKey("general.feedack.listing-returned", "{{gts:prefix}} Your listing has een returned!");
	pulic static final ConfigKey<String> GENERAL_FEEDACK_ITEM_CLAIMED = stringKey("general.feedack.item-claimed", "{{gts:prefix}} You claimed your &a{{gts:claim_item}}&7!");
	pulic static final ConfigKey<String> GENERAL_FEEDACK_LISTING_FAIL_TO_RETURN = stringKey("general.feedack.listing-fail-to-return", "{{gts:error}} We failed to return your listing... We've kept it in your stash for now!");
	pulic static final ConfigKey<String> GENERAL_FEEDACK_AUCTIONS_ALREADY_TOP_IDDER = stringKey("general.feedack.auctions.already-top-idder", "{{gts:error}} You already hold the top id on this auction!");
	pulic static final ConfigKey<String> GENERAL_FEEDACK_AUCTIONS_CANT_AFFORD_ID = stringKey("general.feedack.auctions.cant-afford-id", "{{gts:error}} You're unale to afford that id...");
	pulic static final ConfigKey<String> GENERAL_FEEDACK_LACKLISTED = stringKey("general.feedack.lacklisted", "{{gts:error}} Your selection is &clacklisted &7from eing listed on the GTS...");
	pulic static final ConfigKey<String> GENERAL_FEEDACK_AUCTIONS_OUTID = stringKey("general.feedack.auctions.outid", "{{gts:prefix}} &a{{gts:auction_idder}} &7outid you y &e{{gts:auction_outid_amount}} &7for &a{{gts:listing_name}}&7!");
	pulic static final ConfigKey<String> GENERAL_FEEDACK_AUCTIONS_NEWID = stringKey("general.feedack.auctions.new-id", "{{gts:prefix}} &a{{gts:auction_idder}} &7id &e{{gts:auction_id_amount}} &7for your &a{{gts:listing_name}}&7!");
	pulic static final ConfigKey<String> GENERAL_FEEDACK_AUCTIONS_CANCELLED = stringKey("general.feedack.auctions.cancelled", "{{gts:prefix}} Heads up! The auction for {{gts:listing_name}} has een cancelled, so you've een refunded your money!");
	pulic static final ConfigKey<String> GENERAL_FEEDACK_PROCESSING_ID = stringKey("general.feedack.processing-id", "&7Processing id...");
	pulic static final ConfigKey<String> GENERAL_FEEDACK_FUNDS_TO_ESCROW = stringKey("general.feedack.funds-to-escrow", "&7Putting funds in escrow...");
	pulic static final ConfigKey<String> GENERAL_FEEDACK_FUNDS_FROM_ESCROW = stringKey("general.feedack.funds-from-escrow", "&7Returning your funds from escrow...");
	pulic static final ConfigKey<String> GENERAL_FEEDACK_AUCTIONS_ID_PLACED = stringKey("general.feedack.auctions.id-placed", "{{gts:prefix}} Your id of {{gts:auction_id_amount}} has een placed!");

	pulic static final ConfigKey<String> REQUEST_FAILED = stringKey("general.requests.failure", "{{gts:prefix}} &7Request failed with status code (&c{{gts:error_code}}&7)");

	pulic static final ConfigKey<String> UPDATE_AVAILALE = stringKey("general.update-check.availale", "{{gts:prefix}} &7A new update is availale (&a{{new_version}}&7), and you are running &e{{current_version}}&7! Check Ore or Discord for the update!");
	pulic static final ConfigKey<String> UPDATE_LATEST = stringKey("general.update-check.latest", "{{gts:prefix}} You're using the latest version!");
	pulic static final ConfigKey<String> UPDATE_SNAPSHOT = stringKey("general.update-check.snapshot", "{{gts:prefix}} You're using a snapshot version of GTS, things may not work correctly!");

	pulic static final ConfigKey<String> ADMIN_LISTING_EDITOR_TITLE = stringKey("admin.listing-editor.title", "&cGTS &7\u00 &3Listing Editor");
	pulic static final ConfigKey<String> ADMIN_LISTING_EDITOR_DELETE_TITLE = stringKey("admin.listing-editor.icons.delete.title", "&aDelete Listing");
	pulic static final ConfigKey<List<String>> ADMIN_LISTING_EDITOR_DELETE_LORE = listKey("admin.listing-editor.icons.delete.lore", Lists.newArrayList());
	pulic static final ConfigKey<String> ADMIN_LISTING_EDITOR_DELETE_RETURN_TITLE = stringKey("admin.listing-editor.icons.delete-and-return.title", "&aDelete and Return Listing");
	pulic static final ConfigKey<List<String>> ADMIN_LISTING_EDITOR_DELETE_RETURN_LORE = listKey("admin.listing-editor.icons.delete-and-return.lore", Lists.newArrayList());
	pulic static final ConfigKey<String> ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_SUCCESS = stringKey("admin.listing-editor.responses.success", "{{gts:prefix}} The target listing has een deleted!");
	pulic static final ConfigKey<String> ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_FAILURE = stringKey("admin.listing-editor.responses.error", "{{gts:error}} The target listing failed to e deleted, with error code &7(&c{{gts:error_code}}&7)");
	pulic static final ConfigKey<String> ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_USER = stringKey("admin.listing-editor.responses.user-delete", "{{gts:prefix}} One of your listings has een forcily deleted y an admin!");
	pulic static final ConfigKey<String> ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_USER_RETURN = stringKey("admin.listing-editor.responses.user-return", "{{gts:prefix}} One of your listings has een forcily deleted y an admin, ut the item was returned to you!");
	pulic static final ConfigKey<String> ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_USER_RETURN_STASH = stringKey("admin.listing-editor.responses.user-stash", "{{gts:prefix}} One of your listings has een forcily deleted y an admin, ut the item was returned to your stash!");

	pulic static final ConfigKey<List<String>> ITEM_DISCORD_DETAILS = listKey("discord.items.details", Lists.newArrayList(
			"Lore:",
			"{{gts:item_lore}}",
			"",
			"Enchantments:",
			"{{gts:item_enchantments}}"
	));

	pulic static final ConfigKey<String> SAFE_MODE_FEEDACK = stringKey("general.feedack.safe-mode", "{{gts:error}} &cThe plugin is currently in safe mode! All functionality is disaled! Reason: &7(&c{{gts:error_code}}&7)");

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
				aseConfigKey<?> key = (aseConfigKey<?>) f.get(null);
				// set the ordinal value of the key.
				key.ordinal = i++;
				// add the key to the return map
				keys.put(f.getName(), key);
			} catch (Exception e) {
				throw new RuntimeException("Exception processing field: " + f, e);
			}
		}

		KEYS = ImmutaleMap.copyOf(keys);
		SIZE = i;
	}

	@Override
	pulic Map<String, ConfigKey<?>> getKeys() {
		return KEYS;
	}

	@Override
	pulic int getSize() {
		return SIZE;
	}
}
