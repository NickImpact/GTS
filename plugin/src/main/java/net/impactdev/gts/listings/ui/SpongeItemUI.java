package net.impactdev.gts.listings.ui;

import com.google.common.collect.Lists;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.listings.data.ChosenItemEntry;
import net.impactdev.gts.sponge.utils.items.ProvidedIcons;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.placeholders.PlaceholderSources;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.gts.api.blacklist.Blacklist;
import net.impactdev.gts.api.listings.ui.EntrySelection;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.ui.Historical;
import net.impactdev.gts.sponge.listings.makeup.SpongeEntry;
import net.impactdev.gts.sponge.listings.ui.AbstractSpongeEntryUI;
import net.impactdev.gts.ui.SpongeMainMenu;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.ui.containers.ImpactorUI;
import net.impactdev.impactor.api.ui.containers.icons.DisplayProvider;
import net.impactdev.impactor.api.ui.containers.icons.Icon;
import net.impactdev.impactor.api.ui.containers.layouts.Layout;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.math.vector.Vector2i;

import java.util.Optional;
import java.util.function.Supplier;

import static net.impactdev.gts.sponge.utils.Utilities.readMessageConfigOption;

public class SpongeItemUI extends AbstractSpongeEntryUI<ChosenItemEntry> implements Historical<SpongeMainMenu> {

    public SpongeItemUI(ServerPlayer viewer) {
        super(viewer);
    }

    @Override
    protected ImpactorUI.UIBuilder modifyDisplayBuilder(ImpactorUI.UIBuilder builder) {
        return builder.onClick(context -> {
            int slot = context.require(Integer.class);
            System.out.println(slot);


//            if(!transaction.getOriginal().getType().equals(ItemTypes.AIR)) {
//                ItemStackSnapshot clicked = transaction.getOriginal();
//                if(this.chosen != null) {
//                    this.viewer.sendMessage(Text.of(TextColors.RED, "You've already selected an item..."));
//                    return;
//                }
//
//                MessageService<Text> parser = Impactor.getInstance().getRegistry().get(MessageService.class);
//                Blacklist blacklist = Impactor.getInstance().getRegistry().get(Blacklist.class);
//                if(blacklist.isBlacklisted(ItemType.class, clicked.getType().getName())) {
//                    this.viewer.sendMessage(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_BLACKLISTED)));
//                    this.viewer.playSound(SoundTypes.BLOCK_ANVIL_LAND, this.viewer.getPosition(), 1, 1);
//                    return;
//                }
//
//                final int s = slot.getValue() - 45;
//
//                this.chosen = new ChosenItemEntry(clicked, this.getTargetSlotIndex(slot.getValue() - 45));
//                this.getDisplay().setSlot(13, this.createChosenIcon());
//                this.getDisplay().setSlot(44, this.generateConfirmIcon());
//                this.style(true);
//            }
            return false;
        });
    }

    @Override
    protected Component getTitle() {
        return Component.text("Listing Creator - Items");
    }

    @Override
    protected Vector2i getDimensions() {
        return Vector2i.from(9, 5);
    }

    @Override
    protected EntrySelection<? extends SpongeEntry<?>> getSelection() {
        return this.chosen;
    }

    @Override
    protected int getChosenSlot() {
        return 13;
    }

    @Override
    protected int getPriceSlot() {
        return 38;
    }

    @Override
    protected int getSelectionTypeSlot() {
        return 40;
    }

    @Override
    protected int getTimeSlot() {
        return 42;
    }

    @Override
    protected int getConfirmSlot() {
        return 44;
    }

    @Override
    protected double getMinimumMonetaryPrice(ChosenItemEntry chosen) {
        return 1;
    }

    @Override
    public Optional<ChosenItemEntry> getChosenOption() {
        return Optional.ofNullable(this.chosen);
    }

    @Override
    public void open(ServerPlayer user) {
        this.getDisplay().open(PlatformPlayer.from(user));
    }

    @Override
    public Layout getDesign() {
        final MessageService PARSER = Utilities.PARSER;

        Layout.LayoutBuilder slb = Layout.builder();
        slb.size(4).border(ProvidedIcons.BORDER).size(5);
        slb.slots(this.border(ItemTypes.RED_STAINED_GLASS_PANE.get()), 3, 4, 5, 10, 11, 12, 14, 15, 16, 21, 22, 23);
        slb.slots(ProvidedIcons.BORDER, 19, 20, 24, 25, 37, 43);

        slb.slot(this.createNoneChosenIcon(), 13);

        PlaceholderSources sources = PlaceholderSources.builder()
                .append(ServerPlayer.class, () -> this.viewer)
                .build();
        Icon<ItemStack> back = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                        .itemType(ItemTypes.BARRIER)
                        .add(Keys.CUSTOM_NAME, PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_GENERAL_BACK), sources))
                        .build()
                ))
                .listener(context -> {
                    this.getParent().ifPresent(parent -> parent.get().open());
                    return false;
                })
                .build();

        slb.slot(this.createPriceIcon(), 38);
        slb.slot(GTSPlugin.instance().configuration().main().get(ConfigKeys.BINS_ENABLED) ? this.createBINIcon() : this.createAuctionIcon(), 40);
        slb.slot(this.createTimeIcon(), 42);
        slb.slot(this.generateWaitingIcon(false), 44);

        return slb.build();
    }

    @Override
    public Optional<Supplier<SpongeMainMenu>> getParent() {
        return Optional.of(() -> new SpongeMainMenu(this.viewer));
    }

    @Override
    public Icon<ItemStack> createChosenIcon() {
        return Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(this.chosen.getSelection().createStack()))
                .listener(context -> {
                    this.getDisplay().set(this.createNoneChosenIcon(), 13);
                    this.getDisplay().set(this.generateWaitingIcon(false), 44);
                    this.style(false);

                    this.chosen = null;
                    return false;
                })
                .build();
    }

    private int getTargetSlotIndex(int input) {
        if(input >= 27) {
            return input - 27;
        }

        return input + 9;
    }

}
