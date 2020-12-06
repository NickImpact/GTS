package net.impactdev.gts.sponge.listings.ui;

import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.utilities.Builder;
import org.spongepowered.api.entity.living.player.Player;

public interface SpongeMainPageProvider {

    Player getViewer();

    void open();

    static Creator creator() {
        return Impactor.getInstance().getRegistry().createBuilder(Creator.class);
    }

    interface Creator extends Builder<SpongeMainPageProvider, Creator> {

        Creator viewer(Player player);

    }

}
