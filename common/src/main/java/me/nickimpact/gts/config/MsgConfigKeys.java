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

	// Entries
	public static final ConfigKey<List<String>> ENTRY_INFO = listKey("entries.base-info", Lists.newArrayList(
			"",
			"&7Price: &e{{price}}",
			"&7Time Left: &e{{time_left}}"
	));

	// Error messages
	public static final ConfigKey<List<String>> NOT_ENOUGH_FUNDS = listKey("general.purchase.not-enough-funds", Lists.newArrayList("&cUnfortunately, you were unable to afford the price of {{price}}"));
	public static final ConfigKey<List<String>> ALREADY_CLAIMED = listKey("general.purchase.already-claimed", Lists.newArrayList("&cUnfortunately, this listing has already been claimed..."));
	public static final ConfigKey<List<String>> EXPIRED = listKey("general.purchase.expired", Lists.newArrayList("&cUnfortunately, this listing has since expired..."));
	public static final ConfigKey<List<String>> ITEM_ENTRY_BASE_LORE = listKey("entries.item.base.lore", Lists.newArrayList(
			"&7Seller: &e{{seller}}"
	));
	public static final ConfigKey<String> ITEM_ENTRY_BASE_TITLE = stringKey("entries.item.base.title", "{{item_title}}");
	public static final ConfigKey<String> ITEM_ENTRY_SPEC_TEMPLATE = stringKey("entries.item.spec-template", "{{item_title}}");

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
	public static final ConfigKey<String> UI_ITEMS_SELLOPTION_NOT_SELECTED = stringKey("ui.items.icons.not-selected", "&cSelect an Item...");
	public static final ConfigKey<String> UI_TITLES_CONFIRMATION = stringKey("ui.confirm.title", "&cGTS &7\u00bb &3Confirmation");
	public static final ConfigKey<String> UI_MAIN_NO_ENTRIES_AVAILABLE = stringKey("ui.main.no-entries-available", "&cNo Listing Types Available");
	public static final ConfigKey<String> UI_SELL_ITEMS_TITLE = stringKey("ui.sell-options.title", "&cSelect What to Sell");

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

	public static final ConfigKey<String> PRICE_DISPLAY_TITLE = stringKey("buttons.currency.display.title", "&eListing Price");
	public static final ConfigKey<List<String>> PRICE_DISPLAY_LORE = listKey("buttons.currency.display.lore", Lists.newArrayList(
			"&7Target Price: &a{{gts_price}}",
			"",
			"&7Min Price: &a{{gts_min_price}}",
			"&7Max Price: &a{{gts_max_price}}"
	));

	public static final ConfigKey<String> BUTTONS_INCREASE_AMOUNT_TITLE = stringKey("buttons.amount.increase.title", "&aIncrease Amount To Be Listed");
	public static final ConfigKey<List<String>> BUTTONS_INCREASE_AMOUNT_LORE = listKey("buttons.amount.increase.lore", Lists.newArrayList(
			"&7Left Click: &b+{{gts_button_amount_left_click}}",
			"&7Right Click: &b+{{gts_button_amount_right_click}}",
			"&7Shift + Left Click: &b+{{gts_button_amount_shift_left_click}}",
			"&7Shift + Right Click: &b+{{gts_button_amount_shift_right_click}}"
	));

	public static final ConfigKey<String> BUTTONS_DECREASE_AMOUNT_TITLE = stringKey("buttons.amount.decrease.title", "&cDecrease Amount To Be Listed");
	public static final ConfigKey<List<String>> BUTTONS_DECREASE_AMOUNT_LORE = listKey("buttons.amount.decrease.lore", Lists.newArrayList(
			"&7Left Click: &c-{{gts_button_amount_left_click}}",
			"&7Right Click: &c-{{gts_button_amount_right_click}}",
			"&7Shift + Left Click: &c-{{gts_button_amount_shift_left_click}}",
			"&7Shift + Right Click: &c-{{gts_button_amount_shift_right_click}}"
	));

	public static final ConfigKey<String> AMOUNT_DISPLAY_TITLE = stringKey("buttons.amount.display.title", "&eListing Amount");
	public static final ConfigKey<List<String>> AMOUNT_DISPLAY_LORE = listKey("buttons.amount.display.lore", Lists.newArrayList(
			"&7Target amount: &a{{gts_amount}}",
			"",
			"&7Min Size: &a{{gts_min_amount}}",
			"&7Max Size: &a{{gts_max_amount}}"
	));

	public static final ConfigKey<String> ITEMS_NONE_IN_HAND = stringKey("entries.items.command.none-in-hand", "{{gts_error}} Your hand has no item in it!");
	public static final ConfigKey<String> ITEMS_NO_CUSTOM_NAMES = stringKey("entries.items.generic.custom-name-restricted", "{{gts_error}} You can't sell items with custom names!");
	public static final ConfigKey<String> ITEMS_INVENTORY_FULL = stringKey("entries.items.generic.inventory-full", "{{gts_error}} Your inventory is full, so we'll hold onto this item for you!");
	public static final ConfigKey<String> ITEMS_MAX_STACK = stringKey("ui.items.icons.max-stack-size", "Max Stack Size");

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

	public static final ConfigKey<String> PLUGIN_ERROR = stringKey("general.errors.unexpected", "{{gts_error}} Due to an internal server error, your action request was cancelled...");
	public static final ConfigKey<String> UNABLE_TO_TAKE_LISTING = stringKey("general.listings.unable-to-take", "{{gts_error}} Your listing failed to be taken, so we returned the tax you paid!");
	public static final ConfigKey<String> UI_ITEMS_PLAYER_LISTINGS_LORE_DISABLED = stringKey("ui.main.items.player-listings.disabled", "&7Status: &cDisabled");
	public static final ConfigKey<String> UI_ITEMS_PLAYER_LISTINGS_LORE_ENABLED = stringKey("ui.main.items.player-listings.enabled", "&7Status: &aEnabled");
	public static final ConfigKey<String> UI_ITEMS_REFRESH_TITLE = stringKey("ui.main.items.refresh.title", "&eRefresh Listings");
	public static final ConfigKey<String> BLACKLISTED = stringKey("general.errors.blacklisted", "{{gts_error}} Unfortunately, that object has been blacklisted from being placed on the GTS...");
	public static final ConfigKey<String> NOT_PLAYER = stringKey("general.errors.commands.not-player", "{{gts_error}} You must be a player to use that command...");
	public static final ConfigKey<String> INVALID_ARGS = stringKey("general.errors.commands.not-enough-args", "{{gts_error}} Invalid syntax...");
	public static final ConfigKey<String> CONFIRM_SELECTION = stringKey("buttons.general.confirm-selection", "&aConfirm Selection");
	public static final ConfigKey<String> CONFIRM_PURCHASE = stringKey("buttons.general.confirm-purchase", "&aConfirm Purchase");
	public static final ConfigKey<String> CANCEL = stringKey("buttons.general.cancel.title", "&cCancel");
	public static final ConfigKey<String> CLICK_TO_CONFIRM = stringKey("buttons.confirming.click-to-confirm", "&cClick to Confirm");
	public static final ConfigKey<String> CONFIRMED = stringKey("buttons.confirming.confirmed", "&aConfirmed!");
	public static final ConfigKey<String> REQUIRES_CONFIRMATION = stringKey("buttons.confirming.requires", "&cRequires Confirmation");
	public static final ConfigKey<String> REMOVE_BUTTON = stringKey("buttons.remove", "&cClick to Remove your Listing");
	public static final ConfigKey<List<String>> REMOVED_MISSING = listKey("general.errors.remove-listing.not-available", Lists.newArrayList("{{gts_error}} Unfortunately, your listing has either been purchased, or already expired..."));
	public static final ConfigKey<String> IGNORE_ON = stringKey("general.ignoring-broadcasts.on", "{{gts_prefix}} Now ignoring broadcasts");
	public static final ConfigKey<String> IGNORE_OFF = stringKey("general.ignoring-broadcasts.off", "{{gts_prefix}} No longer ignoring broadcasts");
	public static final ConfigKey<String> SOLD_LISTING_INFORM = stringKey("general.listings.inform-of-sell", "{{gts_prefix}} Your &e{{listing_name}} &7has been sold for &a{{gts_price}}&7!");
	public static final ConfigKey<String> SEARCH_NO_OPTION = stringKey("general.errors.searching.no-option", "{{gts_error}} No search option exists for that typing...");
	public static final ConfigKey<String> COOLDOWN_COOLING = stringKey("general.errors.cooldowns.cooling", "{{gts_error}} &7You must wait an additional &a{{gts_cooldown_wait_time}} &7before listing another listing!");


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
