package net.impactdev.gts.locale;

import net.impactdev.gts.locale.configs.GeneralKeys;
import net.impactdev.gts.locale.configs.UIConfigKeys;
import net.impactdev.gts.locale.translations.MultiLineTranslation;
import net.impactdev.gts.locale.translations.Translation;
import net.kyori.adventure.text.Component;

import java.util.List;

import static net.impactdev.gts.locale.translations.Translation.multiline;
import static net.impactdev.gts.locale.translations.Translation.singular;

public interface Messages {

    // ------------------------------------------------------------------------------------------
    // Metadata
    // ------------------------------------------------------------------------------------------
    Translation<Component> PREFIX = singular(GeneralKeys.PREFIX);
    Translation<Component> ERROR = singular(GeneralKeys.ERROR);

    // ------------------------------------------------------------------------------------------
    // General
    // ------------------------------------------------------------------------------------------
    Translation<Component> PUBLISH_BIN_LISTING = singular(GeneralKeys.PUBLISH_LISTING_BIN);
    Translation<Component> SAFE_MODE = singular(GeneralKeys.SAFE_MODE);

    // ------------------------------------------------------------------------------------------
    // UI
    // ------------------------------------------------------------------------------------------
    Translation<Component> UI_MAIN_TITLE = singular(UIConfigKeys.MAIN_MENU_TITLE);
    Translation<Component> UI_MAIN_BROWSER_TITLE = singular(UIConfigKeys.BROWSER_TITLE);
    Translation<List<Component>> UI_MAIN_BROWSER_LORE = multiline(UIConfigKeys.BROWSER_LORE);
}
