package me.nickimpact.gts.entries.items.ui;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.gui.v2.Icon;
import com.nickimpact.impactor.gui.v2.Layout;
import com.nickimpact.impactor.gui.v2.UI;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import me.nickimpact.gts.configuration.ConfigKeys;
import me.nickimpact.gts.entries.items.ItemEntry;
import me.nickimpact.gts.entries.prices.MoneyPrice;
import me.nickimpact.gts.ui.SellUI;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Optional;

public class ItemUI extends EntryUI {

    private UI display;
    private ItemStack selection;

    /** Hard set min and maxes. Note that max can and likely will be modified based on max stack limit */
    private static final int MIN = 1;
    private static final int MAX = 64;

    private int size = 1;

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
                .build(GTS.getInstance())
		        .attachExtraListener((event, pl) -> {
		        	event.getTransactions().forEach(transaction -> {
		        		transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
		        			if(slot.getValue() >= 54) {
		        				if(!transaction.getOriginal().getType().equals(ItemTypes.AIR)) {
							        ItemStack clicked = transaction.getOriginal().createStack();
							        if (this.selection != null && this.selection.equalTo(clicked)) {
								        return;
							        }

							        this.selection = clicked;
							        this.display.setSlot(13, Icon.from(clicked));
							        this.update();
						        }
					        }
				        });
			        });
		        });



        return display.define(this.forgeLayout(player));
    }

    @Override
    protected Layout forgeLayout(Player player) {
        Layout.Builder lb = Layout.builder().dimension(9, 6);
        lb.row(Icon.BORDER, 0).row(Icon.BORDER, 2);
        lb.slots(Icon.BORDER, 9, 17, 34, 43, 52);

        lb.slot(Icon.from(ItemStack.builder().itemType(ItemTypes.BARRIER).add(Keys.DISPLAY_NAME, Text.of(TextColors.GRAY, "Select an Item...")).build()), 13);

        // Setup payment and amount selectors
	    lb.slot(this.increase, 28);
	    lb.slot(this.money, 37);
	    lb.slot(this.decrease, 46);

	    List<Icon> amounts = this.amountIcons();
	    lb.slot(amounts.get(0), 30);
	    lb.slot(amounts.get(1), 39);
	    lb.slot(amounts.get(2), 48);

	    lb.slot(this.timeInc, 32);
	    lb.slot(this.timeIcon, 41);
	    lb.slot(this.timeDec, 50);

        // Initialize click events for rest of the Inventory
//      lb = this.addSlots(player, lb, GridInventory.class, 54);
//	    lb = this.addSlots(player, lb, Hotbar.class, 81);

	    Icon confirm = Icon.from(Icon.CONFIRM.getDisplay());
	    confirm.addListener(clickable -> {
		    if(this.selection != null) {
			    this.display.close(clickable.getPlayer());
		        Listing listing = Listing.builder()
					    .entry(new ItemEntry(ItemStack.builder().fromItemStack(this.selection).quantity(this.size).build(), new MoneyPrice(this.amount)))
					    .doesExpire()
					    .expiration(this.time)
					    .player(player)
					    .build();
		        listing.publish(player);
		    }
	    });
	    lb.slot(confirm, 35);

	    Icon cancel = Icon.from(Icon.CANCEL.getDisplay());
	    cancel.getDisplay().offer(Keys.DYE_COLOR, DyeColors.RED);
	    cancel.addListener(clickable -> {
		    this.display.close(clickable.getPlayer());
		    new SellUI(clickable.getPlayer()).open(clickable.getPlayer(), 1);
	    });
	    lb.slot(cancel, 53);

        return lb.build();
    }

	@Override
	protected double getMin() {
		return 1;
	}

	@Override
	protected double getMax() {
		return 100_000_000;
	}

	@Override
	protected long getTimeMin() {
		return 1800;
	}

	@Override
	protected long getTimeMax() {
		return 3600 * 24;
	}

	@Override
	protected void update() {
		this.display.setSlot(37, this.moneyIcon());
		this.display.setSlot(39, this.amountIcons().get(1));
		this.display.setSlot(41, this.timeIcon());
	}

	@Override
	public double getLeftClickBaseAmount() {
		return GTS.getInstance().getConfig().get(ConfigKeys.PRICING_LEFTCLICK_BASE);
	}

	@Override
	public double getRightClickBaseAmount() {
		return GTS.getInstance().getConfig().get(ConfigKeys.PRICING_RIGHTCLICK_BASE);
	}

	@Override
	public double getLeftClickShiftAmount() {
		return GTS.getInstance().getConfig().get(ConfigKeys.PRICING_LEFTCLICK_SHIFT);
	}

	@Override
	public double getRightClickShiftAmount() {
		return GTS.getInstance().getConfig().get(ConfigKeys.PRICING_RIGHTCLICK_SHIFT);
	}

	private List<Icon> amountIcons() {
    	List<Icon> icons = Lists.newArrayList();

    	Icon inc = Icon.from(
			    ItemStack.builder()
					    .itemType(ItemTypes.DYE)
					    .add(Keys.DYE_COLOR, DyeColors.LIME)
					    .add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Increase Amount"))
					    .add(Keys.ITEM_LORE, Lists.newArrayList(
							    Text.of(TextColors.GRAY, "Left Click: ", TextColors.AQUA, "+1"),
							    Text.of(TextColors.GRAY, "Right Click: ", TextColors.AQUA, "+5"),
							    Text.of(TextColors.GRAY, "Shift + Left Click: ", TextColors.AQUA, "+10"),
							    Text.of(TextColors.GRAY, "Shift + Right Click: ", TextColors.AQUA, "Max Stack Size")
					    ))
					    .build()
	    );
		inc.addListener(clickable -> {
			int max = this.selection != null ? this.selection.getQuantity() : MAX;

			if(clickable.getEvent() instanceof ClickInventoryEvent.Shift) {
				if(clickable.getEvent() instanceof ClickInventoryEvent.Shift.Primary) {
					this.size = Math.min(max, this.size + 10);
				} else {
					this.size = max;
				}
			} else if (clickable.getEvent() instanceof ClickInventoryEvent.Primary) {
				this.size = Math.min(max, this.size + 1);
			} else if(clickable.getEvent() instanceof ClickInventoryEvent.Secondary) {
				this.size = Math.min(max, this.size + 5);
			}
			this.update();
		});

		Icon display = Icon.from(ItemStack.builder()
				.itemType(ItemTypes.PAPER)
				.add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, "Amount of Item"))
				.add(Keys.ITEM_LORE, Lists.newArrayList(
						Text.of(TextColors.GRAY, "Target Amount: ", TextColors.GREEN, this.size),
						Text.EMPTY,
						Text.of(TextColors.GRAY, "Min Size: ", TextColors.GREEN, 1),
						Text.of(TextColors.GRAY, "Max Size: ", TextColors.GREEN, this.selection != null ? this.selection.getQuantity() : MAX))
				)
				.build());

		Icon dec = Icon.from(
				ItemStack.builder()
						.itemType(ItemTypes.DYE)
						.add(Keys.DYE_COLOR, DyeColors.RED)
						.add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Decrease Amount"))
						.add(Keys.ITEM_LORE, Lists.newArrayList(
								Text.of(TextColors.GRAY, "Left Click: ", TextColors.AQUA, "-1"),
								Text.of(TextColors.GRAY, "Right Click: ", TextColors.AQUA, "-5"),
								Text.of(TextColors.GRAY, "Shift + Left Click: ", TextColors.AQUA, "-10"),
								Text.of(TextColors.GRAY, "Shift + Right Click: ", TextColors.AQUA, "Size of 1")
						))
						.build()
		);
		dec.addListener(clickable -> {
			if(clickable.getEvent() instanceof ClickInventoryEvent.Shift) {
				if(clickable.getEvent() instanceof ClickInventoryEvent.Shift.Primary) {
					this.size = Math.max(1, this.size - 10);
				} else {
					this.size = 1;
				}
			} else if (clickable.getEvent() instanceof ClickInventoryEvent.Primary) {
				this.size = Math.max(1, this.size - 1);
			} else if(clickable.getEvent() instanceof ClickInventoryEvent.Secondary) {
				this.size = Math.max(1, this.size - 5);
			}
			this.update();
		});

		icons.add(inc);
		icons.add(display);
		icons.add(dec);

		return icons;
	}
}
