package me.nickimpact.gts.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.impactor.api.configuration.Config;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.listings.ListingManager;
import me.nickimpact.gts.api.listings.prices.Minable;
import me.nickimpact.gts.api.listings.prices.Price;
import me.nickimpact.gts.api.storage.IGtsStorage;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.discord.DiscordNotifier;
import me.nickimpact.gts.discord.Message;
import me.nickimpact.gts.events.SpigotListingEvent;
import me.nickimpact.gts.spigot.SpigotListing;
import me.nickimpact.gts.spigot.tokens.TokenService;
import me.nickimpact.gts.utils.DateTimeFormatUtils;
import me.nickimpact.gts.spigot.MessageUtils;
import me.xanium.gemseconomy.api.GemsEconomyAPI;
import me.xanium.gemseconomy.economy.Currency;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpigotListingManager implements ListingManager<SpigotListing> {

	private List<SpigotListing> listings = Lists.newArrayList();
	private List<UUID> ignorers = Lists.newArrayList();

	@Override
	public Optional<SpigotListing> getListingByID(UUID uuid) {
		return this.getListings().stream().filter(listing -> listing.getUuid().equals(uuid)).findAny();
	}

	@Override
	public List<SpigotListing> getListings() {
		return this.listings;
	}

	@Override
	public List<UUID> getIgnorers() {
		return this.ignorers;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean addToMarket(UUID lister, SpigotListing listing) {
		Optional<Player> source = Optional.ofNullable(Bukkit.getServer().getPlayer(lister));
		Config config = GTS.getInstance().getConfiguration();

		TokenService service = GTS.getInstance().getTokenService();

		if(this.hasMaxListings(lister)) {
			source.ifPresent(src -> this.sendMessages(src, service.process(MsgConfigKeys.MAX_LISTINGS, src, null, null)));
			return false;
		}

		SpigotListingEvent event = new SpigotListingEvent(source.orElse(null), listing);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) {
			source.ifPresent(src -> src.sendMessage(MessageUtils.parse("Your listing was blocked by an administrative source...", true)));
			return false;
		}

		Economy economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();

		Map<String, Function<CommandSender, Optional<String>>> tokens = Maps.newHashMap();

		Map<String, Object> variables = Maps.newHashMap();
		variables.put("listing", listing);
		variables.put("entry", listing.getEntry().getEntry());

		if(config.get(ConfigKeys.MIN_PRICING_ENABLED) && listing instanceof Minable) {
			Price price = listing.getPrice();
			Price min = ((Minable) listing.getEntry()).calcMinPrice();

			tokens.put("min_price", src -> Optional.of(economy.format(min.getPrice())));

			if(!((Minable) listing.getEntry()).isValid(price.getPrice())) {
				source.ifPresent(src -> this.sendMessages(src, service.process(MsgConfigKeys.MIN_PRICE_ERROR, src, tokens, variables)));
				return false;
			}
		}

		double tax = listing.getPrice().calcTax();
		if(tax > 0) {
			if(economy.getBalance(Bukkit.getOfflinePlayer(lister)) < tax) {
				tokens.put("tax", src -> Optional.of(Bukkit.getServicesManager().getRegistration(Economy.class).getProvider().format(tax)));
				source.ifPresent(src -> this.sendMessages(src, service.process(MsgConfigKeys.TAX_INVALID, src, tokens, variables)));
				return false;
			}
		}

		if(listing.getOwnerUUID() != null) {
			if(!source.isPresent()) {
				return false;
			}
			if(!listing.getEntry().doTakeAway(source.get())) {
				source.ifPresent(src -> src.sendMessage(service.process(MsgConfigKeys.UNABLE_TO_TAKE_LISTING, src, tokens, variables)));
				return false;
			}
		}

		GTS.getInstance().getAPIService().getStorage().addListing(listing).exceptionally(throwable -> {
			throwable.printStackTrace();
			return false;
		});
		this.listings.add(listing);

		source.ifPresent(src -> this.sendMessages(src, service.process(MsgConfigKeys.ADD_TEMPLATE, src, tokens, variables)));

		for(Player player : Bukkit.getOnlinePlayers()) {
			if(source.isPresent() && !source.get().getUniqueId().equals(player.getUniqueId())) {
				player.sendMessage(service.process(MsgConfigKeys.ADD_BROADCAST, player, tokens, variables).toArray(new String[]{}));
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
		TokenService service = GTS.getInstance().getTokenService();
		Map<String, Object> variables = Maps.newHashMap();
		variables.put("listing", listing);
		variables.put("entry", listing.getEntry().getEntry());

		Optional<Player> player = Optional.ofNullable(Bukkit.getPlayer(buyer));
		if(!this.listings.contains(listing)) {
			player.ifPresent(p -> p.sendMessage(service.process(MsgConfigKeys.ALREADY_CLAIMED, p, null, null).toArray(new String[]{})));
			return false;
		}

		if(listing.hasExpired()) {
			player.ifPresent(p -> p.sendMessage(service.process(MsgConfigKeys.EXPIRED, p, null, null).toArray(new String[]{})));
			return false;
		}

		Price price = listing.getPrice();
		if(price.canPay(buyer)) {
			if(!listing.getEntry().giveEntry(Bukkit.getOfflinePlayer(buyer))) {
				// Entry responsible for error messages here
				return false;
			}

			price.pay(buyer);
			player.ifPresent(p -> p.sendMessage(service.process(MsgConfigKeys.PURCHASE_PAY, p, null, variables).toArray(new String[]{})));

			price.reward(listing.getOwnerUUID());
			Optional<Player> owner = Optional.ofNullable(Bukkit.getPlayer(listing.getOwnerUUID()));
			owner.ifPresent(p -> p.sendMessage(service.process(MsgConfigKeys.PURCHASE_RECEIVE, player.get(), null, variables).toArray(new String[]{})));

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

		player.ifPresent(p -> p.sendMessage(service.process(MsgConfigKeys.NOT_ENOUGH_FUNDS, p, null, variables).toArray(new String[]{})));
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

	private void sendMessages(CommandSender source, List<String> messages) {
		for(String message : messages) {
			source.sendMessage(message);
		}
	}
}
