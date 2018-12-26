package me.nickimpact.gts.utils;

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
import me.nickimpact.gts.logs.Log;
import me.nickimpact.gts.logs.LogAction;
import me.nickimpact.gts.api.listings.pricing.Price;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tuple;

import java.math.BigDecimal;
import java.time.Instant;
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

	private static Text toOneText(Player player, List<String> text, Map<String, Function<CommandSource, Optional<Text>>> tokens, Map<String, Object> variables) {
		StringBuilder sb = new StringBuilder();
		for(String str : text) {
			sb.append(str).append("\n");
		}

		String base = sb.toString().substring(0, sb.length() - 2);
		return TextParsingUtils.parse(base, player, tokens, variables);
	}

    public static void addToMarket(Player player, Listing listing) {
	    if(ListingUtils.hasMax(player)) {
	    	player.sendMessages(TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.MAX_LISTINGS), player, null, null));
		    return;
	    }

	    ListEvent listEvent = new ListEvent(player, listing, Sponge.getCauseStackManager().getCurrentCause());
	    Sponge.getEventManager().post(listEvent);

	    if(!listEvent.isCancelled()) {
		    Map<String, Object> variables = Maps.newHashMap();
		    variables.put("listing", listing);

		    if(GTS.getInstance().getConfig().get(ConfigKeys.MIN_PRICING_ENABLED) && listing.getEntry() instanceof Minable) {
		    	MoneyPrice price = listing.getEntry().getPrice();
				MoneyPrice min = ((Minable) listing.getEntry()).calcMinPrice();
				if(!((Minable) listing.getEntry()).isValid(price.getPrice())) {
					Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
					tokens.put("min_price", src -> Optional.of(min.getText()));
					player.sendMessages(TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.MIN_PRICE_ERROR), player, tokens, variables));
					return;
				}
		    }

		    Optional<BigDecimal> tax = Optional.empty();
		    if(GTS.getInstance().getConfig().get(ConfigKeys.TAX_ENABLED)) {
				Tuple<BigDecimal, Boolean> t = listing.getEntry().getPrice().calcTax(player);
				if(!t.getSecond()) {
					Map<String, Function<CommandSource, Optional<Text>>> extras = Maps.newHashMap();
					extras.put("tax", src -> Optional.of(Text.of(t.getFirst())));

					player.sendMessages(TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.TAX_INVALID), player, extras, variables));
					return;
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

		    if(!listing.getEntry().doTakeAway(player)) {
			    // Refund applied tax
			    if(tax.isPresent()) {
					UniqueAccount acc = GTS.getInstance().getEconomy().getOrCreateAccount(player.getUniqueId()).orElse(null);
					if (acc != null) {
						acc.deposit(GTS.getInstance().getEconomy().getDefaultCurrency(), tax.get(), Sponge.getCauseStackManager().getCurrentCause());
					}

					player.sendMessage(Text.of(GTSInfo.ERROR, TextColors.RED, "Your listing failed to be taken, so we have refunded the tax applied!"));
			    }
			    return;
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

		    final Text b = toOneText(player, GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ADD_BROADCAST), null, variables);
			GTS.getInstance().getDiscordNotifier().ifPresent(notifier -> {
				Message message = notifier.forgeMessage(GTS.getInstance().getConfig().get(ConfigKeys.DISCORD_NEW_LISTING), b.toPlain());
				notifier.sendMessage(message);
			});

		    Log add = Log.builder()
				    .action(LogAction.Addition)
				    .source(player.getUniqueId())
				    .hover(Log.forgeTemplate(player, listing, LogAction.Addition))
				    .build();
		    GTS.getInstance().getStorage().addLog(add);
		    GTS.getInstance().getUpdater().sendUpdate();
	    }
    }

    public static void purchase(Player player, Listing listing) {
	    Map<String, Object> variables = Maps.newHashMap();
	    variables.put("listing", listing);

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

			final String b = TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.DISCORD_PURCHASE), player, null, variables).toPlain();
			GTS.getInstance().getDiscordNotifier().ifPresent(notifier -> {
				Message message = notifier.forgeMessage(GTS.getInstance().getConfig().get(ConfigKeys.DISCORD_SELL_LISTING), b);
				notifier.sendMessage(message);
			});

			Log buyer = Log.builder()
					.action(LogAction.Purchase)
					.source(player.getUniqueId())
					.hover(Log.forgeTemplate(player, listing, LogAction.Purchase))
					.build();
			GTS.getInstance().getStorage().addLog(buyer);

			Log seller = Log.builder()
					.action(LogAction.Sell)
					.source(listing.getOwnerUUID())
					.hover(Log.forgeTemplate(player, listing, LogAction.Sell))
					.build();
			GTS.getInstance().getStorage().addLog(seller);

			GTS.getInstance().getUpdater().sendUpdate();
		} else {
			player.sendMessages(TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.NOT_ENOUGH_FUNDS), player, null, variables));
		}

    }

    public static void bid(Player player, Listing listing) {
	    Map<String, Object> variables = Maps.newHashMap();
	    variables.put("listing", listing);

		if(listing.getAucData() != null) {
			if(listing.getAucData().getHighBidder() != null && listing.getAucData().getHighBidder().equals(player.getUniqueId())) {
				player.sendMessages(TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.AUCTION_IS_HIGH_BIDDER), player, null, variables));
			} else {
				MoneyPrice newPrice = listing.getEntry().getPrice().calculate(listing.getAucData().getIncrement());
				if(!newPrice.canPay(player)) {
					player.sendMessage(Text.of(GTSInfo.ERROR, "Your balance is too low to bid..."));
					return;
				}

				UUID oldHigh = listing.getAucData().getHighBidder();
				Sponge.getServer().getPlayer(oldHigh).ifPresent(p -> {
					Text message = Text.of(GTSInfo.PREFIX, TextColors.GRAY, TextActions.executeCallback(src -> bid((Player) src, listing)), TextActions.showText(Text.of(TextColors.GRAY, "Click to bid!")), "You've been ", TextColors.RED, "outbid", TextColors.GRAY, "... Click here to bid once more!");
					p.sendMessages(message);
				});

				listing.getAucData().setHighBidder(player.getUniqueId());
				listing.getAucData().setHbNameString(player.getName());
				if(listing.getExpiration().getTime() / 1000 - Date.from(Instant.now()).getTime() / 1000 < 15) {
					listing.increaseTimeForBid();
				}

				player.sendMessages(TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.AUCTION_BID), player, null, variables));
				listing.getEntry().getPrice().add(listing.getAucData().getIncrement());
				GTS.getInstance().getStorage().updateListing(listing);

				List<Text> broadcast = TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.AUCTION_BID_BROADCAST), player, null, variables);
				Sponge.getServer().getOnlinePlayers().stream()
						.filter(pl -> GTS.getInstance().getIgnorers().contains(pl.getUniqueId()))
						.forEach(pl -> pl.sendMessages(broadcast));
			}
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
