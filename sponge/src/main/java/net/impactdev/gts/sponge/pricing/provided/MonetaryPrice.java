package net.impactdev.gts.sponge.pricing.provided;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.pricing.SpongePrice;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.gui.UI;
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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
				.append(TextComponent.of(economy.getDefaultCurrency().format(this.getPrice()).toPlain()))
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
	public void pay(UUID payer, @Nullable Object source) {
		economy.getOrCreateAccount(payer).get().withdraw(economy.getDefaultCurrency(), this.price, Sponge.getCauseStackManager().getCurrentCause());
	}

	@Override
	public boolean reward(UUID recipient) {
		return economy.getOrCreateAccount(recipient).get()
				.deposit(economy.getDefaultCurrency(), this.price, Sponge.getCauseStackManager().getCurrentCause())
				.getResult().equals(ResultType.SUCCESS);
	}

	@Override
	public Class<Void> getSourceType() {
		return Void.class;
	}

	@Override
	public long calculateFee(boolean listingType) {
		Config config = GTSPlugin.getInstance().getConfiguration();
		if(listingType) {
			return Math.round(this.price.doubleValue() * config.get(ConfigKeys.FEES_STARTING_PRICE_RATE_BIN));
		} else {
			return Math.round(this.price.doubleValue() * config.get(ConfigKeys.FEES_STARTING_PRICE_RATE_AUCTION));
		}
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

								Impactor.getInstance().getScheduler().executeSync(() -> {
									callback.accept(ui, null);
								});
								return false;
							} catch (Exception e) {
								Impactor.getInstance().getScheduler().executeSync(() -> {
									callback.accept(ui, null);
								});
								return false;
							}
						})
						.reopenOnFailure(false)
						.build();
				viewer.closeInventory();
				query.sendTo(viewer);
			};
		}

		@Override
		public <U extends UI<?, ?, ?, ?>> Optional<PriceSelectorUI<U>> getSelector(Player viewer, Price<?, ?, ?> price, Consumer<Object> callback) {
			return Optional.empty();
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
