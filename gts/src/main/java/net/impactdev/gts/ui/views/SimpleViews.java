package net.impactdev.gts.ui.views;

import net.impactdev.gts.ui.templates.ViewProvider;
import net.impactdev.gts.ui.templates.registry.TemplateRegistry;
import net.impactdev.gts.util.GTSKeys;
import net.impactdev.impactor.api.ui.containers.views.ChestView;
import net.impactdev.impactor.api.ui.containers.views.pagination.sectioned.SectionedPagination;

public final class SimpleViews {

    public static final ViewProvider<ChestView> MAIN_MENU = viewer -> TemplateRegistry.instance()
            .provide(GTSKeys.gts("main-menu"), viewer);

    public static final ViewProvider<SectionedPagination> BIN_LISTINGS = viewer -> TemplateRegistry.instance()
            .provide(GTSKeys.gts("bin-listings"), viewer);
}
