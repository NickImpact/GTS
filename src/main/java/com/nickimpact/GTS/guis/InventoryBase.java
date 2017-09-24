package com.nickimpact.GTS.guis;

import com.nickimpact.GTS.GTS;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.type.OrderedInventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InventoryBase {

	private Inventory.Builder builder;
	private Map<Integer, InventoryIcon> icons;
	private Inventory inventory;

	private int size;
	private boolean border = false;

	public InventoryBase(int size, Text title){
		this.size = size;
		this.icons = new HashMap<>();
		this.builder = Inventory.builder()
				.property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(title))
				.property(InventoryDimension.PROPERTY_NAME, InventoryDimension.of(9, size))
				.listener(ClickInventoryEvent.class, event -> event.setCancelled(true));

	}

	/**
	 * Retrieves a copy of the current inventory builder.
	 * Typically used before running usage of {@link #buildInventory()} buildInventory()}
	 * to help apply new properties to the viewed inventory
	 *
	 * @return A {@link Inventory.Builder} reference to the current inventory build
	 */
	public Inventory.Builder getInventoryBuilder() {
		return this.builder;
	}

	/**
	 * Adds a {@link InventoryIcon} display to the Mapped table of icons for the UI.
	 *
	 * @param icon The icon we are planning to display
	 */
	public void addIcon(InventoryIcon icon) {
		this.icons.put(icon.getSlot(), icon);
	}

	/**
	 * Attempts to locate a registered icon in the current icon registry for the inventory
	 *
	 * @param slot The slot index of an icon
	 * @return An Optional value, either with an icon or empty when the slot has a null icon
	 */
	public Optional<InventoryIcon> getIcon(int slot) {
		return Optional.ofNullable(this.icons.get(slot));
	}

	/**
	 * Retrieves a copy of all registered icons
	 *
	 * @return A mapping of all icons with their assigned slot index
	 */
	public Map<Integer, InventoryIcon> getAllIcons(){
		return this.icons;
	}

	/**
	 * Draws a border around the inventory, with the top and bottom of the interface
	 * being completely drawn in, and sides with only the walls drawn.
	 *
	 * @param rows The number of rows to draw
	 */
	public void drawBorder(int rows, DyeColor color)
	{
		ItemStack border = ItemStack.builder()
				.itemType(ItemTypes.STAINED_GLASS_PANE)
				.keyValue(Keys.DISPLAY_NAME, Text.of(TextColors.BLACK, ""))
				.keyValue(Keys.DYE_COLOR, color)
				.build();

		for (int y = 0; y < rows; y++)
		{
			if(y == 0 || y == rows - 1){
				for(int x = 0; x < 9; x++){
					this.addIcon(new InventoryIcon(x + (9 * y), border));
				}
			} else {
				this.addIcon(new InventoryIcon((9 * y), border));
				this.addIcon(new InventoryIcon(8 + (9 * y), border));
			}
		}

		this.border = true;
	}

	/**
	 * Attempts to ensure an item is within the drawn border,
	 * if one were drawn at all.
	 *
	 * @param slot The slot to check against
	 * @return True if within border or no border exists, false otherwise
	 */
	public boolean isInBorder(int slot){
		return !this.border || slot % 9 != 0 && slot % 9 != 8 && slot > 9 && slot < (size - 1) * 9;
	}

	/**
	 * Like its counterpart, this method will only update the
	 * specified slot positions, allowing for quicker operation
	 * and less flicker within the UI itself
	 *
	 * Note: Due to a limitation in the Sponge API, we can't dynamically attach
	 *       new listeners to slots on an update. If an item is to change in a
	 *       slot, the listener provided by that original icon will persist.
	 *       Really only use this setup if you are purely doing cosmetic changes,
	 *       or your click listeners are setup to dynamically detect the change
	 *       of the icon.
	 *
	 * @param slots The slot indexes to modify
	 */
	public void updateContents(int... slots){
		OrderedInventory orderedInventory = this.inventory.query(OrderedInventory.class);
		this.icons.forEach((index, inventoryIcon) -> {
			for(int sl : slots){
				if(sl == index){
					Slot slot = orderedInventory.getSlot(
							SlotIndex.of(index)).orElseThrow(() -> new IllegalArgumentException("Invalid index: " + index));
					slot.set(inventoryIcon.getDisplay());
				}
			}
		});
	}

	/**
	 * With the case of an open inventory, we will update all the inventory contents
	 * to their updated slot icons.
	 *
	 * Note: Due to a limitation in the Sponge API, we can't dynamically attach
	 *       new listeners to slots on an update. If an item is to change in a
	 *       slot, the listener provided by that original icon will persist.
	 *       Really only use this setup if you are purely doing cosmetic changes,
	 *       or your click listeners are setup to dynamically detect the change
	 *       of the icon.
	 */
	public void updateContents() {
		OrderedInventory orderedInventory = this.inventory.query(OrderedInventory.class);
		orderedInventory.clear();
		this.icons.forEach((index, inventoryIcon) -> {
			Slot slot = orderedInventory.getSlot(
					SlotIndex.of(index)).orElseThrow(() -> new IllegalArgumentException("Invalid index: " + index));
			slot.set(inventoryIcon.getDisplay());
		});
	}

	/**
	 * Forges a Sponge Inventory. Starts by composing all registered item mappings
	 * to their assigned slots, then proceeds to return the updated inventory.
	 *
	 * @return An {@link Inventory} with all icons sorted
	 */
	public Inventory getInventory() {
		if (this.inventory == null) {
			buildInventory();
		}
		return this.inventory;
	}

	/**
	 * For each and every display icon, register their click events if they have any,
	 * then proceed to establish the inventory. From there, we place each icon in their
	 * assigned slots.
	 */
	private void buildInventory() {
		this.icons.values().forEach(button -> button.getListeners().forEach((clazz, listener) -> this.builder.listener(clazz, listener)));
		this.inventory = this.builder.build(GTS.getInstance());

		OrderedInventory orderedInventory = this.inventory.query(OrderedInventory.class);
		this.icons.forEach((index, inventoryIcon) -> {
			Slot slot = orderedInventory.getSlot(SlotIndex.of(index)).orElseThrow(() -> new IllegalArgumentException("Invalid index: " + index));
			slot.set(inventoryIcon.getDisplay());
		});
	}
}
