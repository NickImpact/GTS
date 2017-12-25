package com.nickimpact.gts.entries.items;

import com.google.common.collect.Lists;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.GTSInfo;
import com.nickimpact.gts.api.json.Typing;
import com.nickimpact.gts.api.listings.entries.Entry;
import com.nickimpact.gts.api.listings.pricing.Price;
import net.minecraft.init.Items;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents an ItemStack that can be added as an entry into the GTS market. The generified typing
 * here represents the lower-level phase of the ItemStack, as an ItemStack in a generic setup
 * like such produces a MalformedParameterizedTypeException, prevention serialization. Luckily
 * for us, Sponge provides a way to go from this low-level back up, so we can simply store the
 * data, then cache the ItemStack once it is recreated.
 *
 * @author NickImpact
 */
@Typing("Item")
public class ItemEntry extends Entry<DataContainer> {

	/** The cached version of the ItemStack */
	private transient ItemStack item;

	public ItemEntry(ItemStack element, Price price) {
		super(element.toContainer(), price);
		this.item = element;
	}

	private ItemStack decode() {
		return item != null ? item : (item = ItemStack.builder().fromContainer(this.element).build());
	}

	@Override
	public String getName() {
		return decode().getType().getName();
	}

	@Override
	protected ItemStack baseItemStack(Player player) {
		return decode();
	}

	@Override
	protected String baseTitleTemplate() {
		return "{{item_title}}";
	}

	@Override
	protected List<String> baseLoreTemplate() {
		List<String> output = Lists.newArrayList(
				"&7Listing ID: &e{{id}}",
				"&7Seller: &e{{seller}}",
				"&7Price: &e{{price}}",
				"&7Time Left: &e{{time_left}}"
		);

		this.decode().get(Keys.ITEM_LORE).ifPresent(lore -> {
			output.add("&aItem Lore:");
			output.addAll(lore.stream().map(Text::toPlain).collect(Collectors.toList()));
		});

		return output;
	}

	@Override
	protected ItemStack confirmItemStack(Player player) {
		return decode();
	}

	@Override
	protected String confirmTitleTemplate() {
		return this.baseTitleTemplate();
	}

	@Override
	protected List<String> confirmLoreTemplate() {
		return this.baseLoreTemplate();
	}

	@Override
	public boolean giveEntry(User user){
		((Player)user).getInventory().offer(this.decode());
		return true;
	}

	@Override
	public boolean doTakeAway(Player player) {
		Optional<ItemStack> opt = player.getInventory().query(this.element).poll();
		return opt.isPresent();
	}
}
