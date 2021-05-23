package net.impactdev.gts.sponge.listings.ui.creator;

import com.google.common.collect.Lists;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.data.registry.GTSKeyMarker;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.api.listings.entries.EntryManager;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.sponge.listings.ui.AbstractSpongeEntryUI;
import net.impactdev.gts.sponge.listings.ui.SpongeMainPageProvider;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.services.text.MessageService;
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

import static net.impactdev.gts.sponge.utils.Utilities.readMessageConfigOption;

public class SpongeEntryTypeSelectionMenu {

    private final Player viewer;

    private final SpongePage<Map.Entry<String, EntryManager<? extends Entry<?, ?>, ?>>> display;

    public SpongeEntryTypeSelectionMenu(Player player) {
        Map<GTSKeyMarker, EntryManager<? extends Entry<?, ?>, ?>> managers = GTSService.getInstance().getGTSComponentManager().getAllEntryManagers();
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);

        SpongePage.SpongePageBuilder builder = SpongePage.builder()
                .viewer(player)
                .title(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_MENU_ENTRY_SELECT_TITLE)))
                .view(this.design(managers.size()))
                .contentZone(InventoryDimension.of(7, managers.size() > 7 ? 2 : 1))
                .offsets(1);

        if(managers.size() > 7) {
            builder.lastPage(ItemTypes.ARROW, 37).nextPage(ItemTypes.ARROW, 43);
        }

        this.viewer = player;
        this.display = builder.build();

        this.display.applier(resource -> {
            return null;
        });

        Optional<Mappings> overrides = Mappings.get(managers.size());
        if(overrides.isPresent()) {
            Mappings mappings = overrides.get();
            Queue<Integer> slots = mappings.createQueue();
            for(Map.Entry<GTSKeyMarker, EntryManager<? extends Entry<?, ?>, ?>> entry : managers.entrySet()) {
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
        builder.dimension(9, size > 7 ? 5 : 4);
        builder.columns(SpongeIcon.BORDER, 0, 8).rows(SpongeIcon.BORDER, 0, size > 7 ? 3 : 2);

        SpongeIcon back = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.BARRIER)
                .add(Keys.DISPLAY_NAME, Utilities.PARSER.parse(readMessageConfigOption(MsgConfigKeys.UI_GENERAL_BACK), Lists.newArrayList(() -> this.viewer)))
                .build()
        );
        back.addListener(clickable -> {
            SpongeMainPageProvider provider = SpongeMainPageProvider.creator().viewer(this.viewer).build();
            provider.open();
        });
        builder.slot(back, size > 7 ? 40 : 31);

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

        private final int[] slots;

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
