package net.impactdev.gts.sponge.listings.ui.creator;

import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.sponge.listings.ui.AbstractSpongeEntryUI;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongePage;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.listings.prices.Price;

import net.impactdev.gts.api.listings.prices.PriceManager;
import net.impactdev.gts.api.listings.ui.EntryUI;
import net.impactdev.gts.common.ui.Historical;
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
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class SpongePriceTypeSelectionMenu implements Historical<AbstractSpongeEntryUI<?>> {

    private final SpongePage<Map.Entry<String, PriceManager<? extends Price<?, ?, ?>, ?>>> display;

    private final AbstractSpongeEntryUI<?> parent;
    private final BiConsumer<EntryUI<?, ?, ?>, Price<?, ?, ?>> callback;

    public SpongePriceTypeSelectionMenu(Player player, AbstractSpongeEntryUI<?> parent, BiConsumer<EntryUI<?, ?, ?>, Price<?, ?, ?>> callback) {
        Map<String, PriceManager<? extends Price<?, ?, ?>, ?>> resources = GTSService.getInstance().getGTSComponentManager().getAllPriceManagers();
        this.parent = parent;
        this.callback = callback;

        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);

        SpongePage.SpongePageBuilder builder = SpongePage.builder()
                .viewer(player)
                .title(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_MENU_PRICE_SELECT_TITLE)))
                .view(this.design(resources.size()))
                .contentZone(InventoryDimension.of(7, resources.size() > 7 ? 2 : 1))
                .offsets(1);

        if(resources.size() > 7) {
            builder.lastPage(ItemTypes.ARROW, 28).nextPage(ItemTypes.ARROW, 34);
        }

        this.display = builder.build();

        this.display.applier(resource -> {
            return null;
        });

        Optional<Mappings> overrides = Mappings.get(resources.size());
        if(overrides.isPresent()) {
            Mappings mappings = overrides.get();
            Queue<Integer> slots = mappings.createQueue();
            for(Map.Entry<String, PriceManager<? extends Price<?, ?, ?>, ?>> entry : resources.entrySet()) {
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

    private SpongeIcon createIcon(String type, PriceManager<? extends Price<?, ?, ?>, ?> resource) {
        ItemType item = Sponge.getRegistry().getType(ItemType.class, resource.getItemID()).orElse(ItemTypes.BARRIER);
        ItemStack rep = ItemStack.builder()
                .itemType(item)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, type))
                .build();
        SpongeIcon icon = new SpongeIcon(rep);
        icon.addListener(clickable -> {
            this.display.close();
            ((PriceManager<? extends Price<?, ?, ?>, Player>) resource).process().accept(clickable.getPlayer(), this.parent, this.callback);
        });
        return icon;
    }

    @Override
    public Optional<Supplier<AbstractSpongeEntryUI<?>>> getParent() {
        return Optional.of(() -> this.parent);
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
