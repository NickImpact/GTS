package net.impactdev.gts.sponge.ui;

import net.impactdev.impactor.sponge.ui.SpongeUI;
import org.spongepowered.api.entity.living.player.Player;

public interface Displayable {

    SpongeUI getView();

    void open(Player player);

}
