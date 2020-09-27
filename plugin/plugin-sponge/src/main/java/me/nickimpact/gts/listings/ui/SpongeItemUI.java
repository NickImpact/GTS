package me.nickimpact.gts.listings.ui;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.Impactor;
import com.nickimpact.impactor.api.services.text.MessageService;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeLayout;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.nickimpact.gts.api.blacklist.Blacklist;
import me.nickimpact.gts.api.listings.prices.Price;
import me.nickimpact.gts.api.listings.ui.EntrySelection;
import me.nickimpact.gts.common.config.MsgConfigKeys;
import me.nickimpact.gts.common.ui.Historical;
import me.nickimpact.gts.listings.SpongeItemEntry;
import me.nickimpact.gts.sponge.listings.makeup.SpongeEntry;
import me.nickimpact.gts.sponge.listings.ui.AbstractSpongeEntryUI;
import me.nickimpact.gts.sponge.pricing.provided.MonetaryPrice;
import me.nickimpact.gts.ui.SpongeMainMenu;
import me.nickimpact.gts.sponge.utils.Utilities;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.function.Supplier;

import static me.nickimpact.gts.sponge.utils.Utilities.readMessageConfigOption;

public class SpongeItemUI extends AbstractSpongeEntryUI<SpongeItemUI.Chosen> implements Historical<SpongeMainMenu> {

    public SpongeItemUI(Player viewer) {
        super(viewer);
        this.getDisplay().attachListener((player, event) -> {
                    event.getTransactions().forEach(transaction -> {
                        transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                            if(slot.getValue() >= 45) {
                                if(!transaction.getOriginal().getType().equals(ItemTypes.AIR)) {
                                    ItemStackSnapshot clicked = transaction.getOriginal();
                                    if(this.chosen != null) {
                                        this.viewer.sendMessage(Text.of(TextColors.RED, "You've already selected an item..."));
                                        return;
                                    }

                                    Blacklist blacklist = Impactor.getInstance().getRegistry().get(Blacklist.class);
                                    if(blacklist.isBlacklisted(ItemType.class, clicked.getType().getName())) {
                                        this.viewer.sendMessage(Text.of(TextColors.RED, "Blacklisted"));
                                        this.viewer.playSound(SoundTypes.BLOCK_ANVIL_LAND, this.viewer.getPosition(), 1, 1);
                                        return;
                                    }

                                    final int s = slot.getValue() - 45;

                                    this.chosen = new Chosen(clicked, s);
                                    this.getDisplay().setSlot(13, this.createChosenIcon());
                                    this.getDisplay().setSlot(44, this.generateConfirmIcon());
                                    this.style(true);
                                }
                            }
                        });
                    });
                });
        this.getDisplay().define(this.design());
    }

    @Override
    protected Text getTitle() {
        return Text.of("Listing Creator - Items");
    }

    @Override
    protected InventoryDimension getDimensions() {
        return InventoryDimension.of(9, 5);
    }

    @Override
    protected EntrySelection<? extends SpongeEntry<?>> getSelection() {
        return this.chosen;
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
    public Optional<Chosen> getChosenOption() {
        return Optional.ofNullable(this.chosen);
    }

    @Override
    public void open(Player user) {
        this.getDisplay().open(user);
    }

    private SpongeLayout design() {
        final MessageService<Text> PARSER = Utilities.PARSER;

        SpongeLayout.SpongeLayoutBuilder slb = SpongeLayout.builder();
        slb.dimension(9, 4).border().dimension(9, 5);
        slb.slots(this.border(DyeColors.RED), 3, 4, 5, 10, 11, 12, 14, 15, 16, 21, 22, 23);
        slb.slots(SpongeIcon.BORDER, 19, 20, 24, 25, 37, 43);

        slb.slot(this.createNoneChosenIcon(), 13);

        SpongeIcon back = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.BARRIER)
                .add(Keys.DISPLAY_NAME, PARSER.parse(readMessageConfigOption(MsgConfigKeys.UI_GENERAL_BACK), Lists.newArrayList(() -> this.viewer)))
                .build()
        );
        back.addListener(clickable -> {
            this.getParent().ifPresent(parent -> parent.get().open());
        });
        slb.slot(back, 36);

        SpongeIcon price = this.createPriceIcon();
        slb.slot(price, 38);

        slb.slot(this.createBINIcon(), 40);

        SpongeIcon time = this.createTimeIcon();
        slb.slot(time, 42);

        SpongeIcon waiting = this.generateWaitingIcon(false);
        slb.slot(waiting, 44);

        return slb.build();
    }

    @Override
    public Optional<Supplier<SpongeMainMenu>> getParent() {
        return Optional.of(() -> new SpongeMainMenu(this.viewer));
    }

    @Override
    public SpongeIcon createChosenIcon() {
        SpongeIcon icon = new SpongeIcon(this.chosen.getSelection().createStack());
        icon.addListener(clickable -> {
            this.getDisplay().setSlot(13, this.createNoneChosenIcon());
            this.getDisplay().setSlot(44, this.generateWaitingIcon(false));
            this.style(false);

            this.chosen = null;
        });
        return icon;
    }

    @Override
    public SpongeIcon createSelectionTypeIcon() {
        return null;
    }

    @Getter
    @RequiredArgsConstructor
    protected static class Chosen implements EntrySelection<SpongeItemEntry> {
        private final ItemStackSnapshot selection;
        private final int slot;

        @Override
        public SpongeItemEntry createFromSelection() {
            return new SpongeItemEntry(this.selection);
        }
    }

}
