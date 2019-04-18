package me.nickimpact.gts.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import com.nickimpact.impactor.api.configuration.ConfigKeyHolder;
import com.nickimpact.impactor.api.configuration.keys.BaseConfigKey;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.nickimpact.impactor.api.configuration.ConfigKeyTypes.*;

public class MsgConfigKeys implements ConfigKeyHolder {

	// Plugin chat prefix (replacement option for {{gts_prefix}}
	public static final ConfigKey<String> PREFIX = stringKey("general.gts-prefix", "&eGTS &7\u00bb");
	public static final ConfigKey<String> ERROR_PREFIX = stringKey("general.gts-prefix-error", "&eGTS &7(&cERROR&7)");

	// Generic messages for the program
	// Best to support lists of text here, as a server may decide to go heavy on text formatting
	public static final ConfigKey<List<String>> MAX_LISTINGS = listKey("general.max-listings", Lists.newArrayList(
			"{{gts_prefix}} &cUnfortunately, you can't deposit another listing, since you already have {{max_listings}} deposited..."
	));
	public static final ConfigKey<List<String>> ADD_TEMPLATE = listKey("general.addition-to-seller", Lists.newArrayList(
			"{{gts_prefix}} &7Your &a{{listing_name}} &7has been added to the market!"
	));
	public static final ConfigKey<List<String>> TAX_APPLICATION = listKey("general.taxes.applied", Lists.newArrayList(
			"&c&l- {{tax}} &7(&aTaxes&7)"
	));
	public static final ConfigKey<List<String>> TAX_INVALID = listKey("general.taxes.invalid", Lists.newArrayList(
			"{{gts_prefix}} &cUnable to afford the tax of &e{{tax}} &cfor this listing..."
	));
	public static final ConfigKey<List<String>> ADD_BROADCAST = listKey("general.addition-broadcast", Lists.newArrayList(
			"{{gts_prefix}} &c{{player}} &7has added a &a{{listing_specifics}} &7to the GTS for &a{{price}}&7!"
	));
	public static final ConfigKey<List<String>> PURCHASE_PAY = listKey("general.prices.pay", Lists.newArrayList(
			"{{gts_prefix}} &7You have purchased a &a{{listing_specifics}} &7for &e{{price}}&7!"
	));
	public static final ConfigKey<List<String>> PURCHASE_RECEIVE = listKey("general.prices.receive", Lists.newArrayList(
			"{{gts_prefix}} &a{{buyer}} &7purchased your &a{{listing_name}} &7listing for &a{{price}}&7!"
	));
	public static final ConfigKey<List<String>> AUCTION_BID_BROADCAST = listKey("general.auctions.bid", Lists.newArrayList(
			"{{gts_prefix}} &e{{player}} &7has placed a bid on the &a{{listing_specifics}}!"
	));
	public static final ConfigKey<List<String>> AUCTION_BID = listKey("general.auctions.bid-personal", Lists.newArrayList(
			"{{gts_prefix}} &7Your bid has been placed! If you win, you will pay &e{{price}}&7!"
	));
	public static final ConfigKey<List<String>> AUCTION_WIN_BROADCAST = listKey("general.auctions.win", Lists.newArrayList(
			"{{gts_prefix}} &e{{player}} &7has won the auction for the &a{{listing_specifics}}!"
	));
	public static final ConfigKey<List<String>> AUCTION_WIN = listKey("general.auctions.win-personal", Lists.newArrayList(
			"{{gts_prefix}} &7Congrats! You've won the auction on the &e{{listing_specifics}} &7for &a{{price}}&7!"
	));
	public static final ConfigKey<List<String>> AUCTION_SOLD = listKey("general.auctions.sold", Lists.newArrayList(
			"{{gts_prefix}} &7Your &e{{listing_specifics}} &7auction was sold to &e{{high_bidder}} &7for &a{{price}}&7!"
	));
	public static final ConfigKey<List<String>> AUCTION_IS_HIGH_BIDDER = listKey("general.auctions.is-high-bidder", Lists.newArrayList(
			"{{gts_prefix}} &cHold off! You wouldn't want to bid against yourself!"
	));
	public static final ConfigKey<List<String>> REMOVAL_CHOICE = listKey("general.removal.choice", Lists.newArrayList(
			"{{gts_prefix}} &7Your &a{{listing_name}} &7listing has been returned!"
	));
	public static final ConfigKey<List<String>> REMOVAL_EXPIRES = listKey("general.removal.expires", Lists.newArrayList(
			"{{gts_prefix}} &7Your &a{{listing_name}} &7listing has expired, and has thus been returned!"
	));
	public static final ConfigKey<List<String>> MIN_PRICE_ERROR = listKey("general.prices.min-price.invalid", Lists.newArrayList(
			"{{gts_error}} &7In order to sell your &a{{listing_name}}&7, you need to list it for the price of &e{{min_price}}&7..."
	));

