package me.nickimpact.gts.sponge.pricing.provided;

import lombok.Setter;
import me.nickimpact.gts.api.listings.makeup.Display;
import me.nickimpact.gts.common.config.ConfigKeys;
import me.nickimpact.gts.sponge.SpongePlugin;
import me.nickimpact.gts.sponge.pricing.SpongePrice;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.util.UUID;

public class MoneyPrice implements SpongePrice<CurrencyValue> {

	@Setter private static EconomyService economy;
	private final CurrencyValue price;

	public MoneyPrice(double price) {
		this.price = new CurrencyValue(new BigDecimal(price));
	}

	@Override
	public TextComponent getText() {
		return TextComponent.builder()
				.append(TextComponent.of(economy.getDefaultCurrency().format(this.getPrice().getPricable()).toPlain()).color(TextColor.YELLOW))
				.build();
	}

	@Override
	public Display<ItemStack> getDisplay() {
		return () -> ItemStack.builder()
				.itemType(ItemTypes.GOLD_INGOT)
				.add(Keys.DISPLAY_NAME, Text.of(this.getText()))
				.build();
	}

	@Override
	public CurrencyValue getPrice() {
		return this.price;
	}

	@Override
	public boolean canPay(UUID payer) {
		return economy.getOrCreateAccount(payer).get().getBalance(economy.getDefaultCurrency()).compareTo(price.getPricable()) >= 0;
	}

	@Override
	public void pay(UUID payer) {
		economy.getOrCreateAccount(payer).get().withdraw(economy.getDefaultCurrency(), price.getPricable(), Sponge.getCauseStackManager().getCurrentCause());
	}

	@Override
	public void reward(UUID recipient) {
		economy.getOrCreateAccount(recipient).get().deposit(economy.getDefaultCurrency(), price.getPricable(), Sponge.getCauseStackManager().getCurrentCause());
	}

	@Override
	public double getTax() {
		return price.getPricable().doubleValue() * SpongePlugin.getInstance().getConfiguration().get(ConfigKeys.TAX_MONEY_TAX);
	}

}
