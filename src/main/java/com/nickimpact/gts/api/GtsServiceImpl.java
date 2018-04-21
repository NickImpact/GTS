package com.nickimpact.gts.api;

import com.google.common.collect.Lists;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.GTSInfo;
import com.nickimpact.gts.api.exceptions.NotMinableException;
import com.nickimpact.gts.api.json.Registry;
import com.nickimpact.gts.api.listings.entries.Entry;
import com.nickimpact.gts.api.listings.entries.MinPriceMapping;
import com.nickimpact.gts.api.listings.entries.Minable;
import com.nickimpact.gts.api.listings.pricing.Price;
import com.nickimpact.gts.api.text.Token;
import com.nickimpact.gts.api.text.TokenService;
import com.nickimpact.gts.entries.items.ItemEntry;
import com.nickimpact.gts.entries.prices.ItemPrice;
import com.nickimpact.gts.entries.prices.MoneyPrice;
import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class GtsServiceImpl implements GtsService {

	/** The registry that holds the typings for all entries */
	private Registry<Entry> entries = new Registry<>();

	/** The registry that holds the typings for all prices */
	private Registry<Price> prices = new Registry<>();

	private MinPriceMapping minPriceMapping = new MinPriceMapping();

	private TokenService tokens;

	@Override
	public Registry getRegistry(RegistryType type) {
		return type == RegistryType.ENTRY ? entries : prices;
	}

	@Override
	public void registerEntry(Class<? extends Entry> entry) {
		this.registerEntries(Lists.newArrayList(entry));
	}

	@Override
	public void registerEntries(Collection<Class<? extends Entry>> entries) {
		entries.forEach(entry -> {
			try {
				this.entries.register(entry);

				if(!entry.equals(ItemEntry.class)) {
					GTS.getInstance().getConsole().ifPresent(console -> {
						console.sendMessage(Text.of(GTSInfo.PREFIX, "Loaded entry type: " + entry.getSimpleName()));
					});
				}
			} catch (Exception e) {
				GTS.getInstance().getConsole().ifPresent(console -> {
					console.sendMessage(Text.of(
							GTSInfo.ERROR, e.getMessage()
					));
				});
			}
		});
	}

	@Override
	public Collection<Class<? extends Entry>> getEntries() {
		return this.entries.getTypings().values();
	}

	@Override
	public void registerPrice(Class<? extends Price> price) {
		this.registerPrices(Lists.newArrayList(price));
	}

	@Override
	public void registerPrices(Collection<Class<? extends Price>> prices) {
		prices.forEach(price -> {
			try {
				this.prices.register(price);

				if(!price.equals(ItemPrice.class) || !price.equals(MoneyPrice.class)) {
					GTS.getInstance().getConsole().ifPresent(console -> {
						console.sendMessage(Text.of(GTSInfo.PREFIX, "Loaded price type: " + price.getSimpleName()));
					});
				}
			} catch (Exception e) {
				GTS.getInstance().getConsole().ifPresent(console -> {
					console.sendMessage(Text.of(
							GTSInfo.ERROR, e.getMessage()
					));
				});
			}
		});
	}

	@Override
	public Collection<Class<? extends Price>> getPrices() {
		return this.prices.getTypings().values();
	}

	@Override
	public void addMinPriceOption(Class<? extends Entry> clazz, Function<EntryElement, Price> function) throws NotMinableException {
		this.getEntries().stream().filter(entry -> entry.equals(clazz)).forEach(
				entry -> {
					if(!entry.isAssignableFrom(Minable.class)) {
						throw new NotMinableException(String.format("%s does not support min prices...", clazz.getSimpleName()));
					}
					this.minPriceMapping.getMapping().put(entry, function);
				}
		);
	}

	@Override
	public Optional<List<Function<EntryElement, Price>>> getMinPriceOptions(Class<? extends Entry> clazz) {
		return Optional.ofNullable(this.minPriceMapping.getMapping().get(clazz));
	}

	@Override
	public void registerTokenService() {
		this.tokens = new TokenService();
	}

	@Override
	public TokenService getTokensService() throws Exception {
		if(!Sponge.getServiceManager().isRegistered(NucleusMessageTokenService.class)) {
			throw new Exception("Token Service has not yet been initialized...");
		}

		return this.tokens;
	}

	@Override
	public void addTokens(Token... tokens) {
		for(Token token : tokens) {
			try {
				this.addToken(token);
			} catch (Exception e) {
				GTS.getInstance().getConsole().ifPresent(console -> console.sendMessage(Text.of(
						GTSInfo.ERROR, e.getMessage()
				)));
			}
		}
	}

	private void addToken(Token token) throws Exception {
		this.getTokensService().register(token.getKey(), token.getTranslator());
	}
}