	// Items
	public static final ConfigKey<String> UI_ITEMS_PLAYER_LISTINGS_TITLE = stringKey("item-displays.player-listings.title", "&eYour Listings");
	public static final ConfigKey<List<String>> UI_ITEMS_PLAYER_LISTINGS_LORE = listKey("item-displays.player-listings.lore", Lists.newArrayList());

	// Entries
	public static final ConfigKey<List<String>> ENTRY_INFO = listKey("entries.base-info", Lists.newArrayList(
			"",
			"&7Price: &e{{price}}",
			"&7Time Left: &e{{time_left}}"
	));
	public static final ConfigKey<List<String>> AUCTION_INFO = listKey("entries.auction-info", Lists.newArrayList(
			"",
			"&7High Bidder: &e{{high_bidder}}",
			"&7Current Price: &e{{auc_price}}",
			"&7Increment: &e{{increment}}",
			"&7Time Left: &e{{time_left}}"
	));

	// Error messages
	public static final ConfigKey<List<String>> NOT_ENOUGH_FUNDS = listKey("general.purchase.not-enough-funds", Lists.newArrayList("&cUnfortunately, you were unable to afford the price of {{price}}"));
	public static final ConfigKey<List<String>> ALREADY_CLAIMED = listKey("general.purchase.already-claimed", Lists.newArrayList("&cUnfortunately, this listing has already been claimed..."));
	public static final ConfigKey<List<String>> ITEM_ENTRY_BASE_LORE = listKey("entries.item.base.lore", Lists.newArrayList(
			"&7Seller: &e{{seller}}"
	));
	public static final ConfigKey<List<String>> ITEM_ENTRY_CONFIRM_LORE = listKey("entries.item.confirm.lore", Lists.newArrayList(
			"&7Seller: &e{{seller}}"
	));
	public static final ConfigKey<String> ITEM_ENTRY_CONFIRM_TITLE = stringKey("entries.item.confirm.title", "&ePurchase {{item_title}}?");
	public static final ConfigKey<String> ITEM_ENTRY_BASE_TITLE = stringKey("entries.item.base.title", "{{item_title}}");
	public static final ConfigKey<String> ITEM_ENTRY_SPEC_TEMPLATE = stringKey("entries.item.spec-template", "{{item_title}}");

	public static final ConfigKey<String> DISCORD_PURCHASE = stringKey("discord.purchase", "{{buyer}} just purchased a {{listing_specifics}} from {{seller}} for {{price}}");
	public static final ConfigKey<String> DISCORD_REMOVE = stringKey("discord.purchase", "{{player}} has removed their {{listing_specifics}} from the GTS!");

	public static final ConfigKey<String> REFRESH_ICON = stringKey("item-displays.refresh-icon.title", "&eRefresh Listings");

	// -----------------------------------------------------------------------------
	// As of 4.1.4
	// -----------------------------------------------------------------------------
	public static final ConfigKey<String> FILTER_TITLE = stringKey("ui.main.filters.title", "&eShow only {{gts_entry_classification}}?");
	public static final ConfigKey<String> FILTER_STATUS_ENABLED = stringKey("ui.main.filters.status.enabled", "&7Status: &aEnabled");
	public static final ConfigKey<String> FILTER_STATUS_DISABLED = stringKey("ui.main.filters.status.disabled", "&7Status: &cDisabled");

