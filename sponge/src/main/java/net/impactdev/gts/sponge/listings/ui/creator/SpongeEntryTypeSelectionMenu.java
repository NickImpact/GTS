package net.impactdev.gts.sponge.listings.ui.creator;

import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.data.registry.GTSKeyMarker;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.api.listings.entries.EntryManager;
import net.impactdev.gts.api.ui.GTSMenu;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.sponge.listings.ui.AbstractSpongeEntryUI;
import net.impactdev.gts.sponge.listings.ui.SpongeMainPageProvider;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.gts.sponge.utils.items.ProvidedIcons;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.ui.containers.ImpactorUI;
import net.impactdev.impactor.api.ui.containers.icons.DisplayProvider;
import net.impactdev.impactor.api.ui.containers.icons.Icon;
import net.impactdev.impactor.api.ui.containers.layouts.Layout;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

import static net.impactdev.gts.sponge.utils.Utilities.readMessageConfigOption;

public class SpongeEntryTypeSelectionMenu implements GTSMenu {

    private final ServerPlayer viewer;
    private final ImpactorUI display;

    public SpongeEntryTypeSelectionMenu(ServerPlayer player) {
        Map<GTSKeyMarker, EntryManager<? extends Entry<?, ?>, ?>> managers = GTSService.getInstance().getGTSComponentManager().getAllEntryManagers();
        final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);

        Mappings mappings = Mappings.get(managers.size()).orElseThrow(() -> new IllegalStateException("Awaiting further functionality"));
        Layout.LayoutBuilder builder = Layout.builder()
                .from(this.design(managers.size()));
        Queue<Integer> slots = mappings.createQueue();
        for(Map.Entry<GTSKeyMarker, EntryManager<? extends Entry<?, ?>, ?>> entry : managers.entrySet()) {
            Icon<ItemStack> icon = this.createIcon(entry.getValue().getName(), entry.getValue());
            builder.slot(icon, slots.poll());
        }

        this.display = ImpactorUI.builder()
                .provider(Key.key("gts", "entry-selection"))
                .title(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_MENU_ENTRY_SELECT_TITLE)))
                .layout(builder.build())
                .build();

        this.viewer = player;
    }

    public void open() {
        this.display.open(PlatformPlayer.from(this.viewer));
    }

    private Layout design(int size) {
        Layout.LayoutBuilder builder = Layout.builder();
        builder.size(size > 7 ? 5 : 4);
        builder.columns(ProvidedIcons.BORDER, 1, 9).rows(ProvidedIcons.BORDER, 1, size > 7 ? 4 : 3);

        Icon<ItemStack> back = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                        .itemType(ItemTypes.BARRIER)
                        .add(Keys.CUSTOM_NAME, Utilities.PARSER.parse(readMessageConfigOption(MsgConfigKeys.UI_GENERAL_BACK)))
                        .build()
                ))
                .listener(context -> {
                    SpongeMainPageProvider provider = SpongeMainPageProvider.creator().viewer(this.viewer).build();
                    provider.open();
                    return false;
                })
                .build();
        builder.slot(back, size > 7 ? 40 : 31);

        return builder.build();
    }

    private Icon<ItemStack> createIcon(String type, EntryManager<? extends Entry<?, ?>, ?> resource) {
        ItemType item = Sponge.game().registry(RegistryTypes.ITEM_TYPE)
                .findEntry(ResourceKey.resolve(resource.getItemID()))
                .map(RegistryEntry::value)
                .orElse(ItemTypes.BARRIER.get());

        ItemStack rep = ItemStack.builder()
                .itemType(item)
                .add(Keys.CUSTOM_NAME, Component.text(type).color(NamedTextColor.GREEN))
                .build();

        return Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(rep))
                .listener(context -> {
                    this.display.close(PlatformPlayer.from(this.viewer));
                    ((AbstractSpongeEntryUI<?>)(((EntryManager<? extends Entry<?, ?>, Player>) resource).getSellingUI(this.viewer)).get()).open(this.viewer);
                    return false;
                })
                .build();
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
