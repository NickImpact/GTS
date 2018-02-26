package com.nickimpact.gts.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.GTSInfo;
import com.nickimpact.gts.api.listings.entries.Entry;
import com.nickimpact.gts.api.listings.entries.EntryHolder;
import com.nickimpact.gts.api.listings.pricing.Price;
import com.nickimpact.gts.api.listings.pricing.PriceHolder;
import com.nickimpact.gts.api.listings.pricing.RewardException;
import com.nickimpact.gts.configuration.ConfigKeys;
import com.nickimpact.gts.configuration.MsgConfigKeys;
import com.nickimpact.gts.api.events.ListEvent;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.pricing.PricingException;
import com.nickimpact.gts.api.utils.MessageUtils;
import com.nickimpact.gts.logs.Log;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import lombok.Setter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class ListingUtils {

    public static void addToMarket(Player player, Listing listing) {
        if(hasMax(player)) {
	        Map<String, Function<CommandSource, Optional<Text>>> replacements = Maps.newHashMap();
	        replacements.put("max_listings", src -> Optional.of(Text.of(GTS.getInstance().getConfig().get(ConfigKeys.MAX_LISTINGS))));
            try {
	            player.sendMessages(GTS.getInstance().getTextParsingUtils().parse(
			            GTS.getInstance().getMsgConfig().get(MsgConfigKeys.MAX_LISTINGS),
			            player,
			            replacements,
			            null
	            ));
            } catch (NucleusException e) {
	            e.printStackTrace();
            }
            return;
        }

	    ListEvent listEvent = new ListEvent(
			    player,
			    listing,
			    Cause.of(EventContext.builder()
							    .add(EventContextKeys.PLUGIN, GTS.getInstance().getPluginContainer())
							    .add(EventContextKeys.PLAYER_SIMULATED, player.getProfile())
							    .build(),
			             GTS.getInstance()
			    )
	    );
	    Sponge.getEventManager().post(listEvent);

	    if(!listEvent.isCancelled()) {
	    	// We can check here for minimum prices, if we decide to support it

		    Map<String, Object> variables = Maps.newHashMap();
		    variables.put("dummy", listing.getEntry().getElement());
		    variables.put("dummy2", listing);
		    variables.put("dummy3", listing.getEntry());

		    Optional<BigDecimal> tax = Optional.empty();
		    if(GTS.getInstance().getConfig().get(ConfigKeys.TAX_ENABLED)) {
			    try {
			    	BigDecimal t = listing.getEntry().getPrice().calcTax(player);
				    if(t.signum() == -1) {
						Map<String, Function<CommandSource, Optional<Text>>> extras = Maps.newHashMap();
						extras.put("tax", src -> Optional.of(Text.of(t)));

						player.sendMessages(GTS.getInstance().getTextParsingUtils().parse(
								GTS.getInstance().getMsgConfig().get(MsgConfigKeys.TAX_INVALID),
								player,
								extras,
								variables
						));
				    } else {
				    	tax = Optional.of(t);
				    }
			    } catch (Exception e) {
			    	if(e instanceof PricingException) {
					    MessageUtils.genAndSendErrorMessage(
							    "Tax Error",
							    "Unable to calculate tax",
							    "Player: " + player.getName()
					    );
				    }
			    }
		    }

		    if(GTS.getInstance().getConfig().get(ConfigKeys.TAX_ENABLED)) {
			    try {
				    final BigDecimal t = tax.orElse(BigDecimal.ZERO);
				    Map<String, Function<CommandSource, Optional<Text>>> extras = Maps.newHashMap();
				    extras.put("tax", src -> Optional.of(
						    Text.of(GTS.getInstance().getEconomy().getDefaultCurrency().format(t))));
				    player.sendMessages(GTS.getInstance().getTextParsingUtils().parse(
						    GTS.getInstance().getMsgConfig().get(MsgConfigKeys.TAX_APPLICATION),
						    player,
						    extras,
						    null
				    ));
			    } catch (NucleusException e) {
				    MessageUtils.genAndSendErrorMessage(
						    "Message Parse Error",
						    "Nucleus was unable to decode a message properly...",
						    "Template: " + GTS.getInstance().getMsgConfig().get(MsgConfigKeys.TAX_APPLICATION)
				    );
			    }
		    }

		    if(!listing.getEntry().doTakeAway(player)) {
			    // Refund applied tax
			    if(GTS.getInstance().getConfig().get(ConfigKeys.TAX_ENABLED)) {
				    try {
					    UniqueAccount acc = GTS.getInstance().getEconomy().getOrCreateAccount(
							    player.getUniqueId()).orElse(null);
					    if (acc != null)
						    acc.deposit(GTS.getInstance().getEconomy().getDefaultCurrency(),
						                tax.orElse(BigDecimal.ZERO),
						                Cause.builder().append(GTS.getInstance()).build(EventContext.empty()));

					    player.sendMessage(Text.of(GTSInfo.ERROR, TextColors.RED,
					                               "Your listing failed to be taken, so we have refunded the tax applied!"));
				    } catch (Exception e) {
					    e.printStackTrace();
				    }
			    }
			    return;
		    }

			try {
				player.sendMessages(GTS.getInstance().getTextParsingUtils().parse(
						GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ADD_TEMPLATE),
						player,
						null,
						variables
				));
			} catch (NucleusException e) {
				MessageUtils.genAndSendErrorMessage(
						"Message Parse Error",
						"Nucleus was unable to decode a message properly...",
						"Template: " + GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ADD_TEMPLATE)
				);
			}

		    GTS.getInstance().getStorage().addListing(listing);
			GTS.getInstance().getListingsCache().add(listing);

			// Broadcast a message to everyone but the player who deposited the listing and the ignorers
		    Set<Player> players = Sponge.getServer().getOnlinePlayers().stream()
				    .filter(pl -> !pl.getUniqueId().equals(player.getUniqueId()))
				    .filter(pl -> !GTS.getInstance().getIgnorers().contains(pl.getUniqueId()))
				    .collect(Collectors.toSet());
			List<Text> broadcast;

		    try {
			    broadcast = GTS.getInstance().getTextParsingUtils().parse(
			    		GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ADD_BROADCAST),
					    player,
					    null,
					    variables
			    );
		    } catch (NucleusException e) {
			    broadcast = Lists.newArrayList(
			    		Text.of(GTSInfo.PREFIX, "&e" + player.getName() + " &7has deposited a new listing!")
			    );
		    }
		    for(Player pl : players) {
			    pl.sendMessages(broadcast);
		    }

		    GTS.getInstance().getUpdater().sendUpdate();
	    }
    }

    public static void purchase(Player player, Listing listing) {
	    Map<String, Object> variables = Maps.newHashMap();
	    variables.put("dummy", listing.getEntry().getElement());
	    variables.put("dummy2", listing);
	    variables.put("dummy3", listing.getEntry());

		if(!GTS.getInstance().getListingsCache().contains(listing)) {
			try {
				player.sendMessages(
						GTS.getInstance().getTextParsingUtils().parse(
								GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ALREADY_CLAIMED),
								player,
								null,
								variables
						)
				);
			} catch (NucleusException e) {
				e.printStackTrace();
			}
			return;
		}

		Price price = listing.getEntry().getPrice();
	    try {
		    if(price.canPay(player)) {
				price.pay(player);
				player.sendMessages(
						GTS.getInstance().getTextParsingUtils().parse(
								GTS.getInstance().getMsgConfig().get(MsgConfigKeys.PURCHASE_PAY),
								player,
								null,
								variables
						)
				);

				if(!price.supportsOfflineReward()) {
					Player receiver;
					if ((receiver = Sponge.getServer().getPlayer(listing.getOwnerUUID()).orElse(null)) != null) {
						price.reward(listing.getOwnerUUID());
						receiver.sendMessages(
								GTS.getInstance().getTextParsingUtils().parse(
										GTS.getInstance().getMsgConfig().get(MsgConfigKeys.PURCHASE_RECEIVE),
										player,
										null,
										variables
								)
						);
					}
					else {
						addHeldPrice(new PriceHolder(UUID.randomUUID(), listing.getOwnerUUID(), price));
					}
				} else {
					price.reward(listing.getOwnerUUID());
					Sponge.getServer().getPlayer(listing.getOwnerUUID()).ifPresent(pl -> {
						try {
							pl.sendMessages(
									GTS.getInstance().getTextParsingUtils().parse(
											GTS.getInstance().getMsgConfig().get(MsgConfigKeys.PURCHASE_RECEIVE),
											player,
											null,
											variables
									)
							);
						} catch (NucleusException e) {
							e.printStackTrace();
						}
					});
				}

				listing.getEntry().giveEntry(player);
				deleteEntry(listing);
				GTS.getInstance().getUpdater().sendUpdate();
		    } else {
		    	player.sendMessages(
		    			GTS.getInstance().getTextParsingUtils().parse(
		    					GTS.getInstance().getMsgConfig().get(MsgConfigKeys.NOT_ENOUGH_FUNDS),
							    player,
							    null,
							    variables
					    )
			    );
		    }
	    } catch (Exception e) {
	    	if(e instanceof RewardException) {
			    addHeldPrice(new PriceHolder(UUID.randomUUID(), listing.getOwnerUUID(), price));
		    } else {
			    player.sendMessages(
					    Text.of(GTSInfo.ERROR,
					            "Unfortunately, you were unable to purchase the listing due to an error...")
			    );
			    GTS.getInstance().getConsole().ifPresent(console -> console.sendMessages(
					    Text.of(GTSInfo.ERROR, e.getMessage())
			    ));
		    }
	    }
    }

    public static void bid(Player player, Listing listing) {
	    Map<String, Object> variables = Maps.newHashMap();
	    variables.put("listing_specifics", listing);
	    variables.put("listing_name", listing);
	    variables.put("time_left", listing);
	    variables.put("id", listing);

		if(listing.getAucData() != null) {
			if(listing.getAucData().getHighBidder().equals(player.getUniqueId())) {
				try {
					player.sendMessages(
							GTS.getInstance().getTextParsingUtils().parse(
									GTS.getInstance().getMsgConfig().get(MsgConfigKeys.AUCTION_IS_HIGH_BIDDER),
									player,
									null,
									variables
							)
					);
				} catch (NucleusException e) {
					e.printStackTrace();
				}
			} else {
				listing.getAucData().setHighBidder(player.getUniqueId());
				listing.getAucData().setNumIncrements(listing.getAucData().getNumIncrements());

				try {
					List<Text> broadcast = GTS.getInstance().getTextParsingUtils().parse(
							GTS.getInstance().getMsgConfig().get(MsgConfigKeys.AUCTION_BID),
							player,
							null,
							variables
					);
					listing.getAucData().getListeners().stream()
							.filter(pl -> GTS.getInstance().getIgnorers().contains(pl.getUniqueId()))
							.forEach(pl -> pl.sendMessages(broadcast));

				} catch (NucleusException e) {
					e.printStackTrace();
				}
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

	public static void addHeldEntry(EntryHolder holder) {
		GTS.getInstance().getHeldEntryCache().add(holder);
		GTS.getInstance().getStorage().addHeldElement(holder);
	}

	public static void addHeldPrice(PriceHolder holder) {
		GTS.getInstance().getHeldPriceCache().add(holder);
		GTS.getInstance().getStorage().addHeldPrice(holder);
	}
}