	public static final ConfigKey<List<String>> FILTER_NOTES = listKey("ui.main.filters.notes", Lists.newArrayList(
			"",
			"&bControls:",
			"&7Left Click: &aApply action",
			"&7Right Click: &aSwitch filter",
			"",
			"&bNOTE:",
			"&7This option will be overridden by",
			"&7the &eYour Listings &7option",
			"&7if it is enabled."
	));

	public static final ConfigKey<String> UI_TITLES_MAIN = stringKey("ui.main.title", "&cGTS &7\u00bb &3Listings");
	public static final ConfigKey<String> UI_TITLES_ITEMS = stringKey("ui.items.title", "&cGTS &7(&3Items&7)");
	public static final ConfigKey<String> UI_TITLES_CONFIRMATION = stringKey("ui.confirm.title", "&cGTS &7\u00bb &3Confirmation");
	public static final ConfigKey<String> UI_MAIN_NO_ENTRIES_AVAILABLE = stringKey("ui.main.no-entries-available", "&cNo Listing Types Available");

	public static final ConfigKey<String> TRANSLATIONS_YES = stringKey("translations.yes", "Yes");
	public static final ConfigKey<String> TRANSLATIONS_NO = stringKey("translations.no", "No");

	public static final ConfigKey<String> NO_PERMISSION = stringKey("general.errors.no-permission", "{{gts_error}} You don't have permission to use this!");
	public static final ConfigKey<String> PRICE_NOT_POSITIVE = stringKey("general.errors.non-positive-price", "{{gts_error}} Invalid price! Value must be positive!");
	public static final ConfigKey<String> PRICE_MAX_INVALID = stringKey("general.errors.max-price.invalid", "{{gts_error}} Your request is above the max amount of &e{{gts_max_price}}&7!");
	public static final ConfigKey<String> ERROR_BLACKLISTED = stringKey("general.errors.blacklisted", "{{gts_error}} Sorry, but &e{{gts_entry}} &7has been blacklisted from the GTS...");

	public static final ConfigKey<String> BUTTONS_INCREASE_CURRENCY_TITLE = stringKey("buttons.currency.increase.title", "&aIncrease Price Requested");
	public static final ConfigKey<List<String>> BUTTONS_INCREASE_CURRENCY_LORE = listKey("buttons.currency.increase.lore", Lists.newArrayList(
			"&7Left Click: &b+{{gts_button_currency_left_click}}",
			"&7Right Click: &b+{{gts_button_currency_right_click}}",
			"&7Shift + Left Click: &b+{{gts_button_currency_shift_left_click}}",
			"&7Shift + Right Click: &b+{{gts_button_currency_shift_right_click}}"
	));

	public static final ConfigKey<String> BUTTONS_DECREASE_CURRENCY_TITLE = stringKey("buttons.currency.decrease.title", "&cDecrease Price Requested");
	public static final ConfigKey<List<String>> BUTTONS_DECREASE_CURRENCY_LORE = listKey("buttons.currency.decrease.lore", Lists.newArrayList(
			"&7Left Click: &c-{{gts_button_currency_left_click}}",
			"&7Right Click: &c-{{gts_button_currency_right_click}}",
			"&7Shift + Left Click: &c-{{gts_button_currency_shift_left_click}}",
			"&7Shift + Right Click: &c-{{gts_button_currency_shift_right_click}}"
	));

	public static final ConfigKey<String> BUTTONS_INCREASE_TIME_TITLE = stringKey("buttons.time.increase.title", "&aIncrease Time");
	public static final ConfigKey<List<String>> BUTTONS_INCREASE_TIME_LORE = listKey("buttons.time.increase.lore", Lists.newArrayList(
			"&7Left Click: &b+{{gts_button_time_left_click}}",
			"&7Right Click: &b+{{gts_button_time_right_click}}",
			"&7Shift + Left Click: &b+{{gts_button_time_shift_left_click}}",
			"&7Shift + Right Click: &b+{{gts_button_time_shift_right_click}}"
	));

	public static final ConfigKey<String> BUTTONS_DECREASE_TIME_TITLE = stringKey("buttons.time.decrease.title", "&cDecrease Time");
	public static final ConfigKey<List<String>> BUTTONS_DECREASE_TIME_LORE = listKey("buttons.time.decrease.lore", Lists.newArrayList(
			"&7Left Click: &c-{{gts_button_time_left_click}}",
			"&7Right Click: &c-{{gts_button_time_right_click}}",
			"&7Shift + Left Click: &c-{{gts_button_time_shift_left_click}}",
			"&7Shift + Right Click: &c-{{gts_button_time_shift_right_click}}"
	));

