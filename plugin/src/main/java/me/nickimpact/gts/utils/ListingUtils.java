package me.nickimpact.gts.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.GTSInfo;
import me.nickimpact.gts.api.listings.entries.Minable;
import me.nickimpact.gts.configuration.ConfigKeys;
import me.nickimpact.gts.configuration.MsgConfigKeys;
import me.nickimpact.gts.api.events.ListEvent;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.discord.Message;
import me.nickimpact.gts.entries.prices.MoneyPrice;
import me.nickimpact.gts.internal.TextParsingUtils;
import me.nickimpact.gts.api.listings.pricing.Price;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tuple;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class ListingUtils {

	public static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy '-' hh:mm:ss a z");

	private static Text toOneText(Player player, List<String> text, Map<String, Function<CommandSource, Optional<Text>>> tokens, Map<String, Object> variables) {
		StringBuilder sb = new StringBuilder();
		for(String str : text) {
			sb.append(str).append("\n");
		}

		String base = sb.toString().substring(0, sb.length() - 2);
		return TextParsingUtils.parse(base, player, tokens, variables);
	}

    public static boolean addToMarket(Player player, Listing listing) {
	    if(ListingUtils.hasMax(player)) {
	    	player.sendMessages(TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.MAX_LISTINGS), player, null, null));
		    return false;
	    }

	    ListEvent listEvent = new ListEvent(player, listing, Sponge.getCauseStackManager().getCurrentCause());
	    Sponge.getEventManager().post(listEvent);

	    if(!listEvent.isCancelled()) {
		    Map<String, Object> variables = Maps.newHashMap();
		    variables.put("listing", listing);
		    variables.put("entry", listing.getEntry().getEntry());

		    if(GTS.getInstance().getConfig().get(ConfigKeys.MIN_PRICING_ENABLED) && listing.getEntry() instanceof Minable) {
		    	MoneyPrice price = listing.getEntry().getPrice();
				MoneyPrice min = ((Minable) listing.getEntry()).calcMinPrice();
				if(!((Minable) listing.getEntry()).isValid(price.getPrice())) {
					Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
					tokens.put("min_price", src -> Optional.of(min.getText()));
					player.sendMessages(TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.MIN_PRICE_ERROR), player, tokens, variables));
					return false;
				}
		    }

		    Optional<BigDecimal> tax = Optional.empty();
		    if(GTS.getInstance().getConfig().get(ConfigKeys.TAX_ENABLED)) {
				Tuple<BigDecimal, Boolean> t = listing.getEntry().getPrice().calcTax(player);
				if(!t.getSecond()) {
					Map<String, Function<CommandSource, Optional<Text>>> extras = Maps.newHashMap();
					extras.put("tax", src -> Optional.of(Text.of(t.getFirst())));

					player.sendMessages(TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.TAX_INVALID), player, extras, variables));
					return false;
				} else {
					tax = Optional.of(t.getFirst());
				}

				final BigDecimal ta = tax.orElse(BigDecimal.ZERO);
				Map<String, Function<CommandSource, Optional<Text>>> extras = Maps.newHashMap();
				extras.put("tax", src -> Optional.of(
						Text.of(GTS.getInstance().getEconomy().getDefaultCurrency().format(ta))));
				player.sendMessages(TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.TAX_APPLICATION), player, extras, null));
				GTS.getInstance().getEconomy().getOrCreateAccount(player.getUniqueId()).ifPresent(acc -> {
					acc.withdraw(GTS.getInstance().getEconomy().getDefaultCurrency(), ta, Sponge.getCauseStackManager().getCurrentCause());
				});

		    }

		    if(!listing.getOwnerName().equalsIgnoreCase("Console")) {
			    if (!listing.getEntry().doTakeAway(player)) {
				    // Refund applied tax
				    if (tax.isPresent()) {
					    UniqueAccount acc = GTS.getInstance().getEconomy().getOrCreateAccount(player.getUniqueId()).orElse(null);
					    if (acc != null) {
						    acc.deposit(GTS.getInstance().getEconomy().getDefaultCurrency(), tax.get(), Sponge.getCauseStackManager().getCurrentCause());
					    }

					    player.sendMessage(Text.of(GTSInfo.ERROR, TextColors.RED, "Your listing failed to be taken, so we have refunded the tax applied!"));
				    }
				    return false;
			    }
		    }

		    player.sendMessages(TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ADD_TEMPLATE), player, null, variables));

		    GTS.getInstance().getStorage().addListing(listing);
			GTS.getInstance().getListingsCache().add(listing);

			// Broadcast a message to everyone but the player who deposited the listing and the ignorers
		    Set<Player> players = Sponge.getServer().getOnlinePlayers().stream()
				    .filter(pl -> !pl.getUniqueId().equals(player.getUniqueId()))
				    .filter(pl -> !GTS.getInstance().getIgnorers().contains(pl.getUniqueId()))
				    .collect(Collectors.toSet());
			List<Text> broadcast = TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ADD_BROADCAST), player, null, variables);

		    for(Player pl : players) {
			    pl.sendMessages(broadcast);
		    }

			GTS.getInstance().getDiscordNotifier().ifPresent(notifier -> {
				Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
				tokens.put("gts_publisher", src -> Optional.of(Text.of(listing.getOwnerName())));
				tokens.put("gts_publisher_id", src -> Optional.of(Text.of(listing.getOwnerUUID())));
				tokens.put("gts_published_item", src -> Optional.of(Text.of(listing.getEntry().getName())));

				List<String> details = Lists.newArrayList("");
				details.addAll(listing.getEntry().getDetails());
				tokens.put("gts_published_item_details", src -> Optional.of(Text.of(StringUtils.stringListToString(details))));
				tokens.put("gts_publishing_price", src -> Optional.of(Text.of(listing.getEntry().getPrice().getText().toPlain())));
				tokens.put("gts_publishing_expiration", src -> Optional.of(Text.of(sdf.format(listing.getExpiration()))));

				Message message = notifier.forgeMessage(GTS.getInstance().getConfig().get(ConfigKeys.DISCORD_SELL_LISTING), StringUtils.textListToString(TextParsingUtils.fetchAndParseMsgs(
						null, MsgConfigKeys.DISCORD_PUBLISH_TEMPLATE, tokens, null)
				));
				notifier.sendMessage(message);
			});

		    GTS.getInstance().getUpdater().sendUpdate();

		    return true;
	    }

	    return false;
    }

    public static void purchase(Player player, Listing listing) {
	    Map<String, Object> variables = Maps.newHashMap();
	    variables.put("listing", listing);
	    variables.put("entry", listing.getEntry().getEntry());

		if(!GTS.getInstance().getListingsCache().contains(listing)) {
			player.sendMessages(TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ALREADY_CLAIMED), player, null, variables));
			return;
		}

		if(listing.hasExpired()) {
			player.sendMessage(Text.of(GTSInfo.ERROR, TextColors.GRAY, "That listing has expired..."));
			return;
		}

		Price price = listing.getEntry().getPrice();
		if(price.canPay(player)) {
			if(!listing.getEntry().giveEntry(player)) {
				return;
			}

			price.pay(player);
			player.sendMessages(TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.PURCHASE_PAY), player, null, variables));

			Player receiver;
			price.reward(listing.getOwnerUUID());
			if ((receiver = Sponge.getServer().getPlayer(listing.getOwnerUUID()).orElse(null)) != null) {
				receiver.sendMessages(TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.PURCHASE_RECEIVE), player, null, variables));
			}

			deleteEntry(listing);

			GTS.getInstance().getDiscordNotifier().ifPresent(notifier -> {
				Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
				tokens.put("gts_seller", src -> Optional.of(Text.of(listing.getOwnerName())));
				tokens.put("gts_seller_id", src -> Optional.of(Text.of(listing.getOwnerUUID())));
				tokens.put("gts_buyer", src -> Optional.of(Text.of(player.getName())));
				tokens.put("gts_buyer_id", src -> Optional.of(Text.of(player.getUniqueId())));

				tokens.put("gts_published_item", src -> Optional.of(Text.of(listing.getEntry().getName())));

				List<String> details = Lists.newArrayList("");
				details.addAll(listing.getEntry().getDetails());
				tokens.put("gts_published_item_details", src -> Optional.of(Text.of(StringUtils.stringListToString(details))));
				tokens.put("gts_publishing_price", src -> Optional.of(Text.of(listing.getEntry().getPrice().getText().toPlain())));
				tokens.put("gts_publishing_expiration", src -> Optional.of(Text.of(sdf.format(listing.getExpiration()))));

				Message message = notifier.forgeMessage(GTS.getInstance().getConfig().get(ConfigKeys.DISCORD_NEW_LISTING), StringUtils.textListToString(TextParsingUtils.fetchAndParseMsgs(
						null, MsgConfigKeys.DISCORD_PURCHASE_TEMPLATE, tokens, null)
				));
				notifier.sendMessage(message);
			});

			GTS.getInstance().getUpdater().sendUpdate();
		} else {
			player.sendMessages(TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.NOT_ENOUGH_FUNDS), player, null, variables));
		}

    }

    private static boolean hasMax(User user) {
        return hasMax(user.getUniqueId());
    }

    private static boolean hasMax(UUID uuid) {
        int count = GTS.getInstance().getListingsCache().stream().filter(listing -> listing.getOwnerUUID().equals(uuid)).collect(Collectors.toList()).size();
        return count >= GTS.getInstance().getConfig().get(ConfigKeys.MAX_LISTINGS);
    }

	public static void deleteEntry(Listing entry) {
    	GTS.getInstance().getListingsCache().remove(entry);
    	GTS.getInstance().getStorage().removeListing(entry.getUuid());
	}
}
