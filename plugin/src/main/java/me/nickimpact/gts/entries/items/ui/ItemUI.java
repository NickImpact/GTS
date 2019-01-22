package me.nickimpact.gts.entries.items.ui;

import com.nickimpact.impactor.gui.v2.Icon;
import com.nickimpact.impactor.gui.v2.Layout;
import com.nickimpact.impactor.gui.v2.UI;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.InventoryTransformation;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.type.GridInventory;
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
                .dimension(InventoryDimension.of(9, 6)) // Double chest + player inventory
                .build(GTS.getInstance());



        return display.define(this.forgeLayout(player));
    }

    @Override
    protected Layout forgeLayout(Player player) {
        Layout.Builder lb = Layout.builder().dimension(9, 6);
        lb.row(Icon.BORDER, 0).row(Icon.BORDER, 2);
        lb.column(Icon.BORDER, 0).column(Icon.BORDER, 8);

        lb.slot(Icon.from(ItemStack.builder().itemType(ItemTypes.BARRIER).add(Keys.DISPLAY_NAME, Text.of(TextColors.GRAY, "Select an Item...")).build()), 13);

        // Setup payment and amount selectors

        // Initialize click events for rest of the Inventory
        lb.dimension(9, 10);
        lb = this.addSlots(player, lb, GridInventory.class, 54);
	    lb = this.addSlots(player, lb, Hotbar.class, 81);

        return lb.build();
    }

	@Override
	protected double getMin() {
		return 0;
	}

	@Override
	protected double getMax() {
		return 100_000_000;
	}

	@Override
	protected void update() {

	}

	private <T extends Inventory> Layout.Builder addSlots(Player player, Layout.Builder lb, Class<T> invType, int start) {
    	int i = 0;
    	MainPlayerInventory main = player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class));
    	Iterable<Slot> slots;
    	if(invType.equals(GridInventory.class)) {
    		slots = main.getGrid().slots();
	    } else {
    		slots = main.getHotbar().slots();
	    }

		for(Slot slot : slots) {
			Optional<ItemStack> item = slot.peek();
			if(item.isPresent()) {
				Icon icon = Icon.from(item.get());
				final int s = i;
				icon.addListener(clickable -> {
					if(this.selection != null && this.selection.equalTo(icon.getDisplay())) {
						return;
					}

					this.selection = icon.getDisplay();
					this.display.setSlot(13, Icon.from(item.get()));
				});

				lb = lb.slot(icon, start + i);
			}
			i++;
		}

		return lb;
	}
}
