package net.impactdev.gts.sponge.listings.ui;

import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.builders.Builder;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public interface SpongeMainPageProvider {

    ServerPlayer getViewer();

    void open();

    static Creator creator() {
        return Impactor.getInstance().getRegistry().createBuilder(Creator.class);
    }

    interface Creator extends Builder<SpongeMainPageProvider> {

        Creator viewer(ServerPlayer player);

    }

}