	public static final ConfigKey<String> TIME_DISPLAY_TITLE = stringKey("buttons.time.display.title", "&eListing Time");
	public static final ConfigKey<List<String>> TIME_DISPLAY_LORE = listKey("buttons.time.display.lore", Lists.newArrayList(
			"&7Target Time: &a{{gts_time}}",
			"",
			"&7Min Time: &a{{gts_min_time}}",
			"&7Max Time: &a{{gts_max_time}}"
	));

	public static final ConfigKey<String> PRICE_DISPLAY_TITLE = stringKey("buttons.currency.display.title", "&eListing Price");
	public static final ConfigKey<List<String>> PRICE_DISPLAY_LORE = listKey("buttons.currency.display.lore", Lists.newArrayList(
			"&7Target Price: &a{{gts_price}}",
			"",
			"&7Min Price: &a{{gts_min_price}}",
			"&7Max Price: &a{{gts_max_price}}"
	));

	public static final ConfigKey<String> COMMANDS_ERROR_TIMEARG_IMPROPER = stringKey("commands.time.argument.improper", "The specified time is of an incorrect format, or breaches time constraints...");

	public static final ConfigKey<String> ITEMS_NONE_IN_HAND = stringKey("entries.items.command.none-in-hand", "{{gts_error}} Your hand has no item in it!");
	public static final ConfigKey<String> ITEMS_NO_CUSTOM_NAMES = stringKey("entries.items.generic.custom-name-restricted", "{{gts_error}} Your can't sell items with custom names!");
	public static final ConfigKey<String> ITEMS_INVENTORY_FULL = stringKey("entries.items.generic.inventory-full", "{{gts_error}} Your inventory is full, so we'll hold onto this item for you!");

	public static final ConfigKey<List<String>> DISCORD_PUBLISH_TEMPLATE = listKey("discord.templates.publish", Lists.newArrayList(
			"Publisher: {{gts_publisher}}",
			"Publisher Identifier: {{gts_publisher_id}}",
			"",
			"Published Item: {{gts_published_item}}",
			"Item Details: {{gts_published_item_details}}",
			"Requested Price: {{gts_publishing_price}}",
			"Expiration Time: {{gts_publishing_expiration}}"
	));
	public static final ConfigKey<List<String>> DISCORD_PURCHASE_TEMPLATE = listKey("discord.templates.purchase", Lists.newArrayList(
			"Buyer: {{gts_buyer}}",
			"Buyer Identifier: {{gts_buyer_id}}",
			"",
			"Seller: {{gts_seller}}",
			"Seller Identifier: {{gts_seller_id}}",
			"",
			"Item: {{gts_published_item}}",
			"Item Details: {{gts_published_item_details}}",
			"Price: {{gts_publishing_price}}"
	));
	public static final ConfigKey<List<String>> DISCORD_EXPIRATION_TEMPLATE = listKey("discord.templates.expiration", Lists.newArrayList(
			"Publisher: {{gts_publisher}}",
			"Publisher Identifier: {{gts_publisher_id}}",
			"",
			"Item: {{gts_published_item}}",
			"Price: {{gts_publishing_price}}"
	));
	public static final ConfigKey<List<String>> DISCORD_REMOVAL_TEMPLATE = listKey("discord.templates.removal", Lists.newArrayList(
			"Publisher: {{gts_publisher}}",
			"Publisher Identifier: {{gts_publisher_id}}",
			"",
			"Item: {{gts_published_item}}",
			"Item Details: {{gts_published_item_details}}"
	));
	public static final ConfigKey<String> LISTING_EVENT_CANCELLED = stringKey("general.listings.event-cancelled", "{{gts_error}} Your listing was blocked by an administrative source...");

	private static final Map<String, ConfigKey<?>> KEYS;
	private static final int SIZE;

	static {
		Map<String, ConfigKey<?>> keys = new LinkedHashMap<>();
		Field[] values = ConfigKeys.class.getFields();
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
