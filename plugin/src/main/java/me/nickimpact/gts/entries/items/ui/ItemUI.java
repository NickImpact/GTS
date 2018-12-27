package me.nickimpact.gts.entries.items.ui;

import com.nickimpact.impactor.gui.v2.Icon;
import com.nickimpact.impactor.gui.v2.Layout;
import com.nickimpact.impactor.gui.v2.UI;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.Optional;

public class ItemUI extends EntryUI {

    private UI display;
    private ItemStack selection;
    private int amount;
    private BigDecimal price;

    /** Hard set min and maxes. Note that max can and likely will be modified based on max stack limit */
    private static final int MIN = 1;
    private static final int MAX = 64;

    public ItemUI() {}

    private ItemUI(Player player) {
        super(player);
        this.display = this.createUI(player);
    }

    @Override
    public ItemUI createFor(Player player) {
        return new ItemUI(player);
    }

    @Override
    public UI getDisplay() {
        return this.display;
    }

    @Override
    protected UI createUI(Player player) {
        UI display = UI.builder()
                .title(Text.of(TextColors.RED, "GTS ", TextColors.GRAY, "(", TextColors.DARK_AQUA, "Items", TextColors.GRAY, ")"))
                .archetype(InventoryArchetypes.DOUBLE_CHEST)
                .dimension(InventoryDimension.of(9, 10)) // Double chest + player inventory
                .build(GTS.getInstance());

        return display.define(this.forgeLayout(player));
    }

    @Override
    protected Layout forgeLayout(Player player) {
        Layout.Builder lb = Layout.builder().dimension(9, 6);
        lb.row(Icon.BORDER, 0).row(Icon.BORDER, 2);
        lb.column(Icon.BORDER, 0).column(Icon.BORDER, 8).slots(Icon.BORDER, 12, 14);

        lb.slot(Icon.from(ItemStack.builder().itemType(ItemTypes.BARRIER).add(Keys.DISPLAY_NAME, Text.of(TextColors.GRAY, "Select an Item...")).build()), 13);

        // Setup payment and amount selectors

        // Initialize click events for rest of the Inventory
        lb.dimension(9, 10);
        MainPlayerInventory pInv = player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class));
        for(int i = 0; i < 36; i++) {
            Optional<ItemStack> item = pInv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(i))).first().peek();
            if(item.isPresent()) {
                Icon icon = Icon.from(item.get());
                icon.addListener(clickable -> {
                    if(this.selection.equalTo(icon.getDisplay())) {
                        return;
                    }

                    this.selection = icon.getDisplay();
                    this.display.setSlot(13, Icon.from(item.get()));
                });

                lb.slot(icon, 54 + i);
            }
        }

        return lb.build();
    }
}
