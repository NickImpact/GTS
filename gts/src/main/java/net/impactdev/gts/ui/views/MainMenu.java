package net.impactdev.gts.ui.views;

import net.impactdev.gts.locale.Messages;
import net.impactdev.gts.ui.Icons;
import net.impactdev.gts.ui.Viewable;
import net.impactdev.gts.util.GTSKeys;
import net.impactdev.impactor.api.items.ImpactorItemStack;
import net.impactdev.impactor.api.items.extensions.SkullStack;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.ui.containers.Icon;
import net.impactdev.impactor.api.ui.containers.layouts.ChestLayout;
import net.impactdev.impactor.api.ui.containers.views.ChestView;
import net.impactdev.impactor.api.utilities.context.Context;

public final class MainMenu implements Viewable {

    private final ChestView view;
    private final PlatformPlayer viewer;

    public MainMenu(PlatformPlayer viewer) {
        this.viewer = viewer;
        this.view = ChestView.builder()
                .provider(GTSKeys.gts("main-menu"))
                .title(Messages.UI_MAIN_TITLE.build(this.viewer.locale(), Context.empty()))
                .readonly(true)
                .layout(this.create())
                .build();
    }

    private ChestLayout create() {
        ChestLayout.ChestLayoutBuilder builder = ChestLayout.builder().size(5);
        builder.rows(Icons.BLACK_BORDER, 1, 5);
        builder.columns(Icons.BLACK_BORDER, 1, 7, 9);

        SkullStack browser = ImpactorItemStack.skull()
                .player("MmUyY2M0MjAxNWU2Njc4ZjhmZDQ5Y2NjMDFmYmY3ODdmMWJhMmMzMmJjZjU1OWEwMTUzMzJmYzVkYjUwIn19fQ==", true)
                .title(Messages.UI_MAIN_BROWSER_TITLE.build(this.viewer.locale(), Context.empty()))
                .lore(Messages.UI_MAIN_BROWSER_LORE.build(this.viewer.locale(), Context.empty()))
                .build();

        Icon icon = Icon.builder()
                .display(() -> browser)
                .constant()
                .listener(context -> false)
                .build();

        builder.slot(icon, 21);

        return builder.build();
    }

    @Override
    public void open(PlatformPlayer target) {
        this.view.open(target);
    }
}
