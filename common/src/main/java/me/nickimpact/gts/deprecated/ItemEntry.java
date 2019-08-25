package me.nickimpact.gts.deprecated;

import com.nickimpact.impactor.api.json.JsonTyping;
import me.nickimpact.gts.api.deprecated.Entry;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.item.inventory.ItemStack;

@Deprecated
@JsonTyping("item")
public class ItemEntry extends Entry<DataContainer, ItemStack> {

	public ItemEntry(){}

	@Override
	public ItemStack getEntry() {
		return ItemStack.builder().fromContainer(this.element).build();
	}
}
