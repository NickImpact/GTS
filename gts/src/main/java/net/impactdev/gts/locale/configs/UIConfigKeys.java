package net.impactdev.gts.locale.configs;

import com.google.common.collect.Lists;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.configuration.loader.KeyProvider;

import java.util.List;

import static net.impactdev.gts.locale.configs.PathCreator.create;
import static net.impactdev.impactor.api.configuration.ConfigKeyTypes.listKey;
import static net.impactdev.impactor.api.configuration.ConfigKeyTypes.stringKey;

@KeyProvider
public class UIConfigKeys {

    // ----------------------------------------------------------------------------------------------------
    // Main Menu
    // ----------------------------------------------------------------------------------------------------
    public static final ConfigKey<String> MAIN_MENU_TITLE = stringKey(
            create("gts.menus.main.title"),
            "<gradient:red:yellow>GTS</gradient>"
    );
    public static final ConfigKey<String> BROWSER_TITLE = stringKey(
            create("gts.menus.main.icons.browser.title"),
            "<gradient:green:blue>Browser</gradient>"
    );
    public static final ConfigKey<List<String>> BROWSER_LORE = listKey(
            create("gts.menus.main.icons.browser.lore"),
            Lists.newArrayList()
    );

    // ----------------------------------------------------------------------------------------------------
}
