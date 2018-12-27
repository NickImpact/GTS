package me.nickimpact.gts.internal;

import com.google.common.collect.ArrayListMultimap;
import lombok.Setter;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.GTSInfo;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.holders.EntryRegistry;
import me.nickimpact.gts.api.json.Registry;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.pricing.Price;
import me.nickimpact.gts.api.text.TokenService;
import me.nickimpact.gts.entries.prices.MoneyPrice;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.function.Function;

public class GtsServiceImpl implements GtsService {

	/** The registry that holds the typings for all entries */
	private EntryRegistry entries = new EntryRegistry();

	/** The registry that holds the typings for all prices */
	private Registry<Price> prices = new Registry<>();

	/** The internal Token Service handled by GTS */
	@Setter private TokenService tokens;

	private ArrayListMultimap<Class<?>, Function<?, MoneyPrice>> minPriceOptions = ArrayListMultimap.create();

	@Override
	public Registry getRegistry(RegistryType type) {
		return type == RegistryType.ENTRY ? entries.getRegistry() : prices;
	}

	@Override
	public EntryRegistry getEntryRegistry() {
		return this.entries;
	}

	@Override
	public void registerEntry(Class<? extends Entry> entry, EntryUI ui, ItemStack rep) {
		try {
			this.entries.getRegistry().register(entry);
			this.entries.getUis().put(entry, ui);
			this.entries.getReps().put(entry, rep);

			GTS.getInstance().getConsole().ifPresent(console -> {
				console.sendMessage(Text.of(GTSInfo.PREFIX, "Loaded element type: " + entry.getSimpleName()));
			});
		} catch (Exception e) {
			GTS.getInstance().getConsole().ifPresent(console -> {
				console.sendMessage(Text.of(
						GTSInfo.ERROR, e.getMessage()
				));
			});
		}

	}

	@Override
	public Collection<Class<? extends Entry>> getEntries() {
		return this.entries.getRegistry().getTypings().values();
	}

	@Override
	public TokenService getTokensService() {
		return this.tokens;
	}
}
