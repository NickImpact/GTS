package me.nickimpact.gts.manager;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.configuration.Config;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.listings.ListingManager;
import me.nickimpact.gts.api.listings.prices.Minable;
import me.nickimpact.gts.api.listings.prices.Price;
import me.nickimpact.gts.api.storage.IGtsStorage;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.discord.DiscordNotifier;
import me.nickimpact.gts.discord.Message;
import me.nickimpact.gts.events.SpigotListingEvent;
import me.nickimpact.gts.spigot.MoneyPrice;
import me.nickimpact.gts.spigot.SpigotListing;
import me.nickimpact.gts.utils.DateTimeFormatUtils;
import me.nickimpact.gts.utils.MessageUtils;
import me.xanium.gemseconomy.api.GemsEconomyAPI;
import me.xanium.gemseconomy.economy.Currency;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class SpigotListingManager implements ListingManager<SpigotListing> {

	private List<SpigotListing> listings = Lists.newArrayList();

	@Override
	public Optional<SpigotListing> getListingByID(UUID uuid) {
		return this.getListings().stream().filter(listing -> listing.getUuid().equals(uuid)).findAny();
	}

	@Override
	public List<SpigotListing> getListings() {
		return this.listings;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean addToMarket(UUID lister, SpigotListing listing) {
		Optional<Player> source = Optional.ofNullable(Bukkit.getServer().getPlayer(lister));
		Config config = GTS.getInstance().getConfiguration();
		if(config == null) {
			source.ifPresent(src -> src.sendMessage(MessageUtils.parse("Internal error detected with plugin configuration, please inform an administrator!", true)));
			return false;
		}

		if(this.hasMaxListings(lister)) {
			int max = config.get(ConfigKeys.MAX_LISTINGS);
			source.ifPresent(src -> src.sendMessage(MessageUtils.parse(String.format("Unfortunately, you can't deposit another listing. You have already listed the max amount of %d listings...", max), true)));
			return false;
		}

		SpigotListingEvent event = new SpigotListingEvent(source.orElse(null), listing);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) {
			source.ifPresent(src -> src.sendMessage(MessageUtils.parse("Your listing was blocked by an administrative source...", true)));
			return false;
		}

		GemsEconomyAPI api = new GemsEconomyAPI();
		Currency currency = api.getCurrency("dollars");

		if(config.get(ConfigKeys.MIN_PRICING_ENABLED) && listing instanceof Minable) {
			Price price = listing.getPrice();
			Price min = ((Minable) listing.getEntry()).calcMinPrice();

			if(!((Minable) listing.getEntry()).isValid(price.getPrice())) {
				source.ifPresent(src -> src.sendMessage(MessageUtils.parse(String.format("&7In order to sell your &a%s&7, you need to list it for the price of &a%s&7...", listing.getEntry().getName(), currency.format(min.getPrice())), true)));
				return false;
			}
		}

		double tax = listing.getPrice().calcTax();
		if(tax > 0) {
			if(api.getBalance(lister, currency) < tax) {
				source.ifPresent(src -> src.sendMessage(MessageUtils.parse(String.format("&7Unable to afford the tax of &a%s &7for this listing...", currency.format(tax)), true)));
				return false;
			}
		}

		if(listing.getOwnerUUID() != null) {
			if(!source.isPresent()) {
				return false;
			}
			if(!listing.getEntry().doTakeAway(source.get())) {
				source.ifPresent(src -> src.sendMessage(MessageUtils.parse("Your listing has been rejected...", true)));
				return false;
			}
		}

		GTS.getInstance().getAPIService().getStorage().addListing(listing).exceptionally(throwable -> {
			throwable.printStackTrace();
			return false;
		});
		this.listings.add(listing);

		source.ifPresent(src -> src.sendMessage(MessageUtils.parse(String.format("Your &a%s &7has been added to the market!", listing.getName()), false)));

		for(Player player : Bukkit.getOnlinePlayers()) {
			if(source.isPresent() && !source.get().getUniqueId().equals(player.getUniqueId())) {
				player.sendMessage(MessageUtils.parse(
						String.format("%s &7has added a &a%s &7 to the GTS!", Bukkit.getPlayer(lister).getDisplayName(), listing.getEntry().getSpecsTemplate()),
						false
				));
			}
		}

		List<String> details = Lists.newArrayList("");
		details.addAll(listing.getEntry().getDetails());
		String discord = MessageUtils.asSingleWithNewlines(Lists.newArrayList(
				"Publisher: " + Bukkit.getServer().getOfflinePlayer(listing.getOwnerUUID()).getName(),
				"Publisher Identifier: " + listing.getOwnerUUID().toString(),
				"",
				"Published Item: " + listing.getName(),
				"Item Details: " + MessageUtils.asSingleWithNewlines(details),
				"",
				"Requested Price: " + listing.getPrice().getText().toString(),
				"Expiration Time: " + DateTimeFormatUtils.formatExpiration(listing)
		));

		DiscordNotifier notifier = new DiscordNotifier(GTS.getInstance());
		Message message = notifier.forgeMessage(config.get(ConfigKeys.DISCORD_NEW_LISTING), discord);
		notifier.sendMessage(message);

		return true;
	}

	@Override
	public boolean purchase(UUID buyer, SpigotListing listing) {
		Optional<Player> player = Optional.ofNullable(Bukkit.getPlayer(buyer));
		if(!this.listings.contains(listing)) {
			player.ifPresent(p -> p.sendMessage(MessageUtils.parse("Unfortunately, this listing has already been claimed...", true)));
			return false;
		}

		if(listing.hasExpired()) {
			player.ifPresent(p -> p.sendMessage(MessageUtils.parse("Unfortunately, that listing has since expired and can't be purchased...", true)));
			return false;
		}

		Price price = listing.getPrice();
		if(price.canPay(buyer)) {
			if(!listing.getEntry().giveEntry(Bukkit.getOfflinePlayer(buyer))) {
				// Entry responsible for error messages here
				return false;
			}

			price.pay(buyer);
			player.ifPresent(p -> p.sendMessage(MessageUtils.parse("You purchased a &3" + listing.getEntry().getName() + " &7for &e" + price.getText(), false)));

			price.reward(listing.getOwnerUUID());
			Optional<Player> owner = Optional.ofNullable(Bukkit.getPlayer(listing.getOwnerUUID()));
			owner.ifPresent(p -> p.sendMessage(MessageUtils.parse(String.format("Your &a%s &7was purchased by &e%s&7!", listing.getName(), Bukkit.getOfflinePlayer(buyer).getName()), false)));

			this.deleteListing(listing);

			List<String> details = Lists.newArrayList("");
			details.addAll(listing.getEntry().getDetails());
			String discord = MessageUtils.asSingleWithNewlines(Lists.newArrayList(
					"Publisher: " + Bukkit.getOfflinePlayer(listing.getOwnerUUID()).getName(),
					"Publisher Identifier: " + listing.getOwnerUUID().toString(),
					"",
					"Buyer: " + Bukkit.getOfflinePlayer(buyer).getName(),
					"Buyer Identifier: " + buyer.toString(),
					"",
					"Published Item: " + listing.getName(),
					"Item Details: " + MessageUtils.asSingleWithNewlines(details)
			));

			DiscordNotifier notifier = new DiscordNotifier(GTS.getInstance());
			Message message = notifier.forgeMessage(GTS.getInstance().getConfiguration().get(ConfigKeys.DISCORD_SELL_LISTING), discord);
			notifier.sendMessage(message);
			return true;
		}

		player.ifPresent(p -> p.sendMessage(MessageUtils.parse("You don't have enough to afford that listing...", true)));
		return false;
	}

	@Override
	public void deleteListing(SpigotListing listing) {
		this.listings.removeIf(l -> l.getUuid().equals(listing.getUuid()));
		GTS.getInstance().getAPIService().getStorage().deleteListing(listing.getUuid());
	}

	@Override
	public boolean hasMaxListings(UUID lister) {
		int max = GTS.getInstance().getConfiguration().get(ConfigKeys.MAX_LISTINGS);
		return this.listings.stream().filter(listing -> listing.getOwnerUUID().equals(lister)).count() >= max;
	}

	@Override
	public void readStorage() {
		IGtsStorage storage = GTS.getInstance().getAPIService().getStorage();

		try {
			storage.getListings()
					.thenApply(listings -> listings.stream().map(listing -> (SpigotListing) listing).collect(Collectors.toList()))
					.thenAccept(x -> {
						this.listings = x;
						GTS.getInstance().getPluginLogger().info("Successfully read in " + ChatColor.AQUA + this.listings.size() + " listings!");
					})
					.exceptionally(throwable -> {
						GTS.getInstance().getPluginLogger().error("Unable to read in listings, a stacktrace is available below:");
						throwable.printStackTrace();
						return null;
					}).get();
		} catch (InterruptedException | ExecutionException e) {
			GTS.getInstance().getPluginLogger().error("Unable to read in listings, a stacktrace is available below:");
			e.printStackTrace();
		}
	}
}
