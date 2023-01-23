package net.impactdev.gts.ui.views.listings;

import net.impactdev.gts.ui.Viewable;
import net.impactdev.gts.ui.templates.registry.TemplateRegistry;
import net.impactdev.impactor.api.platform.sources.PlatformPlayer;
import net.kyori.adventure.key.Key;


public abstract class ListingsView implements Viewable {

    protected abstract Key key();

    @Override
    public void open(PlatformPlayer target) {
        TemplateRegistry.instance().provide(this.key(), target).open(target);
    }

}
