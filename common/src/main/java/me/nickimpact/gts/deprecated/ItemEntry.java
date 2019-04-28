package me.nickimpact.gts.deprecated;

import com.nickimpact.impactor.api.json.JsonTyping;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.item.inventory.ItemStack;

@Deprecated
@JsonTyping("Item")
public class ItemEntry extends Entry<DataContainer, ItemStack> {
	@Override
	public ItemStack getEntry() {
		return ItemStack.builder().fromContainer(this.element).build();
	}
}
