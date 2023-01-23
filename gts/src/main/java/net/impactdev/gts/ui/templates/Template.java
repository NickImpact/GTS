package net.impactdev.gts.ui.templates;

import net.impactdev.impactor.api.platform.sources.PlatformPlayer;
import net.impactdev.impactor.api.ui.containers.View;

public interface Template<T extends View> {

    T generate(PlatformPlayer viewer);

}
