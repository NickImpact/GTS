package me.nickimpact.gts.ui;

import com.google.common.collect.Maps;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.GTSInfo;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.configuration.ConfigKeys;
import me.nickimpact.gts.configuration.MsgConfigKeys;
import me.nickimpact.gts.discord.Message;
import me.nickimpact.gts.internal.TextParsingUtils;
import me.nickimpact.gts.ui.shared.SharedItems;
import me.nickimpact.gts.utils.ListingUtils;
import com.nickimpact.impactor.gui.v2.Displayable;
import com.nickimpact.impactor.gui.v2.Icon;
import com.nickimpact.impactor.gui.v2.Layout;
import com.nickimpact.impactor.gui.v2.UI;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collection;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.function.Predicate;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class ConfirmUI implements Displayable, Observer {

	private UI ui;

	private Player player;

	/**
	 * The listing that the player might wish to purchase
	 */
	private Listing target;

	/**
	 * The lingering search conditions that can be restored if the user cancels purchase
	 */
	private Collection<Predicate<Listing>> searchConditions;

	private boolean confirmed = false;
	private Layout.Builder copy;

	public ConfirmUI(Player player, Listing target, Collection<Predicate<Listing>> searchConditions) {
		this.player = player;
		this.target = target;
		this.searchConditions = searchConditions;

		this.ui = UI.builder()
				.dimension(InventoryDimension.of(9, 6))
				.title(Text.of(TextColors.RED, "GTS ", TextColors.GRAY, "\u00BB ", TextColors.DARK_AQUA, "Confirmation"))
//				.closeAction((close, pl) -> Sponge.getScheduler().getTasksByName("Confirm-" + this.player.getName()).forEach(Task::cancel))
				.build(GTS.getInstance());
		Layout layout = this.forgeDisplay();
		layout.getElements().entrySet().stream()
				.filter(entry -> (entry.getKey() >= 11 && entry.getKey() <= 13) || entry.getKey() == 20 || entry.getKey() == 22 || (entry.getKey() >= 29 && entry.getKey() <= 31))
				.forEach(entry -> {
					entry.getValue().addListener(clickable -> {
						if (!confirmed) {
							this.confirmed = true;
							this.ui.define(
									copy.hollowSquare(Icon.from(
											ItemStack.builder()
													.itemType(ItemTypes.STAINED_GLASS_PANE)
													.add(Keys.DISPLAY_NAME, Text.EMPTY)
													.add(Keys.DYE_COLOR, DyeColors.LIME)
													.build()
									), 21)
											.slots(this.drawConfirmIcon(), 46, 47, 48)
											.build());
						}
					});
				});
		this.ui.define(layout);

//		GTS.getInstance().getUpdater().addObserver(this);
//		Sponge.getScheduler().createTaskBuilder()
//				.execute(this::apply)
//				.interval(1, TimeUnit.SECONDS)
//				.name("Confirm-" + player.getName())
//				.submit(GTS.getInstance());
	}

	@Override
	public UI getDisplay() {
		return this.ui;
	}

	private Layout forgeDisplay() {
		Layout.Builder lb = Layout.builder();
		lb.row(Icon.BORDER, 0).row(Icon.BORDER, 4).column(Icon.BORDER, 0).column(Icon.BORDER, 8).slot(Icon.BORDER, 49);
		if (!this.target.getOwnerUUID().equals(this.player.getUniqueId())) {
			lb.hollowSquare(Icon.from(
					ItemStack.builder()
							.itemType(ItemTypes.STAINED_GLASS_PANE)
							.add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Click here to confirm!"))
							.add(Keys.DYE_COLOR, DyeColors.RED)
							.build()
			), 21);
		}
		lb.slots(this.target.getOwnerUUID().equals(this.player.getUniqueId()) ? this.drawRemoveIcon() : this.drawNeedConfirmIcon(), 46, 47, 48);
		lb.slots(this.drawDenyIcon(), 50, 51, 52);
		lb.slot(this.drawTarget(), 21);
		lb.slot(Icon.from(this.target.getEntry().confirmItemStack(player, this.target)), 24);

		this.copy = lb;
		return lb.build();
	}

	private Icon drawTarget() {
		return Icon.from(this.target.getEntry().baseItemStack(player, this.target));
	}

	private Icon drawRemoveIcon() {
		Map<String, Object> variables = Maps.newHashMap();
		variables.put("dummy", target.getEntry().getEntry());
		variables.put("dummy2", target);
		variables.put("dummy3", target.getEntry());

		Icon icon = new Icon(
				ItemStack.builder()
						.itemType(ItemTypes.ANVIL)
						.add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Remove from GTS"))
						.build()
		);
		icon.addListener(clickable -> {
			if (!GTS.getInstance().getListingsCache().contains(this.target)) {
				clickable.getPlayer().sendMessages(
						Text.of(GTSInfo.ERROR, "Unfortunately, your listing has already been claimed...")
				);
				return;
			}
			this.target.getEntry().giveEntry(clickable.getPlayer());
			ListingUtils.deleteEntry(this.target);
			clickable.getPlayer().sendMessages(TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.REMOVAL_CHOICE), player, null, variables));

			final String b = TextParsingUtils.fetchAndParseMsg(player, MsgConfigKeys.DISCORD_REMOVE, null, variables).toPlain();
			GTS.getInstance().getDiscordNotifier().ifPresent(notifier -> {
				Message message = notifier.forgeMessage(GTS.getInstance().getConfig().get(ConfigKeys.DISCORD_REMOVE), b);
				notifier.sendMessage(message);
			});

			clickable.getPlayer().closeInventory();
		});

		return icon;
	}

	private Icon drawConfirmIcon() {
		Icon icon = SharedItems.confirmIcon(this.target.getAucData() != null);
		icon.addListener(clickable -> {
			if (confirmed) {
				ListingUtils.purchase(clickable.getPlayer(), this.target);
				clickable.getPlayer().closeInventory();
			} else {
				player.sendMessage(Text.of(GTSInfo.ERROR, TextColors.GRAY, "Please confirm your purchase before proceeding"));
			}
		});

		return icon;
	}

	private Icon drawDenyIcon() {
		Icon icon = SharedItems.denyIcon();
		icon.addListener(clickable -> {
			this.close(player);
			new MainUI(clickable.getPlayer(), searchConditions).open();
		});
		return icon;
	}

	private Icon drawNeedConfirmIcon() {
		Icon icon = Icon.from(ItemStack.builder().itemType(ItemTypes.BARRIER).build());
		icon.getDisplay().offer(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Requires Confirmation..."));
		return icon;
	}

	@Override
	public void update(Observable o, Object arg) {
		this.apply();
	}

	private void apply() {
		this.ui.setSlot(21, this.drawTarget());
	}
}
