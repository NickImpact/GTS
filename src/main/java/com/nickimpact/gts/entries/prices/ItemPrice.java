package com.nickimpact.gts.entries.prices;

import com.nickimpact.gts.api.json.Typing;
import com.nickimpact.gts.api.listings.pricing.Price;
import com.nickimpact.gts.api.listings.pricing.PricingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Typing("Item")
public class ItemPrice extends Price<DataContainer> {

	private transient ItemStack price;

	public ItemPrice(ItemStack price) {
		super(price.toContainer());
		this.price = price;
	}

	private ItemStack decode() {
		return this.price != null ? this.price : (this.price = ItemStack.builder().fromContainer(super.price).build());
	}

	@Override
	public Text getText() {
		return this.decode().get(Keys.DISPLAY_NAME).orElse(Text.of(this.decode().getType().getTranslation().get()));
	}

	@Override
	public boolean canPay(Player player) {
		return player.getInventory().query(this.decode()).capacity() > 0;
	}

	@Override
	public void pay(Player payer) {
		payer.getInventory().query(this.decode()).poll(this.decode().getQuantity());
	}

	@Override
	public BigDecimal calcTax(Player player) {
		return new BigDecimal(-1);
	}

	@Override
	public boolean supportsOfflineReward() {
		return false;
	}

	@Override
	public void reward(UUID uuid) {
		// The get method here will always return true due to an initial offline check by the purchase operation
		@SuppressWarnings("ConstantConditions")
		Player player = Sponge.getServer().getPlayer(uuid).get();
		player.getInventory().offer(this.decode());
	}

	@Override
	public void openCreateUI(Player player) {

	}
}
