package me.nickimpact.gts.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeLayout;
import com.nickimpact.impactor.sponge.ui.SpongeUI;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.searching.Searcher;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.discord.DiscordNotifier;
import me.nickimpact.gts.discord.Message;
import me.nickimpact.gts.sponge.SpongeListing;
import me.nickimpact.gts.sponge.TextParsingUtils;
import me.nickimpact.gts.sponge.utils.MessageUtils;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpongeConfirmUI {

	private SpongeUI view;
	private Player viewer;
	private SpongeListing focus;

	private boolean confirmed;

	/** These settings are for search specific settings */
	private Searcher searcher;
	private String input;

	public SpongeConfirmUI(Player player, SpongeListing focus, @Nullable Searcher searcher, @Nullable String input) {
		this.viewer = player;
		this.focus = focus;
		this.searcher = searcher;
		this.input = input;
		this.view = SpongeUI.builder()
				.dimension(InventoryDimension.of(9, 6))
				.title(GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, GTS.getInstance().getMsgConfig(), MsgConfigKeys.UI_TITLES_CONFIRMATION, null, null))
				.build()
				.define(this.design());
	}

	public void open() {
		this.view.open(this.viewer);
	}

	private SpongeLayout design() {
		SpongeLayout.SpongeLayoutBuilder slb = SpongeLayout.builder();
		slb.rows(SpongeIcon.BORDER, 0, 4);
		slb.columns(SpongeIcon.BORDER, 0, 8);
		slb.slot(SpongeIcon.BORDER, 49);

		slb.slot(new SpongeIcon(focus.getDisplay(this.viewer)), 22);

		Config msgs = GTS.getInstance().getMsgConfig();
		TextParsingUtils parser = GTS.getInstance().getTextParsingUtils();
		if(!this.viewer.getUniqueId().equals(this.focus.getOwnerUUID())) {
			SpongeIcon confirmer = new SpongeIcon(ItemStack.builder()
					.itemType(ItemTypes.STAINED_GLASS_PANE)
					.add(Keys.DISPLAY_NAME, parser.fetchAndParseMsg(this.viewer, msgs, MsgConfigKeys.CLICK_TO_CONFIRM, null, null))
					.add(Keys.DYE_COLOR, DyeColors.GRAY)
					.build()
			);
			confirmer.addListener(clickable -> {
				SpongeLayout layout = this.view.getLayout();
				SpongeLayout.SpongeLayoutBuilder slbm = SpongeLayout.builder().from(layout);

				this.confirmed = true;

				SpongeIcon confirmed = new SpongeIcon(ItemStack.builder()
						.itemType(ItemTypes.STAINED_GLASS_PANE)
						.add(Keys.DISPLAY_NAME, parser.fetchAndParseMsg(this.viewer, msgs, MsgConfigKeys.CONFIRMED, null, null))
						.add(Keys.DYE_COLOR, DyeColors.LIME)
						.build()
				);
				slbm.hollowSquare(confirmed, 22);

				SpongeIcon purchase = new SpongeIcon(ItemStack.builder()
						.itemType(ItemTypes.STAINED_GLASS_PANE)
						.add(Keys.DISPLAY_NAME, parser.fetchAndParseMsg(this.viewer, msgs, MsgConfigKeys.CONFIRM_PURCHASE, null, null))
						.add(Keys.DYE_COLOR, DyeColors.LIME)
						.build()
				);
				purchase.addListener(c2 -> {
					if(this.confirmed) {
						GTS.getInstance().getAPIService().getListingManager().purchase(c2.getPlayer().getUniqueId(), this.focus);
						this.view.close(c2.getPlayer());
					}
				});
				slbm.slots(purchase, 46, 47, 48);
				this.view.define(slbm.build());
			});
			slb.hollowSquare(confirmer, 22);

			SpongeIcon require = new SpongeIcon(ItemStack.builder()
					.itemType(ItemTypes.BARRIER)
					.add(Keys.DISPLAY_NAME, parser.fetchAndParseMsg(this.viewer, msgs, MsgConfigKeys.REQUIRES_CONFIRMATION, null, null))
					.build()
			);
			slb.slots(require, 46, 47, 48);
		} else {
			SpongeIcon remover = new SpongeIcon(ItemStack.builder()
					.itemType(ItemTypes.ANVIL)
					.add(Keys.DISPLAY_NAME, parser.fetchAndParseMsg(this.viewer, msgs, MsgConfigKeys.REMOVE_BUTTON, null, null))
					.build()
			);

			Map<String, Object> variables = Maps.newHashMap();
			variables.put("listing", this.focus);
			variables.put("entry", this.focus.getEntry().getEntry());

			remover.addListener(clickable -> {
				this.view.close(clickable.getPlayer());
				if(!GTS.getInstance().getAPIService().getListingManager().getListingByID(this.focus.getUuid()).isPresent()) {
					clickable.getPlayer().sendMessages(parser.fetchAndParseMsgs(this.viewer, msgs, MsgConfigKeys.REMOVED_MISSING, null, variables));
					return;
				}

				this.focus.getEntry().giveEntry(clickable.getPlayer());
				GTS.getInstance().getAPIService().getListingManager().deleteListing(this.focus);

				List<String> details = Lists.newArrayList("");
				details.addAll(this.focus.getEntry().getDetails());

				clickable.getPlayer().sendMessages(GTS.getInstance().getTextParsingUtils().fetchAndParseMsgs(clickable.getPlayer(), MsgConfigKeys.REMOVAL_CHOICE, null, null));

				Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
				tokens.put("gts_publisher", src -> Optional.of(Text.of(clickable.getPlayer().getName())));
				tokens.put("gts_publisher_id", src -> Optional.of(Text.of(clickable.getPlayer().getUniqueId().toString())));
				tokens.put("gts_published_item", src -> Optional.of(Text.of(this.focus.getEntry().getName())));
				tokens.put("gts_published_item_details", src -> Optional.of(Text.of(MessageUtils.asSingleWithNewlines(details))));

				String discord = MessageUtils.asSingleWithNewlines(GTS.getInstance().getTextParsingUtils().fetchAndParseMsgs(
						null, GTS.getInstance().getMsgConfig(), MsgConfigKeys.DISCORD_REMOVAL_TEMPLATE, tokens, variables
				).stream().map(Text::toPlain).collect(Collectors.toList()));

				DiscordNotifier notifier = new DiscordNotifier(GTS.getInstance());
				Message message = notifier.forgeMessage(GTS.getInstance().getConfiguration().get(ConfigKeys.DISCORD_REMOVE), discord);
				notifier.sendMessage(message);
			});
			slb.slots(remover, 46, 47, 48);
		}

		SpongeIcon cancel = new SpongeIcon(ItemStack.builder()
				.itemType(ItemTypes.DYE)
				.add(Keys.DISPLAY_NAME, parser.fetchAndParseMsg(this.viewer, msgs, MsgConfigKeys.CANCEL, null, null))
				.add(Keys.DYE_COLOR, DyeColors.GRAY)
				.build()
		);
		cancel.addListener(clickable -> {
			this.view.close(this.viewer);
			new SpongeMainUI(this.viewer, this.searcher, this.input).open();
		});
		return slb.slots(cancel, 50, 51, 52).build();
	}
}
