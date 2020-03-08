package me.nickimpact.gts.common.tasks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.impactor.api.configuration.Config;
import io.github.nucleuspowered.nucleus.api.placeholder.PlaceholderVariables;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.discord.DiscordNotifier;
import me.nickimpact.gts.discord.Message;
import me.nickimpact.gts.sponge.SpongeListing;
import me.nickimpact.gts.sponge.text.placeholders.ListingPlaceholderVariableKey;
import me.nickimpact.gts.sponge.utils.MessageUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpongeListingTasks implements ListingTasks<SpongeListing> {
	@Override
	public void createExpirationTask() {
		Sponge.getScheduler().createTaskBuilder().execute(() -> {
			final List<SpongeListing> listings = ImmutableList.copyOf(GTS.getInstance().getAPIService().getListingManager().getListings());
			listings.stream().filter(listing -> listing.getExpiration().isBefore(LocalDateTime.now())).forEach(listing -> {
				if(expire(listing)) {
					GTS.getInstance().getAPIService().getListingManager().deleteListing(listing);
				}
			});
		}).interval(1, TimeUnit.SECONDS).submit(GTS.getInstance());
	}

	@Override
	public boolean expire(SpongeListing listing) {
		Optional<User> user = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(listing.getOwnerUUID());
		if(user.isPresent()) {
			if(!user.get().isOnline()) {
				if(listing.getEntry().supportsOffline()) {
					boolean state = listing.getEntry().giveEntry(user.get());
					if(state) {
						this.sendDiscordMessage(listing, user.get());
					}

					return state;
				} else {
					return false;
				}
			}

			if(!listing.getEntry().giveEntry(user.get())) {
				return false;
			}

			PlaceholderVariables variables = PlaceholderVariables.builder()
					.put(new ListingPlaceholderVariableKey(), listing)
					.build();

			if(user.get().getPlayer().isPresent()) {
				Player player = user.get().getPlayer().get();
				Config config = GTS.getInstance().getMsgConfig();
				player.sendMessages(GTS.getInstance().getTextParsingUtils().fetchAndParseMsgs(player, config, MsgConfigKeys.REMOVAL_EXPIRES, null, variables));
			}

			this.sendDiscordMessage(listing, user.get());
			return true;
		}

		return false;
	}

	private void sendDiscordMessage(SpongeListing listing, User user) {
		PlaceholderVariables variables = PlaceholderVariables.builder()
				.put(new ListingPlaceholderVariableKey(), listing)
				.build();

		List<String> details = Lists.newArrayList("");
		details.addAll(listing.getEntry().getDetails());

		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		tokens.put("gts_publisher", src -> Optional.of(Text.of(user.getName())));
		tokens.put("gts_publisher_id", src -> Optional.of(Text.of(listing.getOwnerUUID().toString())));
		tokens.put("gts_published_item", src -> Optional.of(Text.of(listing.getEntry().getName())));
		tokens.put("gts_published_item_details", src -> Optional.of(Text.of(MessageUtils.asSingleWithNewlines(details))));

		String discord = MessageUtils.asSingleWithNewlines(GTS.getInstance().getTextParsingUtils().fetchAndParseMsgs(
				null, GTS.getInstance().getMsgConfig(), MsgConfigKeys.DISCORD_EXPIRATION_TEMPLATE, tokens, variables
		).stream().map(Text::toPlain).collect(Collectors.toList()));

		DiscordNotifier notifier = new DiscordNotifier(GTS.getInstance());
		Message message = notifier.forgeMessage(GTS.getInstance().getConfiguration().get(ConfigKeys.DISCORD_EXPIRE), discord);
		notifier.sendMessage(message);
	}
}
