package net.impactdev.gts.ui.templates;

import net.impactdev.impactor.api.platform.sources.PlatformPlayer;

@FunctionalInterface
public interface ViewProvider<T> {

    T provide(PlatformPlayer viewer);

}
