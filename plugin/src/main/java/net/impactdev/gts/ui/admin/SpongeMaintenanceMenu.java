package net.impactdev.gts.ui.admin;

import net.impactdev.gts.api.maintenance.MaintenanceMode;
import net.impactdev.gts.sponge.utils.items.ProvidedIcons;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.ui.containers.ImpactorUI;
import net.impactdev.impactor.api.ui.containers.layouts.Layout;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;

import java.util.function.Supplier;

public class SpongeMaintenanceMenu {

    private PlatformPlayer viewer;
    private ImpactorUI display;

    public SpongeMaintenanceMenu(ServerPlayer viewer) {

    }

    private Layout design() {
        Layout.LayoutBuilder builder = Layout.builder().size(5);
        builder.slots(ProvidedIcons.BORDER, 37, 38, 42, 43).columns(ProvidedIcons.BORDER, 0, 8);

        return builder.build();
    }

    private enum ItemTypeMapping {

        ALL(MaintenanceMode.ALL, ItemTypes.ANVIL),

        ;

        private MaintenanceMode mode;
        private Supplier<ItemType> type;

        ItemTypeMapping(MaintenanceMode mode, Supplier<ItemType> type) {
            this.mode = mode;
            this.type = type;
        }

        public MaintenanceMode getMode() {
            return this.mode;
        }

        public ItemType getType() {
            return this.type.get();
        }
    }
}
