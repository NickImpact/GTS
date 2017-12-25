package com.nickimpact.gts.api.configuration;

import com.google.common.collect.Lists;
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
			"{{gts_prefix}}&cUnfortunately, you can't deposit another listing, since you already have {{max_listings}} deposited..."
	));

	// Listing output
	public static final ConfigKey<List<String>> ADD_TEMPLATE = ListKey.of("addition-to-seller", Lists.newArrayList(
			"{{gts_prefix}}&7Your {{listing_name}} has been added to the market!"
	));
	public static final ConfigKey<String> TAX_APPLICATION = StringKey.of("tax.applied", "&c&l- {{tax}} &7(&aTaxes&7)");
	public static final ConfigKey<String> TAX_INVALID = StringKey.of("tax.invalid", "{{gts_prefix}}&cUnable to afford the tax of &e{{tax}} &7for this listing...");

	public static final ConfigKey<List<String>> ADD_BROADCAST = ListKey.of("addition-broadcast", Lists.newArrayList(

	));

	public static final ConfigKey<String> UI_ITEMS_NEXT_PAGE = StringKey.of("next-page", "&a\u2192 Next Page \u2192");
	public static final ConfigKey<String> UI_ITEMS_LAST_PAGE = StringKey.of("last-page", "&c\u2190 Last Page \u2190");
	public static final ConfigKey<String> UI_ITEMS_REFRESH = StringKey.of("refresh", "&eRefresh Listings");
	public static final ConfigKey<String> UI_ITEMS_PLAYER_TITLE = StringKey.of("head.title", "&ePlayer Info");
	public static final ConfigKey<List<String>> UI_ITEMS_PLAYER_LORE = ListKey.of("head.lore", Lists.newArrayList());
	public static final ConfigKey<String> UI_ITEMS_SORT_TITLE = StringKey.of("sort.title", "&eSort Listings");
	public static final ConfigKey<List<String>> UI_ITEMS_SORT_LORE = ListKey.of("sort.lore", Lists.newArrayList());

}
