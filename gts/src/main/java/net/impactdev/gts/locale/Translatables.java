package net.impactdev.gts.locale;

import net.impactdev.gts.locale.translations.TranslationProvider;
import net.kyori.adventure.text.Component;

import static net.impactdev.gts.locale.translations.TranslationProvider.provider;

public interface Translatables {

    // ------------------------------------------------------------------------------------------
    // Metadata
    // ------------------------------------------------------------------------------------------
    TranslationProvider<Component> PREFIX = provider("metadata.prefixes.nominal");
    TranslationProvider<Component> ERROR = provider("metadata.prefixes.error");

    // ------------------------------------------------------------------------------------------
    // General
    // ------------------------------------------------------------------------------------------
    TranslationProvider<Component> BEGIN_LISTING_PUBLISH = provider("feedback.publish.begin");
    TranslationProvider<Component> FAILED_PUBLISH_MAX_LISTINGS = provider("feedback.publish.failure.max-listings");
    TranslationProvider<Component> FAILED_PUBLISH_LISTENER_FAILURE = provider("feedback.publish.failure.listeners");
    TranslationProvider<Component> PUBLISHED_LISTING_COLLECTION = provider("feedback.publish.collection");

    TranslationProvider<Component> PUBLISH_BIN_LISTING = provider("broadcasts.publish-listing.buy-it-now");
    TranslationProvider<Component> PUBLISH_AUCTION_LISTING = provider("broadcasts.publish-listing.auction");
//    Translation<Component> SAFE_MODE = singular(GeneralKeys.SAFE_MODE);

    // ------------------------------------------------------------------------------------------
    // Translation Downloading
    // ------------------------------------------------------------------------------------------
    TranslationProvider<Component> TRANSLATIONS_DOWNLOAD_FAILED = provider("translations.download.failed");

    // ------------------------------------------------------------------------------------------
    // UI
    // ------------------------------------------------------------------------------------------
//    Translation<Component> UI_MAIN_TITLE = singular(UIConfigKeys.MAIN_MENU_TITLE);
//    Translation<Component> UI_MAIN_BROWSER_TITLE = singular(UIConfigKeys.BROWSER_TITLE);
//    Translation<List<Component>> UI_MAIN_BROWSER_LORE = multiline(UIConfigKeys.BROWSER_LORE);
}
