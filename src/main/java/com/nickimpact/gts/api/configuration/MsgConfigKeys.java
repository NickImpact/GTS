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

	// Item titles (Only one line)
	public static final ConfigKey<String> UI_ITEMS_NEXT_PAGE = StringKey.of("ui.items.next-page", "&a\u2192 Next Page \u2192");
	public static final ConfigKey<String> UI_ITEMS_LAST_PAGE = StringKey.of("ui.items.last-page", "&c\u2190 Last Page \u2190");
	public static final ConfigKey<String> UI_ITEMS_REFRESH = StringKey.of("ui.items.refresh", "&eRefresh Listings");
	public static final ConfigKey<String> UI_ITEMS_PLAYER_TITLE = StringKey.of("", "");

	// Item Lore (multiline)
	public static final ConfigKey<List<String>> UI_ITEMS_PLAYER_LORE = ListKey.of("", Lists.newArrayList());
}
