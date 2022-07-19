package net.impactdev.gts.sponge.pricing.provided;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.commands.CommandGenerator;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.pricing.SpongePrice;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.chat.ChatProcessor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.gts.api.data.registry.GTSKeyMarker;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.api.listings.prices.PriceManager;
import net.impactdev.gts.api.listings.ui.EntryUI;
import net.impactdev.gts.api.util.TriConsumer;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.ui.containers.ImpactorUI;
import net.impactdev.impactor.api.ui.signs.SignQuery;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.math.vector.Vector3d;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@GTSKeyMarker("currency")
public class MonetaryPrice implements SpongePrice<BigDecimal, Void> {

	private static EconomyService economy;
	private final BigDecimal price;

	public MonetaryPrice(double price) {
		this.price = new BigDecimal(price);
	}

	public static void setEconomy(EconomyService economy) {
		MonetaryPrice.economy = economy;
	}

	@Override
	public TextComponent getText() {
		return Component.text()
				.append(economy.defaultCurrency().format(this.getPrice()))
				.build();
	}

	@Override
	public Display<ItemStack> getDisplay() {
		return () -> ItemStack.builder()
				.itemType(ItemTypes.GOLD_INGOT)
				.add(Keys.CUSTOM_NAME, this.getText())
				.build();
	}

	@Override
	public BigDecimal getPrice() {
		return this.price;
	}

	@Override
	public boolean canPay(UUID payer) {
		return economy.findOrCreateAccount(payer).get()
				.balance(economy.defaultCurrency()).compareTo(this.price) >= 0;
	}

	@Override
	public void pay(UUID payer, @Nullable Object source, @NonNull AtomicBoolean marker) {
		economy.findOrCreateAccount(payer).get().withdraw(economy.defaultCurrency(), this.price);
		marker.set(true);
	}

	@Override
	public boolean reward(UUID recipient) {
		return economy.findOrCreateAccount(recipient).get()
				.deposit(economy.defaultCurrency(), this.price)
				.result()
				.equals(ResultType.SUCCESS);
	}

	@Override
	public Class<Void> getSourceType() {
		return Void.class;
	}

	@Override
	public long calculateFee(boolean listingType) {
		Config config = GTSPlugin.instance().configuration().main();
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

	public static class MonetaryPriceManager implements PriceManager<MonetaryPrice> {

		@Override
		public void process(PlatformPlayer target, EntryUI<?> source, BiConsumer<EntryUI<?>, Price<?, ?, ?>> callback) {
			Impactor.getInstance().getRegistry().get(ChatProcessor.class).register(target.uuid(), input -> {
				try {
					double value = Double.parseDouble(input);
					if(value > 0) {
						SpongePrice<BigDecimal, Void> price = new MonetaryPrice(value);
						Impactor.getInstance().getScheduler().executeSync(() -> {
							callback.accept(source, price);
						});
					}
				} catch (Exception ignored) {}

				Impactor.getInstance().getScheduler().executeSync(() -> {
					callback.accept(source, null);
				});
			});
			source.open(target);
		}

		@Override
		public Optional<PriceSelectorUI> getSelector(PlatformPlayer viewer, Price<?, ?, ?> price, Consumer<Object> callback) {
			return Optional.empty();
		}

		@Override
		public CommandGenerator.PriceGenerator<? extends Price<?, ?, ?>> getPriceCommandCreator() {
			return new MonetaryPriceCommandCreator();
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

	public static class MonetaryPriceCommandCreator implements CommandGenerator.PriceGenerator<MonetaryPrice> {

		@Override
		public String[] getAliases() {
			return new String[0];
		}

		@Override
		public MonetaryPrice create(UUID source, Queue<String> args, Context context) throws Exception {
			double value = this.require(args, Double::parseDouble);
			return new MonetaryPrice(value);
		}
	}

}
