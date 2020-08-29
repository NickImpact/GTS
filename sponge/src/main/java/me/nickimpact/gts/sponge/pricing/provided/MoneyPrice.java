package me.nickimpact.gts.sponge.pricing.provided;

import com.google.gson.JsonObject;
import com.nickimpact.impactor.api.json.factory.JObject;
import lombok.Setter;
import me.nickimpact.gts.api.data.registry.GTSKeyMarker;
import me.nickimpact.gts.api.listings.makeup.Display;
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

@GTSKeyMarker("currency")
public class MoneyPrice implements SpongePrice<BigDecimal> {

	@Setter private static EconomyService economy;
	private final BigDecimal price;

	public MoneyPrice(double price) {
		this.price = new BigDecimal(price);
	}

	@Override
	public TextComponent getText() {
		return TextComponent.builder()
				.append(TextComponent.of(economy.getDefaultCurrency().format(this.getPrice()).toPlain()).color(TextColor.YELLOW))
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
	public BigDecimal getPrice() {
		return this.price;
	}

	@Override
	public boolean canPay(UUID payer) {
		return economy.getOrCreateAccount(payer).get().getBalance(economy.getDefaultCurrency()).compareTo(price) >= 0;
	}

	@Override
	public void pay(UUID payer) {
		economy.getOrCreateAccount(payer).get().withdraw(economy.getDefaultCurrency(), price, Sponge.getCauseStackManager().getCurrentCause());
	}

	@Override
	public void reward(UUID recipient) {
		economy.getOrCreateAccount(recipient).get().deposit(economy.getDefaultCurrency(), price, Sponge.getCauseStackManager().getCurrentCause());
	}

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public JObject serialize() {
		return new JObject()
				.add("version", this.getVersion())
				.add("value", this.price);
	}

	public static MoneyPrice deserialize(JsonObject json) {
		return new MoneyPrice(json.get("value").getAsDouble());
	}

}
