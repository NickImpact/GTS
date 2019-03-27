package me.nickimpact.gts.internal;

import com.google.common.collect.ArrayListMultimap;
import lombok.Setter;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.GTSInfo;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.holders.EntryRegistry;
import me.nickimpact.gts.api.json.Registry;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.pricing.Price;
import me.nickimpact.gts.api.text.TokenService;
import me.nickimpact.gts.entries.prices.MoneyPrice;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
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
	public void registerEntry(List<String> identifier, Class<? extends Entry> entry, EntryUI ui, String rep, BiFunction<CommandSource, String[], CommandResult> cmd) {
		try {
			this.entries.getRegistry().register(entry);
			this.entries.getClassifications().add(new EntryClassification(entry, identifier, rep, ui, cmd));

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
