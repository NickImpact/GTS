package me.nickimpact.gts.manager;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.configuration.Config;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.ListingManager;
import me.nickimpact.gts.api.listings.prices.Minable;
import me.nickimpact.gts.api.listings.prices.Price;
import me.nickimpact.gts.api.storage.IGtsStorage;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.discord.DiscordNotifier;
import me.nickimpact.gts.discord.Message;
import me.nickimpact.gts.events.SpigotListingEvent;
import me.nickimpact.gts.spigot.SpigotListing;
import me.xanium.gemseconomy.api.GemsEconomyAPI;
import me.xanium.gemseconomy.economy.Currency;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SpigotListingManager implements ListingManager<SpigotListing> {

	public static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy '-' hh:mm:ss a z");

	private List<Listing> listings = Lists.newArrayList();

	@Override
	public Optional<SpigotListing> getListingByID(UUID uuid) {
		return Optional.empty();
	}

	@Override
	public List<SpigotListing> getListings() {
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean addToMarket(UUID lister, SpigotListing listing) {
		Optional<Player> source = Optional.ofNullable(Bukkit.getServer().getPlayer(lister));
		Config config = GTS.getInstance().getConfiguration();
		if(config == null) {
			source.ifPresent(src -> src.sendMessage(this.parse("Internal error detected with plugin configuration, please inform an administrator!", true)));
			return false;
		}

		if(this.hasMaxListings(lister)) {
			int max = config.get(ConfigKeys.MAX_LISTINGS);
			source.ifPresent(src -> src.sendMessage(this.parse(String.format("Unfortunately, you can't deposit another listing. You have already listed the max amount of %d listings...", max), true)));
			return false;
		}

		SpigotListingEvent event = new SpigotListingEvent(source.orElse(null), listing);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) {
			source.ifPresent(src -> src.sendMessage(this.parse("Your listing was blocked by an administrative source...", true)));
			return false;
		}

		GemsEconomyAPI api = new GemsEconomyAPI();
		Currency currency = api.getCurrency("dollars");

		if(config.get(ConfigKeys.MIN_PRICING_ENABLED) && listing instanceof Minable) {
			Price price = listing.getPrice();
			Price min = ((Minable) listing.getEntry()).calcMinPrice();

			if(!((Minable) listing.getEntry()).isValid(price.getPrice())) {
				source.ifPresent(src -> src.sendMessage(this.parse(String.format("&7In order to sell your &a%s&7, you need to list it for the price of &a%s&7...", listing.getEntry().getName(), currency.format(min.getPrice())), true)));
				return false;
			}
		}

		double tax = listing.getPrice().calcTax();
		if(tax > 0) {
			if(api.getBalance(lister, currency) < tax) {
				source.ifPresent(src -> src.sendMessage(this.parse(String.format("&7Unable to afford the tax of &a%s &7for this listing...", currency.format(tax)), true)));
				return false;
			}
		}

		if(listing.getOwnerUUID() != null) {
			if(!source.isPresent()) {
				return false;
			}
			if(!listing.getEntry().doTakeAway(source.get())) {
				source.ifPresent(src -> src.sendMessage(this.parse("Your listing has been rejected...", true)));
				return false;
			}
		}

		source.ifPresent(src -> src.sendMessage(this.parse(String.format("Your &a%s &7has been added to the market!", listing.getName()), false)));

		// TODO - Add listing, and broadcast out to the many
		GTS.getInstance().getAPIService().getStorage().addListing(listing);
		this.listings.add(listing);

		List<String> details = Lists.newArrayList("");
		details.addAll(listing.getEntry().getDetails());
		String discord = this.asSingleWithNewlines(Lists.newArrayList(
				"Publisher: " + Bukkit.getServer().getOfflinePlayer(listing.getOwnerUUID()).getName(),
				"Publisher Identifier: " + listing.getOwnerUUID().toString(),
				"",
				"Published Item: " + listing.getName(),
				"Item Details: " + this.asSingleWithNewlines(details),
				"Requested Price: " + listing.getPrice().getText().toString(),
				"Expiration Time: " + sdf.format(listing.getExpiration())
		));

		DiscordNotifier notifier = new DiscordNotifier(GTS.getInstance());
		Message message = notifier.forgeMessage(config.get(ConfigKeys.DISCORD_SELL_LISTING), discord);
		notifier.sendMessage(message);

		return true;
	}

	@Override
	public boolean hasMaxListings(UUID lister) {
		int max = GTS.getInstance().getConfiguration().get(ConfigKeys.MAX_LISTINGS);
		return this.listings.stream().filter(listing -> listing.getOwnerUUID().equals(lister)).count() >= max;
	}

	@Override
	public void readStorage() {
		IGtsStorage storage = GTS.getInstance().getAPIService().getStorage();
		storage.getListings().thenAccept(x -> this.listings = x).exceptionally(throwable -> {
			GTS.getInstance().getPluginLogger().error("Unable to read in listings, a stacktrace is available below:");
			throwable.printStackTrace();
			return null;
		});
	}

	private String[] asArray(List<String> input) {
		return GTS.getInstance().getTextParsingUtils().convertFromList(input);
	}

	private String asSingleWithNewlines(List<String> list) {
		StringBuilder sb = new StringBuilder();
		if(list.size() > 0) {
			sb.append(list.get(0));
			for (int i = 1; i < list.size(); i++) {
				sb.append("\n").append(list.get(i));
			}
		}

		return sb.toString();
	}

	private String parse(String input, boolean error) {
		if(error) {
			return GTS.getInstance().getTextParsingUtils().error(input);
		} else {
			return GTS.getInstance().getTextParsingUtils().normal(input);
		}
	}
}
