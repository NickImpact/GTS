package net.impactdev.gts.sponge.listings.ui.creator;

import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.api.listings.entries.EntryManager;
import net.impactdev.gts.sponge.listings.ui.AbstractSpongeEntryUI;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongePage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

public class SpongeEntryTypeSelectionMenu {

    private final SpongePage<Map.Entry<String, EntryManager<? extends Entry<?, ?>, ?>>> display;

    public SpongeEntryTypeSelectionMenu(Player player) {
        Map<String, EntryManager<? extends Entry<?, ?>, ?>> managers = GTSService.getInstance().getGTSComponentManager().getAllEntryManagers();

        SpongePage.SpongePageBuilder builder = SpongePage.builder()
                .viewer(player)
                .title(Text.of(TextColors.RED, "GTS", TextColors.GOLD, "TODO"))
                .view(this.design(managers.size()))
                .contentZone(InventoryDimension.of(7, managers.size() > 7 ? 2 : 1))
                .offsets(1);

        if(managers.size() > 7) {
            builder.lastPage(ItemTypes.ARROW, 28).nextPage(ItemTypes.ARROW, 34);
        }

        this.display = builder.build();

        this.display.applier(resource -> {
            return null;
        });

        Optional<Mappings> overrides = Mappings.get(managers.size());
        if(overrides.isPresent()) {
            Mappings mappings = overrides.get();
            Queue<Integer> slots = mappings.createQueue();
            for(Map.Entry<String, EntryManager<? extends Entry<?, ?>, ?>> entry : managers.entrySet()) {
                SpongeIcon icon = this.createIcon(entry.getValue().getName(), entry.getValue());
                this.display.getView().setSlot(slots.poll(), icon);
            }
        }
    }

    public void open() {
        this.display.open();
    }

    private SpongeLayout design(int size) {
        SpongeLayout.SpongeLayoutBuilder builder = SpongeLayout.builder();
        builder.dimension(9, size > 7 ? 4 : 3);
        builder.columns(SpongeIcon.BORDER, 0, 8).rows(SpongeIcon.BORDER, 0, 2);

        return builder.build();
    }

    private SpongeIcon createIcon(String type, EntryManager<? extends Entry<?, ?>, ?> resource) {
        ItemType item = Sponge.getRegistry().getType(ItemType.class, resource.getItemID()).orElse(ItemTypes.BARRIER);
        ItemStack rep = ItemStack.builder()
                .itemType(item)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, type))
                .build();
        SpongeIcon icon = new SpongeIcon(rep);
        icon.addListener(clickable -> {
            this.display.close();
            ((AbstractSpongeEntryUI<?>)(((EntryManager<? extends Entry<?, ?>, Player>) resource).getSellingUI(this.display.getViewer())).get()).open(this.display.getViewer());
        });
        return icon;
    }

    private enum Mappings {
        ONE(13),
        TWO(11, 15),
        THREE(11, 13, 15),
        FOUR(10, 12, 14, 16),
        FIVE(11, 12, 13, 14, 15),;

        private int[] slots;

        Mappings(int... slots) {
            this.slots = slots;
        }

        public static Optional<Mappings> get(int size) {
            if(size > 5) {
                return Optional.empty();
            }

            return Optional.of(values()[Math.max(0, size - 1)]);
        }

        public Queue<Integer> createQueue() {
            Queue<Integer> queue = new LinkedList<>();
            for(int slot : this.slots) {
                queue.add(slot);
            }

            return queue;
        }
    }

}
