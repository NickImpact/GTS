package net.impactdev.gts.sponge.pricing.provided;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import net.impactdev.gts.sponge.pricing.SpongePrice;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.gui.signs.SignQuery;
import net.impactdev.impactor.api.json.factory.JObject;
import lombok.Setter;
import net.impactdev.gts.api.data.registry.GTSKeyMarker;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.api.listings.prices.PriceManager;
import net.impactdev.gts.api.listings.ui.EntryUI;
import net.impactdev.gts.api.util.TriConsumer;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.BiConsumer;

@GTSKeyMarker("currency")
public class MonetaryPrice implements SpongePrice<BigDecimal, Void> {

	@Setter private static EconomyService economy;
	private final BigDecimal price;

	public MonetaryPrice(double price) {
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
		return economy.getOrCreateAccount(payer).get().getBalance(economy.getDefaultCurrency()).compareTo(this.price) >= 0;
	}

	@Override
	public void pay(UUID payer, Void source) {
		economy.getOrCreateAccount(payer).get().withdraw(economy.getDefaultCurrency(), this.price, Sponge.getCauseStackManager().getCurrentCause());
	}

	@Override
	public boolean reward(UUID recipient) {
		return economy.getOrCreateAccount(recipient).get()
				.deposit(economy.getDefaultCurrency(), this.price, Sponge.getCauseStackManager().getCurrentCause())
				.getResult().equals(ResultType.SUCCESS);
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

	public static MonetaryPrice deserialize(JsonObject json) {
		return new MonetaryPrice(json.get("value").getAsDouble());
	}

	public static class MonetaryPriceManager implements PriceManager<MonetaryPrice, Player> {

		@Override
		public TriConsumer<Player, EntryUI<?, ?, ?>, BiConsumer<EntryUI<?, ?, ?>, Price<?, ?, ?>>> process() {
			return (viewer, ui, callback) -> {
				SignQuery<Text, Player> query = SignQuery.<Text, Player>builder()
						.position(new Vector3d(0, 1, 0))
						.text(Lists.newArrayList(
								Text.of(""),
								Text.of("----------------"),
								Text.of("Enter a Price"),
								Text.of("for this Listing")
						))
						.response(submission -> {
							try {
								double value = Double.parseDouble(submission.get(0));
								if(value > 0) {
									SpongePrice<BigDecimal, Void> price = new MonetaryPrice(value);

									Impactor.getInstance().getScheduler().executeSync(() -> {
										callback.accept(ui, price);
									});
									return true;
								}
								return false;
							} catch (Exception e) {
								return false;
							}
						})
						.reopenOnFailure(true)
						.build();
				viewer.closeInventory();
				query.sendTo(viewer);
			};
		}

		@Override
		public String getName() {
			return "Monetary";
		}

		@Override
		public String getItemID() {
			return "minecraft:gold_nugget";
		}

		@Override
		public Deserializer<MonetaryPrice> getDeserializer() {
			return MonetaryPrice::deserialize;
		}
	}

}
