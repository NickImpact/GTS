package me.nickimpact.gts.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.impactor.api.configuration.Config;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.enums.CommandResults;
import me.nickimpact.gts.api.listings.ListingManager;
import me.nickimpact.gts.api.listings.SoldListing;
import me.nickimpact.gts.api.listings.prices.Minable;
import me.nickimpact.gts.api.listings.prices.Price;
import me.nickimpact.gts.api.plugin.PluginInstance;
import me.nickimpact.gts.api.storage.IGtsStorage;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.discord.DiscordNotifier;
import me.nickimpact.gts.discord.Message;
import me.nickimpact.gts.events.SpongeListingEvent;
import me.nickimpact.gts.sponge.MoneyPrice;
import me.nickimpact.gts.sponge.SpongeListing;
import me.nickimpact.gts.sponge.SpongePlugin;
import me.nickimpact.gts.sponge.TextParsingUtils;
import me.nickimpact.gts.sponge.utils.MessageUtils;
import me.nickimpact.gts.utils.DateTimeFormatUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpongeListingManager implements ListingManager<SpongeListing> {

	private List<SpongeListing> listings = Lists.newArrayList();
	private List<UUID> ignorers = Lists.newArrayList();

	@Override
	public Optional<SpongeListing> getListingByID(UUID uuid) {
		return this.getListings().stream().filter(listing -> listing.getUuid().equals(uuid)).findAny();
	}

	@Override
	public List<SpongeListing> getListings() {
		return this.listings;
	}

	@Override
	public List<UUID> getIgnorers() {
		return this.ignorers;
	}

	@Override
	public boolean addToMarket(UUID lister, SpongeListing listing) {
		Optional<Player> source = Sponge.getServer().getPlayer(lister);
		Config config = GTS.getInstance().getConfiguration();
		Config msgConfig = GTS.getInstance().getMsgConfig();
		TextParsingUtils parser = GTS.getInstance().getTextParsingUtils();
		if(config == null || !source.isPresent()) {
			source.ifPresent(src -> src.sendMessage(parser.parse(msgConfig.get(MsgConfigKeys.PLUGIN_ERROR), null, null, null)));
			return false;
		}

		Map<String, Object> variables = Maps.newHashMap();
		variables.put("listing", listing);
		variables.put("entry", listing.getEntry().getEntry());

		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();

		if(this.hasMaxListings(lister)) {
			source.ifPresent(src -> src.sendMessages(parser.parse(
					msgConfig.get(MsgConfigKeys.MAX_LISTINGS), source.get(), null, null)
			));
			return false;
		}

		SpongeListingEvent event = new SpongeListingEvent(source.orElse(null), listing);
		Sponge.getEventManager().post(event);
		if(event.isCancelled()) {
			source.ifPresent(src -> src.sendMessage(parser.parse(msgConfig.get(MsgConfigKeys.LISTING_EVENT_CANCELLED), source.get(), null, null)));
			return false;
		}

		if(config.get(ConfigKeys.MIN_PRICING_ENABLED) && listing.getEntry() instanceof Minable) {
			Price price = listing.getPrice();
			Price min = ((Minable) listing.getEntry()).calcMinPrice();
			tokens.put("min_price", src -> Optional.of((Text) min.getText()));

			if(!((Minable) listing.getEntry()).isValid(price.getPrice())) {
				source.ifPresent(src -> src.sendMessages(parser.parse(msgConfig.get(MsgConfigKeys.MIN_PRICE_ERROR), source.get(), tokens, variables)));
				return false;
			}
		}

		if(listing.getPrice().getPrice() <= 0) {
			source.ifPresent(src -> src.sendMessage(parser.fetchAndParseMsg(src, config, MsgConfigKeys.PRICE_NOT_POSITIVE, null, null)));
			return false;
		}

		if(listing.getPrice().getPrice() > config.get(ConfigKeys.MAX_MONEY_PRICE)) {
			source.ifPresent(src -> src.sendMessage(parser.fetchAndParseMsg(src, config, MsgConfigKeys.PRICE_MAX_INVALID, null, null)));
			return false;
		}

		EconomyService economy = GTS.getInstance().getEconomy();
		double tax = listing.getPrice().calcTax();
		if(config.get(ConfigKeys.TAX_ENABLED)) {
			tokens.put("tax", src -> Optional.of(economy.getDefaultCurrency().format(new BigDecimal(tax), 2)));
			if (tax > 0) {
				if (economy.getOrCreateAccount(lister).get().getBalance(economy.getDefaultCurrency()).doubleValue() < tax) {
					tokens.put("tax", src -> Optional.of(Text.of(String.format("%.2f", tax))));
					source.ifPresent(src -> src.sendMessages(parser.parse(msgConfig.get(MsgConfigKeys.TAX_INVALID), source.get(), tokens, variables)));
					return false;
				}

				economy.getOrCreateAccount(lister).get().withdraw(economy.getDefaultCurrency(), new BigDecimal(tax), Sponge.getCauseStackManager().getCurrentCause());
			}
		}

		if(listing.getOwnerUUID() != null) {
			if(!listing.getEntry().doTakeAway(source.get())) {
				source.ifPresent(src -> src.sendMessages(parser.parse(msgConfig.get(MsgConfigKeys.UNABLE_TO_TAKE_LISTING), source.get(), null, null)));
				if(config.get(ConfigKeys.TAX_ENABLED)) {
					economy.getOrCreateAccount(lister).get().deposit(economy.getDefaultCurrency(), new BigDecimal(tax), Sponge.getCauseStackManager().getCurrentCause());
				}
				return false;
			}
		}

		GTS.getInstance().getAPIService().getStorage().addListing(listing).exceptionally(throwable -> {
			throwable.printStackTrace();
			return false;
		});
		this.listings.add(listing);

		source.ifPresent(src -> src.sendMessages(parser.parse(msgConfig.get(MsgConfigKeys.ADD_TEMPLATE), source.get(), tokens, variables)));

		if(config.get(ConfigKeys.TAX_ENABLED)) {
			source.ifPresent(src -> src.sendMessages(parser.parse(msgConfig.get(MsgConfigKeys.TAX_APPLICATION), source.get(), tokens, variables)));
		}

		for(Player player : Sponge.getServer().getOnlinePlayers()) {
			if(!source.get().getUniqueId().equals(player.getUniqueId()) && !ignorers.contains(player.getUniqueId())) {
				player.sendMessages(parser.parse(msgConfig.get(MsgConfigKeys.ADD_BROADCAST), source.get(), tokens, variables));
			}
		}

		List<String> details = Lists.newArrayList("");
		details.addAll(listing.getEntry().getDetails());

		tokens.put("gts_publisher", src -> Optional.of(Text.of(Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(lister).map(User::getName).orElse("???"))));
		tokens.put("gts_publisher_id", src -> Optional.of(Text.of(lister.toString())));
		tokens.put("gts_published_item", src -> Optional.of(Text.of(listing.getEntry().getName())));
		tokens.put("gts_published_item_details", src -> Optional.of(Text.of(MessageUtils.asSingleWithNewlines(details))));
		tokens.put("gts_publishing_price", src -> Optional.of(((MoneyPrice) listing.getPrice()).getText()));
		tokens.put("gts_publishing_expiration", src -> Optional.of(Text.of(DateTimeFormatUtils.formatExpiration(listing))));

		String discord = MessageUtils.asSingleWithNewlines(GTS.getInstance().getTextParsingUtils().fetchAndParseMsgs(
				null, GTS.getInstance().getMsgConfig(), MsgConfigKeys.DISCORD_PUBLISH_TEMPLATE, tokens, variables
		).stream().map(Text::toPlain).collect(Collectors.toList()));

		DiscordNotifier notifier = new DiscordNotifier(GTS.getInstance());
		Message message = notifier.forgeMessage(GTS.getInstance().getConfiguration().get(ConfigKeys.DISCORD_NEW_LISTING), discord);
		notifier.sendMessage(message);

		return true;
	}

	@Override
	public boolean purchase(UUID buyer, SpongeListing listing) {
		Config msgConfig = GTS.getInstance().getMsgConfig();
		TextParsingUtils parser = GTS.getInstance().getTextParsingUtils();

		Map<String, Object> variables = Maps.newHashMap();
		variables.put("listing", listing);
		variables.put("entry", listing.getEntry().getEntry());

		Optional<Player> player = Sponge.getServer().getPlayer(buyer);
		if(!this.listings.contains(listing)) {
			player.ifPresent(p -> p.sendMessages(parser.fetchAndParseMsgs(p, msgConfig, MsgConfigKeys.ALREADY_CLAIMED, null, null)));
			return false;
		}

		if(listing.hasExpired()) {
			player.ifPresent(p -> p.sendMessages(parser.fetchAndParseMsgs(p, msgConfig, MsgConfigKeys.EXPIRED, null, null)));
			return false;
		}

		Price price = listing.getPrice();
		if(price.canPay(buyer)) {
			if(!listing.getEntry().giveEntry(Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(buyer).get())) {
				// Entry responsible for error messages here
				return false;
			}

			price.pay(buyer);
			player.ifPresent(p -> p.sendMessages(parser.fetchAndParseMsgs(p, msgConfig, MsgConfigKeys.PURCHASE_PAY, null, variables)));

			price.reward(listing.getOwnerUUID());
			Optional<Player> owner = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(listing.getOwnerUUID()).map(User::getPlayer).get();
			owner.ifPresent(p -> p.sendMessages(parser.fetchAndParseMsgs(player.get(), msgConfig, MsgConfigKeys.PURCHASE_RECEIVE, null, variables)));

			if(!owner.isPresent()) {
				SoldListing sl = new SoldListing(listing.getEntry().getName(), listing.getPrice().getPrice());
				GTS.getInstance().getAPIService().getStorage().addToSoldListings(listing.getOwnerUUID(), sl);
			}

			this.deleteListing(listing);

			List<String> details = Lists.newArrayList("");
			details.addAll(listing.getEntry().getDetails());

			EconomyService economy = ((SpongePlugin) PluginInstance.getInstance()).getEconomy();

			Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
			tokens.put("gts_seller", src -> Optional.of(Text.of(Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(listing.getOwnerUUID()).map(User::getName).orElse(""))));
			tokens.put("gts_seller_id", src -> Optional.of(Text.of(listing.getOwnerUUID().toString())));
			tokens.put("gts_published_item", src -> Optional.of(Text.of(listing.getEntry().getName())));
			tokens.put("gts_published_item_details", src -> Optional.of(Text.of(MessageUtils.asSingleWithNewlines(details))));
			tokens.put("gts_publishing_price", src -> Optional.of(economy.getDefaultCurrency().format(new BigDecimal(listing.getPrice().getPrice()))));
			tokens.put("gts_buyer", src -> Optional.of(Text.of(player.get().getName())));
			tokens.put("gts_buyer_id", src -> Optional.of(Text.of(buyer.toString())));

			String discord = MessageUtils.asSingleWithNewlines(GTS.getInstance().getTextParsingUtils().fetchAndParseMsgs(
					null, GTS.getInstance().getMsgConfig(), MsgConfigKeys.DISCORD_PURCHASE_TEMPLATE, tokens, variables
			).stream().map(Text::toPlain).collect(Collectors.toList()));

			DiscordNotifier notifier = new DiscordNotifier(GTS.getInstance());
			Message message = notifier.forgeMessage(GTS.getInstance().getConfiguration().get(ConfigKeys.DISCORD_SELL_LISTING), discord);
			notifier.sendMessage(message);
			return true;
		}

		player.ifPresent(p -> p.sendMessages(parser.fetchAndParseMsgs(player.get(), msgConfig, MsgConfigKeys.NOT_ENOUGH_FUNDS, null, variables)));
		return false;
	}

	@Override
	public void deleteListing(SpongeListing listing) {
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
					.thenApply(listings -> listings.stream().map(listing -> (SpongeListing) listing).collect(Collectors.toList()))
					.thenAccept(x -> {
						this.listings = x;
						GTS.getInstance().getPluginLogger().info("Successfully read in &b" + this.listings.size() + " listings!");
					})
					.exceptionally(throwable -> {
						GTS.getInstance().getPluginLogger().error("Unable to read in listings, a stacktrace is available below:");
						throwable.printStackTrace();
						return null;
					}).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
}
