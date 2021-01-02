package net.impactdev.gts.ui.admin;

import net.impactdev.gts.api.maintenance.MaintenanceMode;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongeUI;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;

import java.util.function.Supplier;

public class SpongeMaintenanceMenu {

    private Player viewer;
    private SpongeUI display;

    public SpongeMaintenanceMenu(Player viewer) {

    }

    private SpongeLayout design() {
        SpongeLayout.SpongeLayoutBuilder builder = SpongeLayout.builder().dimension(9, 5);
        builder.slots(SpongeIcon.BORDER, 37, 38, 42, 43).columns(SpongeIcon.BORDER, 0, 8);

        return builder.build();
    }

    private enum ItemTypeMapping {

        ALL(MaintenanceMode.ALL, () -> ItemTypes.ANVIL),

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
