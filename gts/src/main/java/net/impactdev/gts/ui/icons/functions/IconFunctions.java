package net.impactdev.gts.ui.icons.functions;

import net.impactdev.gts.ui.views.SimpleViews;
import net.impactdev.gts.ui.views.listings.ListingsView;
import net.impactdev.gts.util.GTSKeys;
import net.impactdev.impactor.api.platform.sources.PlatformPlayer;
import net.impactdev.impactor.api.ui.metadata.UIMetadataKeys;
import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

public final class IconFunctions {

    public static final IconFunction EXIT_UI = create("exit_ui", context -> {
        PlatformPlayer player = context.require(PlatformPlayer.class);
        player.metadata(UIMetadataKeys.OPENED_VIEW).ifPresent(view -> view.close(player));
    });

    public static final IconFunction OPEN_BIN_LISTINGS = create("open_bin_listings", context -> {
        PlatformPlayer player = context.require(PlatformPlayer.class);
        SimpleViews.BIN_LISTINGS.provide(player).open(player);
    });

    public static final IconFunction OPEN_AUCTIONS = create("open_auction_listings", context -> {
        PlatformPlayer player = context.require(PlatformPlayer.class);

    });

    public static final IconFunction OPEN_SECURE_TRADE = create("open_secure_trade", context -> {

    });

    public static final IconFunction OPEN_BAZAAR = create("open_bazaar", context -> {

    });

    public static final IconFunction OPEN_STASH = create("open_stash", context -> {

    });

    public static void dummy() {}

    private static IconFunction create(
            @NotNull @Pattern("[a-z0-9_\\-./]+") @Subst("dummy") String identifier,
            @NotNull IconFunction function
    ) {
        return IconFunctionRegistry.register(GTSKeys.gts(identifier), function);
    }
}
