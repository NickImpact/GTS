package net.impactdev.gts.common.config

import com.google.common.collect.ImmutableMap
import com.google.common.collect.Lists
import net.impactdev.gts.common.config.types.time.TimeLanguageOptions
import net.impactdev.gts.common.config.wrappers.SortConfigurationOptions
import net.impactdev.gts.common.config.wrappers.TitleLorePair
import net.impactdev.impactor.api.configuration.ConfigKey
import net.impactdev.impactor.api.configuration.ConfigKeyHolder
import net.impactdev.impactor.api.configuration.ConfigKeyTypes
import net.impactdev.impactor.api.configuration.ConfigurationAdapter
import net.impactdev.impactor.api.configuration.keys.BaseConfigKey
import java.lang.reflect.Modifier
import java.util.*

class MsgConfigKeys : ConfigKeyHolder {
    companion object {
        // Plugin chat prefix (replacement option for {{gts_prefix}}
		@JvmField
		val PREFIX: ConfigKey<String> = ConfigKeyTypes.stringKey("general.gts-prefix", "&eGTS &7\u00bb")
        @JvmField
		val ERROR_PREFIX: ConfigKey<String> =
            ConfigKeyTypes.stringKey("general.gts-prefix-error", "&eGTS &7(&cERROR&7)")

        // Generic messages for the program
        // Best to support lists of text here, as a server may decide to go heavy on text formatting
		@JvmField
		val MAX_LISTINGS: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "general.max-listings", Lists.newArrayList(
                "{{gts:prefix}} &cUnfortunately, you can't deposit another listing, since you already have {{gts:max_listings}} deposited..."
            )
        )
        @JvmField
		val ADD_TEMPLATE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "general.addition-to-seller", Lists.newArrayList(
                "{{gts:prefix}} &7Your &a{{gts:listing_name}} &7has been added to the market!"
            )
        )
        @JvmField
		val ADD_BROADCAST_BIN: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "general.addition-broadcast.buy-it-now", Lists.newArrayList(
                "{{gts:prefix}} {{gts:seller}} &7has added a &a{{gts:listing_details}} &7to the GTS for &a{{gts:bin_price}}&7!"
            )
        )
        @JvmField
		val ADD_BROADCAST_AUCTION: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "general.addition-broadcast.auctions", Lists.newArrayList(
                "{{gts:prefix}} {{gts:seller}} &7has added a &a{{gts:listing_details}} &7to the GTS for auction, starting at &e{{gts:auction_start_price}}&7!"
            )
        )
        @JvmField
		val PURCHASE_PAY: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "general.prices.pay", Lists.newArrayList(
                "{{gts:prefix}} &7You have purchased a &a{{gts:listing_details}} &7for &e{{gts:bin_price}}&7!"
            )
        )
        @JvmField
		val PURCHASE_PAY_FAIL_TO_GIVE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "general.prices.pay-fail-to-give", Lists.newArrayList(
                "{{gts:error}} &7The listing could not be rewarded at this time, please check your stash!"
            )
        )
        @JvmField
		val PURCHASE_RECEIVE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "general.prices.receive", Lists.newArrayList(
                "{{gts:prefix}} &a{{gts:purchaser}} &7purchased your &a{{gts:listing_name}} &7listing for &a{{gts:bin_price}}&7!"
            )
        )
        @JvmField
		val MIN_PRICE_ERROR: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "general.prices.min-price.invalid", Lists.newArrayList(
                "{{gts:error}} &7In order to sell your &a{{gts:listing_name}}&7, you need to list it for the price of &e{{gts:min_price}}&7..."
            )
        )
        @JvmField
		val MAX_PRICE_ERROR: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "general.prices.max-price.invalid", Lists.newArrayList(
                "{{gts:error}} &7In order to sell your &a{{gts:listing_name}}&7, you need to list it for the price at or below &e{{gts:max_price}}&7..."
            )
        )

        // Error messages
        val PRICE_NOT_POSITIVE: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "general.errors.non-positive-price",
            "{{gts:error}} Invalid price! Value must be positive!"
        )
        val PRICE_MAX_INVALID: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "general.errors.max-price.invalid",
            "{{gts:error}} Your request is above the max amount of &e{{gts_max_price}}&7!"
        )
        @JvmField
		val DISCORD_PUBLISH_TEMPLATE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "discord.templates.publish.buyitnow", Lists.newArrayList(
                "Listing ID: {{discord:listing_id}}",
                "",
                "Publisher: {{discord:publisher}}",
                "Identifier: {{discord:publisher_id}}",
                "",
                "Requested Price: {{discord:price}}",
                "Expiration Time: {{discord:expiration}}"
            )
        )
        @JvmField
		val DISCORD_PUBLISH_AUCTION_TEMPLATE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "discord.templates.publish.auction", Lists.newArrayList(
                "Listing ID: {{discord:listing_id}}",
                "",
                "Publisher: {{discord:publisher}}",
                "Identifier: {{discord:publisher_id}}",
                "",
                "Starting Bid: {{discord:starting_bid}}",
                "Expiration Time: {{discord:expiration}}"
            )
        )
        @JvmField
		val DISCORD_PURCHASE_TEMPLATE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "discord.templates.purchase", Lists.newArrayList(
                "Listing ID: {{discord:listing_id}}",
                "",
                "Buyer: {{discord:actor}}",
                "Buyer Identifier: {{discord:actor_id}}",
                "",
                "Seller: {{discord:publisher}}",
                "Seller Identifier: {{discord:publisher_id}}",
                "",
                "Price: {{discord:price}}"
            )
        )
        @JvmField
		val DISCORD_BID_TEMPLATE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "discord.templates.bid", Lists.newArrayList(
                "Listing ID: {{discord:listing_id}}",
                "",
                "Bidder: {{discord:actor}}",
                "Bidder Identifier: {{discord:actor_id}}",
                "Bid Amount: {{discord:bid}}",
                "",
                "Seller: {{discord:publisher}}",
                "Seller Identifier: {{discord:publisher_id}}"
            )
        )
        @JvmField
		val DISCORD_REMOVAL_TEMPLATE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "discord.templates.removal", Lists.newArrayList(
                "Listing ID: {{discord:listing_id}}",
                "",
                "Publisher: {{discord:publisher}}",
                "Identifier: {{discord:publisher_id}}"
            )
        )
        @JvmField
		val LISTING_EVENT_CANCELLED: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "general.listings.event-cancelled",
            "{{gts:error}} Your listing was blocked by an administrative source..."
        )
        @JvmField
		val UNABLE_TO_TAKE_LISTING: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "general.listings.unable-to-take",
            "{{gts:error}} Your listing failed to be taken..."
        )
        @JvmField
		val CONFIRM_PURCHASE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("buttons.general.confirm-purchase", "&aConfirm Purchase")
        @JvmField
		val AWAITING_CREATE_LISTING_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("buttons.general.awaiting.create-listing.title", "&cCreate Listing")
        @JvmField
		val AWAITING_CREATE_LISTING_LORE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "buttons.general.awaiting.create-listing.lore", Lists.newArrayList(
                "&7Select an element you",
                "&7wish to sell/auction away",
                "&7to create a listing!"
            )
        )
        @JvmField
		val CONFIRM_CREATE_LISTING_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("buttons.general.confirm.create-listing.title", "&aCreate Listing")
        @JvmField
		val CONFIRM_CREATE_LISTING_LORE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "buttons.general.confirm.create-listing.lore", Lists.newArrayList(
                "",
                "&eClick here to create your listing!"
            )
        )
        val AWAITING_SELECT_PRICE_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("buttons.general.awaiting.create-listing.title", "&cConfirm Price")
        val AWAITING_SELECT_PRICE_LORE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "buttons.general.awaiting.create-listing.lore", Lists.newArrayList(
                "&7Please fill out price specifications",
                "&7first to confirm your price!"
            )
        )
        val CONFIRM_SELECT_PRICE_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("buttons.general.confirm.select-price.title", "&aConfirm Price")
        val CONFIRM_SELECT_PRICE_LORE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "buttons.general.confirm.select-price.lore", Lists.newArrayList(
                "",
                "&eClick here to confirm your price!"
            )
        )
        val AWAITING_SELECTION_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("buttons.general.awaiting.selection.title", "&cConfirm Selection")
        val AWAITING_SELECTION_LORE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "buttons.general.awaiting.selection.lore", Lists.newArrayList(
                "&7Please fill out price specifications",
                "&7first to confirm your selection!"
            )
        )
        val CONFIRM_SELECTION_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("buttons.general.confirm.selection.title", "&aConfirm Selection")
        val CONFIRM_SELECTION_LORE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "buttons.general.confirm.selection.lore", Lists.newArrayList(
                "",
                "&eClick here to confirm your selection!"
            )
        )

        // -----------------------------------------------------------------------------
        // Time
        // -----------------------------------------------------------------------------
		@JvmField
		val SECONDS: ConfigKey<TimeLanguageOptions> = ConfigKeyTypes.customKey { c: ConfigurationAdapter ->
            TimeLanguageOptions(
                c.getString("time.seconds.singular", "Second"),
                c.getString("time.seconds.plural", "Seconds")
            )
        }
        @JvmField
		val MINUTES: ConfigKey<TimeLanguageOptions> = ConfigKeyTypes.customKey { c: ConfigurationAdapter ->
            TimeLanguageOptions(
                c.getString("time.minutes.singular", "Minute"),
                c.getString("time.minutes.plural", "Minutes")
            )
        }
        @JvmField
		val HOURS: ConfigKey<TimeLanguageOptions> = ConfigKeyTypes.customKey { c: ConfigurationAdapter ->
            TimeLanguageOptions(
                c.getString("time.hour.singular", "Hour"),
                c.getString("time.hour.plural", "Hours")
            )
        }
        @JvmField
		val DAYS: ConfigKey<TimeLanguageOptions> = ConfigKeyTypes.customKey { c: ConfigurationAdapter ->
            TimeLanguageOptions(
                c.getString("time.days.singular", "Day"),
                c.getString("time.days.plural", "Days")
            )
        }
        @JvmField
		val WEEKS: ConfigKey<TimeLanguageOptions> = ConfigKeyTypes.customKey { c: ConfigurationAdapter ->
            TimeLanguageOptions(
                c.getString("time.weeks.singular", "Week"),
                c.getString("time.weeks.plural", "Weeks")
            )
        }
        @JvmField
		val CUSTOM_TIME_TITLE: ConfigKey<String> = ConfigKeyTypes.stringKey("time.custom.title", "&aCustom Duration")
        @JvmField
		val CUSTOM_TIME_LORE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "time.custom.lore", Lists.newArrayList(
                "&7Specify how long you want",
                "&7the listing to last.",
                "",
                "&eClick to choose your time!"
            )
        )
        @JvmField
		val STATUS_PURCHASED: ConfigKey<String> = ConfigKeyTypes.stringKey("status.purchased", "&7Status: &aPurchased")
        @JvmField
		val STATUS_TIME_EXPIRED: ConfigKey<String> =
            ConfigKeyTypes.stringKey("status.time.expired", "&7Status: &cConcluded")
        @JvmField
		val TIME_REMAINING_TRANSLATION: ConfigKey<String> =
            ConfigKeyTypes.stringKey("status.time.remaining", "&7Ends in: &a{{gts:time_short}}")
        @JvmField
		val TIME_MOMENTS_TRANSLATION: ConfigKey<String> = ConfigKeyTypes.stringKey("time.moments", "Moments")

        // -----------------------------------------------------------------------------
        // UI Based Configuration Options
        // -----------------------------------------------------------------------------
        // General Items
		@JvmField
		val UI_GENERAL_BACK: ConfigKey<String> = ConfigKeyTypes.stringKey("ui.general.back", "&cGo Back")

        // Main Menu
		@JvmField
		val UI_MAIN_TITLE: ConfigKey<String> = ConfigKeyTypes.stringKey("ui.menus.main.title", "&cGTS")
        @JvmField
		val UI_MAIN_BROWSER: ConfigKey<TitleLorePair> = ConfigKeyTypes.customKey { c: ConfigurationAdapter ->
            val title = c.getString("ui.menus.main.browser.title", "&aBrowser")
            val lore = c.getStringList(
                "ui.menus.main.browser.lore", Lists.newArrayList(
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
                )
            )
            TitleLorePair(title, lore)
        }
        @JvmField
		val UI_MAIN_STASH: ConfigKey<TitleLorePair> = ConfigKeyTypes.customKey { c: ConfigurationAdapter ->
            val title = c.getString("ui.menus.main.stash.title", "&aStash")
            val lore = c.getStringList(
                "ui.menus.main.stash.lore", Lists.newArrayList(
                    "&7Items that you have &eacquired",
                    "&7or &eexpired &7can be found here",
                    "&7in order to be claimed!"
                )
            )
            TitleLorePair(title, lore)
        }
        @JvmField
		val UI_MAIN_STASH_CLICK_NOTIF: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.menus.main.stash.click-to-open", "&eClick to open your stash!")
        @JvmField
		val UI_MAIN_SELL: ConfigKey<TitleLorePair> = ConfigKeyTypes.customKey { c: ConfigurationAdapter ->
            val title = c.getString("ui.menus.main.sell.title", "&aSell a Good")
            val lore = c.getStringList(
                "ui.menus.main.sell.lore", Lists.newArrayList(
                    "&7Here, you'll be able to directly",
                    "&7sell items on the GTS market.",
                    "&7Items you list here will be",
                    "&7posted for quick purchase by",
                    "&7another player, and will expire",
                    "&7overtime if nobody ever purchases",
                    "&7your listing.",
                    "",
                    "&eClick to become rich!"
                )
            )
            TitleLorePair(title, lore)
        }
        @JvmField
		val UI_MAIN_VIEW_PERSONAL_LISTINGS: ConfigKey<TitleLorePair> =
            ConfigKeyTypes.customKey { c: ConfigurationAdapter ->
                val title = c.getString("ui.menus.main.view-personal-listings.title", "&aView Your Listings")
                val lore = c.getStringList(
                    "ui.menus.main.view-personal-listings.lore", Lists.newArrayList(
                        "&7View the listings you've",
                        "&7created that are still active",
                        "&7on the market. Expired listings",
                        "&7can be found in your stash!",
                        "",
                        "&eClick to view your listings!"
                    )
                )
                TitleLorePair(title, lore)
            }
        @JvmField
		val UI_MAIN_CURRENT_BIDS_SINGLE: ConfigKey<TitleLorePair> =
            ConfigKeyTypes.customKey { c: ConfigurationAdapter ->
                val title = c.getString("ui.menus.main.bids.title", "&aView Bids")
                val lore = c.getStringList(
                    "ui.menus.main.bids.lore.single", Lists.newArrayList(
                        "&7Items that you have an active",
                        "&7bid against can be found here",
                        "&7for your convenience",
                        "",
                        "&bYou have {{gts:active_bids|fallback=&7Loading...}} active bid",
                        "",
                        "&eClick to inspect!"
                    )
                )
                TitleLorePair(title, lore)
            }
        @JvmField
		val UI_MAIN_CURRENT_BIDS_MULTI: ConfigKey<TitleLorePair> = ConfigKeyTypes.customKey { c: ConfigurationAdapter ->
            val title = c.getString("ui.menus.main.bids.title", "&aView Bids")
            val lore = c.getStringList(
                "ui.menus.main.bids.lore.multi", Lists.newArrayList(
                    "&7Items that you have an active",
                    "&7bid against can be found here",
                    "&7for your convenience",
                    "",
                    "&bYou have {{gts:active_bids|fallback=&7Loading...}} active bids",
                    "",
                    "&eClick to inspect!"
                )
            )
            TitleLorePair(title, lore)
        }
        @JvmField
		val UI_MAIN_PLAYER_SETTINGS: ConfigKey<TitleLorePair> = ConfigKeyTypes.customKey { c: ConfigurationAdapter ->
            val title = c.getString("ui.menus.main.player-settings.title", "&aCustomize your Settings")
            val lore = c.getStringList(
                "ui.menus.main.player-settings.lore", Lists.newArrayList(
                    "&7Control output made by GTS",
                    "&7specifically for yourself!",
                    "",
                    "&7Here, you can set flags that",
                    "&7control a specific output",
                    "&7type!",
                    "",
                    "&eClick to begin editing!"
                )
            )
            TitleLorePair(title, lore)
        }

        // Listings Menu
		@JvmField
		val UI_MENU_LISTINGS_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.menus.listings.title", "&cGTS &7\u00bb &3Listings")
        @JvmField
		val UI_MENU_SEARCH_TITLE: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "ui.menus.listings.search.title",
            "&aSearch"
        )
        @JvmField
		val UI_MENU_SEARCH_LORE_NO_QUERY: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.menus.listings.search.lore.no-query",
            Lists.newArrayList(
                "&7Find items by name, type,",
                "&7or any other options that",
                "&7can identify an item.",
                "",
                "&eClick to begin search!"
            )
        )
        @JvmField
		val UI_MENU_SEARCH_LORE_QUERIED: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
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
        )
        @JvmField
		val UI_MENU_LISTINGS_SORT: ConfigKey<SortConfigurationOptions> =
            ConfigKeyTypes.customKey { c: ConfigurationAdapter ->
                SortConfigurationOptions(
                    c.getString("ui.menus.listings.sort.title", "&aSort"),
                    c.getString("ui.menus.listings.sort.lore.coloring.selected", "&b"),
                    c.getString("ui.menus.listings.sort.lore.coloring.not-selected", "&7"),
                    c.getString("ui.menus.listings.sort.lore.quick-purchase.most-recent", "Most Recent"),
                    c.getString("ui.menus.listings.sort.lore.quick-purchase.ending-soon", "Ending Soon"),
                    c.getString("ui.menus.listings.sort.lore.auctions.highest-bid", "Highest Bid"),
                    c.getString("ui.menus.listings.sort.lore.auctions.lowest-bid", "Lowest Bid"),
                    c.getString("ui.menus.listings.sort.lore.auctions.ending-soon", "Ending Soon"),
                    c.getString("ui.menus.listings.sort.lore.auctions.most-bids", "Most Bids")
                )
            }
        @JvmField
		val UI_MENU_LISTINGS_SPECIAL_LOADING: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.menus.listings.special.loading", "&eFetching Listings...")
        @JvmField
		val UI_MENU_LISTINGS_SPECIAL_TIMED_OUT: ConfigKey<TitleLorePair> =
            ConfigKeyTypes.customKey { c: ConfigurationAdapter ->
                val title = c.getString("ui.menus.listings.special.timed-out.title", "&cFetch Timed Out")
                val lore = c.getStringList(
                    "ui.menus.listings.special.timed-out.lore", Lists.newArrayList(
                        "&7GTS failed to lookup the stored",
                        "&7listings in a timely manner...",
                        "",
                        "&7Please retry opening the menu",
                        "&7in a few moments!"
                    )
                )
                TitleLorePair(title, lore)
            }

        // Stash Window
		@JvmField
		val UI_MENU_STASH_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.menus.stash.title", "&cGTS &7\u00bb &3Stash")
        @JvmField
		val UI_MENU_MAIN_STASH_STATUS: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.menus.main.stash.status", "&b* You have items available for pickup!")
        @JvmField
		val UI_ICON_STASH_COLLECT_ALL_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.icons.stash.collect-all.title", "&aCollect All")
        @JvmField
		val UI_ICON_STASH_COLLECT_ALL_LORE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.icons.stash.collect-all.lore", Lists.newArrayList(
                "&7Allows you to claim all your stashed",
                "&7listings at once! Note that if you",
                "&7don't have the space for a particular",
                "&7listing, it'll be skipped",
                "",
                "&eClick to begin your claim request!"
            )
        )
        @JvmField
		val STASH_COLLECT_ALL_RESULTS: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "ui.menus.stash.collect-all.results",
            "{{gts:prefix}} &7Successfully returned {{gts:stash_returned}} listings!"
        )
        @JvmField
		val UI_MENU_ENTRY_SELECT_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.menus.entry-select.title", "&cGTS &7\u00bb &3Select Entry Type")
        @JvmField
		val UI_MENU_PRICE_SELECT_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.menus.price-select.title", "&cGTS &7\u00bb &3Select Price Type")
        @JvmField
		val UI_MENU_LISTING_SELECTED_OTHER: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.menus.listing-selected.purchaser", "&cGTS &7\u00bb &3Purchase Listing?")
        @JvmField
		val UI_MENU_LISTING_SELECTED_OTHER_AUCTION: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.menus.listing-selected.bidder", "&cGTS &7\u00bb &3Bid on Listing?")
        @JvmField
		val UI_MENU_LISTING_SELECTED_LISTER: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.menus.listing-selected.lister", "&cGTS &7\u00bb &3Remove Listing?")
        @JvmField
		val UI_MENU_LISTING_SELECTED_CLAIM: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.menus.listing-selected.claim", "&cGTS &7\u00bb &3Claim Rewards?")

        // Icons
		@JvmField
		val UI_ICON_BIN_CREATE_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.icons.bin.creator.title", "&aBIN Mode")
        @JvmField
		val UI_ICON_BIN_CREATE_LORE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.icons.bin.creator.lore", Lists.newArrayList(
                "&7Set a price, then one player",
                "&7may buy the listing at that",
                "&7price.",
                "",
                "&8(BIN means Buy It Now)",
                "",
                "&eClick to switch to Auction Mode!"
            )
        )
        @JvmField
		val UI_ICON_AUCTION_CREATE_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.icons.auction.creator.title", "&aAuction Mode")
        @JvmField
		val UI_ICON_AUCTION_CREATE_LORE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.icons.auctions.creator.lore", Lists.newArrayList(
                "&7A listing in which multiple",
                "&7players compete for the listing",
                "&7by bidding against each other",
                "",
                "&eClick to switch to BIN Mode!"
            )
        )
        @JvmField
		val UI_ICON_SELECTED_REMOVE_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.icons.selected.remove.title", "&cRemove Listing?")
        @JvmField
		val UI_ICON_SELECTED_REMOVE_LORE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.icons.selected.remove.lore", Lists.newArrayList(
                "&7Requests a removal of your",
                "&7listing from the &bGTS&7.",
                "",
                "&7NOTE: If your listing has already",
                "&7been claimed, this request may",
                "&7fail...",
                "",
                "&eClick here to request removal!"
            )
        )
        @JvmField
		val UI_ICON_SELECTED_CLAIM_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.icons.selected.claim.title", "&eClaim your {{gts:claim_item}}?")
        @JvmField
		val UI_ICON_SELECTED_CLAIM_LORE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.icons.selected.remove.lore", Lists.newArrayList(
                "&7Requests a removal of your",
                "&7listing from the &bGTS&7.",
                "",
                "&7NOTE: If your listing has already",
                "&7been claimed, this request may",
                "&7fail...",
                "",
                "&eClick here to request removal!"
            )
        )
        @JvmField
		val UI_ICON_PLACE_BID_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.icons.auctions.place-bid.title", "&ePlace Bid")
        @JvmField
		val UI_ICON_PLACE_BID_LORE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.icons.auctions.place-bid.lore", Lists.newArrayList(
                "&7New Bid: &6{{gts:auction_next_required_bid}}"
            )
        )
        @JvmField
		val UI_ICON_PLACE_BID_WITH_USER_BID_PLACED_LORE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.icons.auctions.place-bid.user-previously-bid.lore", Lists.newArrayList(
                "&7New Bid: &6{{gts:auction_next_required_bid}}",
                "&7Your previous bid: &e{{gts:auction_previous_user_bid}}"
            )
        )
        @JvmField
		val UI_ICON_PLACE_BID_CAN_AFFORD: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.icons.auctions.place-bid.appenders.can-afford", Lists.newArrayList(
                "",
                "&eClick to bid!"
            )
        )
        @JvmField
		val UI_ICON_PLACE_BID_CANT_AFFORD: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.icons.auctions.place-bid.appenders.cant-afford", Lists.newArrayList(
                "",
                "&cCan't afford bid!"
            )
        )
        @JvmField
		val UI_ICON_PLACE_BID_IS_TOP_BID: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.icons.auctions.place-bid.appenders.user-is-top-bidder", Lists.newArrayList(
                "",
                "&cYou already hold the top bid!"
            )
        )
        @JvmField
		val UI_ICON_PLACE_CUSTOM_BID_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.icons.auctions.place-bid.custom.title", "&eCustom Bid")
        @JvmField
		val UI_ICON_PLACE_CUSTOM_BID_LORE_BASE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.icons.auctions.place-bid.custom.lore.base", Lists.newArrayList(
                "&7With this option, you can",
                "&7specify a custom bid of your",
                "&7desires, so long as it's",
                "&7at least able to meet the",
                "&7current requirement for the",
                "&7next bid!",
                "",
                "&eClick to cast a custom bid!"
            )
        )
        @JvmField
		val CUSTOM_BID_INVALID: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "ui.icons.auctions.place-bid.custom.actions.bid-invalid",
            "{{gts:error}} You must bid at least &e{{gts:auction_next_required_bid}}&7!"
        )
        @JvmField
		val UI_ICON_BID_HISTORY_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.icons.auctions.bid-history.title", "&eBid History")
        @JvmField
		val UI_ICON_BID_HISTORY_BASE_INFO: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.icons.auctions.bid-history.base-info", Lists.newArrayList(
                "&7Bids Placed: &e{{gts:auction_bids}}"
            )
        )
        @JvmField
		val UI_ICON_BID_HISTORY_SEPARATOR: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.icons.auctions.bid-history.separator", "&8&m-------------------")
        @JvmField
		val UI_ICON_BID_HISTORY_BID_INFO: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.icons.auctions.bid-history.bid-info", Lists.newArrayList(
                "&7Bid: &e{{gts:auction_bid_amount}}",
                "&7By: &e{{gts:auction_bid_actor}}",
                "&b{{gts:auction_bid_since_placed}} ago"
            )
        )
        @JvmField
		val UI_ICON_BID_HISTORY_NO_BIDS: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.icons.auctions.bid-history.no-bids", Lists.newArrayList(
                "",
                "&7Be the first to place a",
                "&7bid on this auction!"
            )
        )

        // Price Selection
		@JvmField
		val UI_PRICE_DISPLAY_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.components.price.display.title", "&ePrice: {{gts:price_selection}}")
        @JvmField
		val UI_PRICE_DISPLAY_LORE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.components.price.display.lore.base", Lists.newArrayList(
                "&7How much to list your",
                "&7be listed on the GTS."
            )
        )
        @JvmField
		val UI_PRICE_DISPLAY_FEES: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.components.price.display.lore.fees", Lists.newArrayList(
                "",
                "&7Fee: &6{{gts:price_fee}} &e({{gts:price_fee_rate}})"
            )
        )

        // Time Selection
		@JvmField
		val UI_TIME_SELECT_TITLE: ConfigKey<String> = ConfigKeyTypes.stringKey("ui.time-select.title", "Select a Time")
        @JvmField
		val UI_TIME_DISPLAY_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.components.time.display.title", "&eDuration: {{gts:time}}")
        @JvmField
		val UI_TIME_DISPLAY_LORE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.components.time.display.lore.base", Lists.newArrayList(
                "&7How long the listing will",
                "&7be listed on the GTS."
            )
        )
        @JvmField
		val UI_TIME_DISPLAY_FEES: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.components.time.display.lore.fees", Lists.newArrayList(
                "",
                "&7Fee: &6{{gts:time_fee}}"
            )
        )
        @JvmField
		val UI_COMPONENT_EDIT_LORE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.components.edit-lore", Lists.newArrayList(
                "",
                "&eClick to edit!"
            )
        )

        // Fees
		@JvmField
		val FEE_APPLICATION: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "general.fees.applied", Lists.newArrayList(
                "{{gts:prefix}} &c&l- {{gts:fees}} &7(&aFees&7)"
            )
        )
        @JvmField
		val FEE_INVALID: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "general.fees.invalid", Lists.newArrayList(
                "{{gts:prefix}} &cUnable to afford the tax of &e{{gts:fees}} &cfor this listing..."
            )
        )
        @JvmField
		val FEE_PRICE_FORMAT: ConfigKey<String> =
            ConfigKeyTypes.stringKey("general.fees.price-format", "&7Price Selection: {{gts:price_fee}}")
        @JvmField
		val FEE_TIME_FORMAT: ConfigKey<String> =
            ConfigKeyTypes.stringKey("general.fees.time-format", "&7Time Selection: {{gts:time_fee}}")

        // Admin Menus
		@JvmField
		val UI_ADMIN_MAIN_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.admin.main.title", "&cGTS &7\u00bb &3Admin Mode")
        @JvmField
		val UI_ADMIN_MAIN_MANAGER: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.admin.main.icons.manager", "&aGTS Listing Manager")
        @JvmField
		val UI_ADMIN_MAIN_PRICE_MGMT: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.admin.main.icons.price-management", "&aPricing Management")
        @JvmField
		val UI_ADMIN_MAIN_DISABLER: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.admin.main.icons.disabler", "&cMaintenance Mode")
        @JvmField
		val UI_ADMIN_MAIN_INFO_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.admin.main.icons.info.title", "&eGTS Admin Mode")
        @JvmField
		val UI_ADMIN_MAIN_INFO_LORE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.admin.main.icons.info.lore", Lists.newArrayList(
                "&7Welcome to the GTS Admin Interface.",
                "&7All interactions provided are designed",
                "&7for server operators to effectively",
                "&7control the GTS system from in-game.",
                "",
                "&7Here, you can control &bpublished listings&7,",
                "&bprice management&7, and place the system",
                "&7into &bmaintenance mode&7."
            )
        )
        @JvmField
		val UI_LISTING_DETAIL_SEPARATOR: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.listings.detail-separator", Lists.newArrayList(
                "&8&m-------------------------"
            )
        )
        @JvmField
		val UI_BIN_DETAILS: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.listings.buy-it-now.details", Lists.newArrayList(
                "&7Seller: &e{{gts:seller}}",
                "&7Buy it now: &a{{gts:bin_price}}",
                "",
                "{{gts:listing_status}}",
                "",
                "&eClick to inspect!"
            )
        )
        @JvmField
		val UI_AUCTION_DETAILS_NO_BIDS: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.listings.auctions.details.no-bids", Lists.newArrayList(
                "&7Seller: &e{{gts:seller}}",
                "&7Starting Bid: &e{{gts:auction_start_price}}",
                "",
                "{{gts:listing_status}}",
                "",
                "&eClick to inspect!"
            )
        )
        @JvmField
		val UI_AUCTION_DETAILS_WITH_SINGLE_BID: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.listings.auctions.details.with-single-bid", Lists.newArrayList(
                "&7Seller: &e{{gts:seller}}",
                "&7Bids: &a{{gts:auction_bids}} bid",
                "",
                "&7Top bid: &e{{gts:auction_high_bid}}",
                "&7Bidder: {{gts:auction_high_bidder}}",
                "",
                "{{gts:listing_status}}",
                "",
                "&eClick to inspect!"
            )
        )
        @JvmField
		val UI_AUCTION_DETAILS_WITH_BIDS: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.listings.auctions.details.with-multiple-bids", Lists.newArrayList(
                "&7Seller: &e{{gts:seller}}",
                "&7Bids: &a{{gts:auction_bids}} bids",
                "",
                "&7Top bid: &e{{gts:auction_high_bid}}",
                "&7Bidder: {{gts:auction_high_bidder}}",
                "",
                "{{gts:listing_status}}",
                "",
                "&eClick to inspect!"
            )
        )

        // Player Settings Menu
		@JvmField
		val UI_PLAYER_SETTINGS_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.player-settings.title", "&cGTS &7\u00bb &3User Settings")
        @JvmField
		val UI_PLAYER_SETTINGS_SETTING_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.player-settings.setting-display.title", "&e{{setting}} Notifications")
        @JvmField
		val UI_PLAYER_SETTINGS_PUBLISH_SETTING_LORE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.player-settings.setting-display.publish", Lists.newArrayList(
                "&7This settings controls whether",
                "&7you'll be informed of new listings",
                "&7that are published to the GTS!"
            )
        )
        @JvmField
		val UI_PLAYER_SETTINGS_SOLD_SETTING_LORE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.player-settings.setting-display.sold", Lists.newArrayList(
                "&7This settings controls whether",
                "&7you'll be informed when your BIN",
                "&7listings have been purchased!"
            )
        )
        @JvmField
		val UI_PLAYER_SETTINGS_BID_SETTING_LORE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.player-settings.setting-display.bid", Lists.newArrayList(
                "&7This settings controls whether",
                "&7you'll be informed when your auctions",
                "&7have received a new bid!"
            )
        )
        @JvmField
		val UI_PLAYER_SETTINGS_OUTBID_SETTING_LORE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.player-settings.setting-display.outbid", Lists.newArrayList(
                "&7This settings controls whether",
                "&7you'll be informed when you've been",
                "&7outbid on an auction you've previously",
                "&7bid on!"
            )
        )
        @JvmField
		val UI_PLAYER_SETTINGS_SETTING_ENABLED: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.player-settings.setting.enabled", "&aEnabled")
        @JvmField
		val UI_PLAYER_SETTINGS_SETTING_DISABLED: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.player-settings.setting.disabled", "&cDisabled")
        @JvmField
		val UI_PLAYER_SETTINGS_SETTING_LOADING: ConfigKey<String> =
            ConfigKeyTypes.stringKey("ui.player-settings.setting.loading", "&6Loading...")
        @JvmField
		val UI_PLAYER_SETTINGS_SETTING_TOGGLE_LORE: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "ui.player-settings.setting.toggle-lore", Lists.newArrayList(
                "&7Click me to toggle the state",
                "&7of this setting!"
            )
        )

        // Generic Messages
		@JvmField
		val GENERAL_FEEDBACK_BEGIN_PROCESSING_REQUEST: ConfigKey<String> =
            ConfigKeyTypes.stringKey("general.feedback.begin-feedback-request", "&7Processing your request...")
        @JvmField
		val GENERAL_FEEDBACK_FEES_COLLECTION: ConfigKey<String> =
            ConfigKeyTypes.stringKey("general.feedback.fees-collect", "&7Collecting fees...")
        @JvmField
		val GENERAL_FEEDBACK_COLLECT_LISTING: ConfigKey<String> =
            ConfigKeyTypes.stringKey("general.feedback.collect-listing", "&7Collecting your listing...")
        @JvmField
		val GENERAL_FEEDBACK_RETURN_FEES: ConfigKey<String> =
            ConfigKeyTypes.stringKey("general.feedback.return-fees-from-failure", "&7Returning fees...")
        @JvmField
		val GENERAL_FEEDBACK_LISTING_RETURNED: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "general.feedback.listing-returned",
            "{{gts:prefix}} Your listing has been returned!"
        )
        @JvmField
		val GENERAL_FEEDBACK_ITEM_CLAIMED: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "general.feedback.item-claimed",
            "{{gts:prefix}} You claimed your &a{{gts:claim_item}}&7!"
        )
        @JvmField
		val GENERAL_FEEDBACK_LISTING_FAIL_TO_RETURN: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "general.feedback.listing-fail-to-return",
            "{{gts:error}} We failed to return your listing... We've kept it in your stash for now!"
        )
        @JvmField
		val GENERAL_FEEDBACK_AUCTIONS_ALREADY_TOP_BIDDER: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "general.feedback.auctions.already-top-bidder",
            "{{gts:error}} You already hold the top bid on this auction!"
        )
        @JvmField
		val GENERAL_FEEDBACK_AUCTIONS_CANT_AFFORD_BID: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "general.feedback.auctions.cant-afford-bid",
            "{{gts:error}} You're unable to afford that bid..."
        )
        val GENERAL_FEEDBACK_BLACKLISTED: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "general.feedback.blacklisted",
            "{{gts:error}} Your selection is &cblacklisted &7from being listed on the GTS..."
        )
        @JvmField
		val GENERAL_FEEDBACK_AUCTIONS_OUTBID: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "general.feedback.auctions.outbid",
            "{{gts:prefix}} &a{{gts:auction_bidder}} &7outbid you by &e{{gts:auction_outbid_amount}} &7for &a{{gts:listing_name}}&7!"
        )
        @JvmField
		val GENERAL_FEEDBACK_AUCTIONS_NEWBID: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "general.feedback.auctions.new-bid",
            "{{gts:prefix}} &a{{gts:auction_bidder}} &7bid &e{{gts:auction_bid_amount}} &7for your &a{{gts:listing_name}}&7!"
        )
        @JvmField
		val GENERAL_FEEDBACK_AUCTIONS_CANCELLED: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "general.feedback.auctions.cancelled",
            "{{gts:prefix}} Heads up! The auction for {{gts:listing_name}} has been cancelled, so you've been refunded your money!"
        )
        @JvmField
		val GENERAL_FEEDBACK_PROCESSING_BID: ConfigKey<String> =
            ConfigKeyTypes.stringKey("general.feedback.processing-bid", "&7Processing bid...")
        @JvmField
		val GENERAL_FEEDBACK_FUNDS_TO_ESCROW: ConfigKey<String> =
            ConfigKeyTypes.stringKey("general.feedback.funds-to-escrow", "&7Putting funds in escrow...")
        @JvmField
		val GENERAL_FEEDBACK_FUNDS_FROM_ESCROW: ConfigKey<String> =
            ConfigKeyTypes.stringKey("general.feedback.funds-from-escrow", "&7Returning your funds from escrow...")
        @JvmField
		val GENERAL_FEEDBACK_AUCTIONS_BID_PLACED: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "general.feedback.auctions.bid-placed",
            "{{gts:prefix}} Your bid of {{gts:auction_bid_amount}} has been placed!"
        )
        @JvmField
		val REQUEST_FAILED: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "general.requests.failure",
            "{{gts:prefix}} &7Request failed with status code (&c{{gts:error_code}}&7)"
        )
        @JvmField
		val UPDATE_AVAILABLE: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "general.update-check.available",
            "{{gts:prefix}} &7A new update is available (&a{{new_version}}&7), and you are running &e{{current_version}}&7! Check Ore or Discord for the update!"
        )
        @JvmField
		val UPDATE_LATEST: ConfigKey<String> =
            ConfigKeyTypes.stringKey("general.update-check.latest", "{{gts:prefix}} You're using the latest version!")
        @JvmField
		val UPDATE_SNAPSHOT: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "general.update-check.snapshot",
            "{{gts:prefix}} You're using a snapshot version of GTS, things may not work correctly!"
        )
        @JvmField
		val ADMIN_LISTING_EDITOR_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("admin.listing-editor.title", "&cGTS &7\u00bb &3Listing Editor")
        @JvmField
		val ADMIN_LISTING_EDITOR_DELETE_TITLE: ConfigKey<String> =
            ConfigKeyTypes.stringKey("admin.listing-editor.icons.delete.title", "&aDelete Listing")
        @JvmField
		val ADMIN_LISTING_EDITOR_DELETE_LORE: ConfigKey<List<String>> =
            ConfigKeyTypes.listKey("admin.listing-editor.icons.delete.lore", Lists.newArrayList())
        @JvmField
		val ADMIN_LISTING_EDITOR_DELETE_RETURN_TITLE: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "admin.listing-editor.icons.delete-and-return.title",
            "&aDelete and Return Listing"
        )
        @JvmField
		val ADMIN_LISTING_EDITOR_DELETE_RETURN_LORE: ConfigKey<List<String>> =
            ConfigKeyTypes.listKey("admin.listing-editor.icons.delete-and-return.lore", Lists.newArrayList())
        @JvmField
		val ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_SUCCESS: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "admin.listing-editor.responses.success",
            "{{gts:prefix}} The target listing has been deleted!"
        )
        @JvmField
		val ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_FAILURE: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "admin.listing-editor.responses.error",
            "{{gts:error}} The target listing failed to be deleted, with error code &7(&c{{gts:error_code}}&7)"
        )
        @JvmField
		val ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_USER: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "admin.listing-editor.responses.user-delete",
            "{{gts:prefix}} One of your listings has been forcibly deleted by an admin!"
        )
        @JvmField
		val ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_USER_RETURN: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "admin.listing-editor.responses.user-return",
            "{{gts:prefix}} One of your listings has been forcibly deleted by an admin, but the item was returned to you!"
        )
        @JvmField
		val ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_USER_RETURN_STASH: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "admin.listing-editor.responses.user-stash",
            "{{gts:prefix}} One of your listings has been forcibly deleted by an admin, but the item was returned to your stash!"
        )
        @JvmField
		val ITEM_DISCORD_DETAILS: ConfigKey<List<String>> = ConfigKeyTypes.listKey(
            "discord.items.details", Lists.newArrayList(
                "Lore:",
                "{{gts:item_lore}}",
                "",
                "Enchantments:",
                "{{gts:item_enchantments}}"
            )
        )
        @JvmField
		val SAFE_MODE_FEEDBACK: ConfigKey<String> = ConfigKeyTypes.stringKey(
            "general.feedback.safe-mode",
            "{{gts:error}} &cThe plugin is currently in safe mode! All functionality is disabled! Reason: &7(&c{{gts:error_code}}&7)"
        )
        private val KEYS: Map<String, ConfigKey<*>>? = null
        private const val SIZE = 0

        init {
            val keys: Map<String, ConfigKey<*>> = LinkedHashMap()
            val values = MsgConfigKeys::class.java.fields
            val i = 0
            for (f in net.impactdev.gts.common.config.values) {
                // ignore non-static fields
                if (!Modifier.isStatic(net.impactdev.gts.common.config.f.getModifiers())) {
                    continue
                }

                // ignore fields that aren't configkeys
                if (ConfigKey::class.java != net.impactdev.gts.common.config.f.getType()) {
                    continue
                }
                try {
                    // get the key instance
                    val key = net.impactdev.gts.common.config.f.get(null) as BaseConfigKey<*>
                    // set the ordinal value of the key.
                    net.impactdev.gts.common.config.key.ordinal = net.impactdev.gts.common.config.i++
                    // add the key to the return map
                    net.impactdev.gts.common.config.keys.put(
                        net.impactdev.gts.common.config.f.getName(),
                        net.impactdev.gts.common.config.key
                    )
                } catch (e: Exception) {
                    throw RuntimeException("Exception processing field: " + net.impactdev.gts.common.config.f, e)
                }
            }
            KEYS = ImmutableMap.copyOf<String, ConfigKey<*>>(net.impactdev.gts.common.config.keys)
            SIZE = net.impactdev.gts.common.config.i
        }
    }

    override fun getKeys(): Map<String, ConfigKey<*>>? {
        return KEYS
    }

    override fun getSize(): Int {
        return SIZE
    }
}