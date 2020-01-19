package me.nickimpact.gts.ui;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.spigot.ui.SpigotIcon;
import com.nickimpact.impactor.spigot.ui.SpigotLayout;
import com.nickimpact.impactor.spigot.ui.SpigotUI;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.searching.Searcher;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.discord.DiscordNotifier;
import me.nickimpact.gts.discord.Message;
import me.nickimpact.gts.spigot.SpigotListing;
import me.nickimpact.gts.spigot.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.List;

public class SpigotConfirmUI {

	private SpigotUI view;
	private Player viewer;
	private SpigotListing focus;

	private boolean confirmed;

	/** These settings are for search specific settings */
	private Searcher searcher;
	private String input;

	public SpigotConfirmUI(Player viewer, SpigotListing focus, @Nullable Searcher searcher, @Nullable String input) {
		this.viewer = viewer;
		this.focus = focus;
		this.searcher = searcher;
		this.input = input;
		this.view = SpigotUI.builder()
				.size(54)
				.title(GTS.getInstance().getTokenService().process(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.UI_TITLES_CONFIRMATION), viewer, null, null))
				.build()
				.define(this.design());
	}

	public void open() {
		this.view.open(viewer);
	}

	private SpigotLayout design() {
		SpigotLayout.SpigotLayoutBuilder builder = SpigotLayout.builder();
		builder.rows(SpigotIcon.BORDER, 0, 4);
		builder.columns(SpigotIcon.BORDER, 0, 8);
		builder.slot(SpigotIcon.BORDER, 49);

		builder.slot(new SpigotIcon(focus.getDisplay(this.viewer)), 22);

		if(!this.viewer.getUniqueId().equals(this.focus.getOwnerUUID())) {
			ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(GTS.getInstance().getTokenService().process(MsgConfigKeys.CONFIRM_SELECTION, viewer, null, null));
			item.setItemMeta(meta);
			SpigotIcon confirm = new SpigotIcon(item);
			confirm.addListener(clickable -> {
				SpigotLayout layout = this.view.getLayout();
				SpigotLayout.SpigotLayoutBuilder slb = SpigotLayout.builder().from(layout);

				this.confirmed = true;
				ItemStack confirmed = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
				ItemMeta m = confirmed.getItemMeta();
				m.setDisplayName(GTS.getInstance().getTokenService().process(MsgConfigKeys.CONFIRMED, viewer, null, null));
				confirmed.setItemMeta(m);
				SpigotIcon conf = new SpigotIcon(confirmed);
				slb.hollowSquare(conf, 22);

				ItemStack click = new ItemStack(Material.INK_SACK, 1, (short) 10);
				ItemMeta c = click.getItemMeta();
				c.setDisplayName(GTS.getInstance().getTokenService().process(MsgConfigKeys.CONFIRM_PURCHASE, viewer, null, null));
				click.setItemMeta(c);
				SpigotIcon cl = new SpigotIcon(click);
				cl.addListener(c2 -> {
					if (this.confirmed) {
						GTS.getInstance().getAPIService().getListingManager().purchase(c2.getPlayer().getUniqueId(), this.focus);
						this.view.close(c2.getPlayer());
					}
				});
				slb.slots(cl, 46, 47, 48);
				this.view.define(slb.build());
			});
			builder.hollowSquare(confirm, 22);

			ItemStack require = new ItemStack(Material.BARRIER);
			ItemMeta rMeta = require.getItemMeta();
			rMeta.setDisplayName(GTS.getInstance().getTokenService().process(MsgConfigKeys.REQUIRES_CONFIRMATION, viewer, null, null));
			require.setItemMeta(rMeta);
			builder.slots(new SpigotIcon(require), 46, 47, 48);
		} else {
			ItemStack remover = new ItemStack(Material.ANVIL);
			ItemMeta m = remover.getItemMeta();
			m.setDisplayName(GTS.getInstance().getTokenService().process(MsgConfigKeys.REMOVE_BUTTON, viewer, null, null));
			remover.setItemMeta(m);
			SpigotIcon icon = new SpigotIcon(remover);
			icon.addListener(clickable -> {
				this.view.close(clickable.getPlayer());
				if(!GTS.getInstance().getAPIService().getListingManager().getListingByID(this.focus.getUuid()).isPresent()) {
					clickable.getPlayer().sendMessage(GTS.getInstance().getTokenService().process(MsgConfigKeys.REMOVED_MISSING, viewer, null, null).toArray(new String[]{}));
					return;
				}

				this.focus.getEntry().giveEntry(clickable.getPlayer());
				GTS.getInstance().getAPIService().getListingManager().deleteListing(this.focus);

				List<String> details = Lists.newArrayList("");
				details.addAll(this.focus.getEntry().getDetails());
				String discord = MessageUtils.asSingleWithNewlines(Lists.newArrayList(
						"Publisher: " + Bukkit.getOfflinePlayer(this.focus.getOwnerUUID()).getName(),
						"Publisher Identifier: " + this.focus.getOwnerUUID().toString(),
						"",
						"Published Item: " + this.focus.getName(),
						"Item Details: " + MessageUtils.asSingleWithNewlines(details)
				));

				DiscordNotifier notifier = new DiscordNotifier(GTS.getInstance());
				Message message = notifier.forgeMessage(GTS.getInstance().getConfiguration().get(ConfigKeys.DISCORD_REMOVE), discord);
				notifier.sendMessage(message);
			});
			builder.slots(icon, 46, 47, 48);
		}

		ItemStack cancel = new ItemStack(Material.INK_SACK, 1, (short) 8);
		ItemMeta cMeta = cancel.getItemMeta();
		cMeta.setDisplayName(GTS.getInstance().getTokenService().process(MsgConfigKeys.CANCEL, viewer, null, null));
		cancel.setItemMeta(cMeta);
		SpigotIcon icon = new SpigotIcon(cancel);
		icon.addListener(clickable -> {
			this.view.close(this.viewer);
			new SpigotMainUI(this.viewer, this.searcher, this.input).open();
		});
		builder.slots(icon, 50, 51, 52);

		return builder.build();
	}
}
