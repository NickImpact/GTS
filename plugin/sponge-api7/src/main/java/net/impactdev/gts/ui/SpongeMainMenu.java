package net.impactdev.gts.ui;

import com.google.common.collect.Lists;
import net.impactdev.gts.api.events.placeholders.PlaceholderReadyEvent;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.sponge.listings.ui.SpongeMainPageProvider;
import net.impactdev.gts.sponge.listings.ui.creator.SpongeEntryTypeSelectionMenu;
import net.impactdev.gts.ui.submenu.SpongeListingMenu;
import net.impactdev.gts.ui.submenu.settings.PlayerSettingsMenu;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.event.EventSubscription;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongeUI;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.config.wrappers.TitleLorePair;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.gts.ui.submenu.stash.SpongeStashMenu;
import net.impactdev.gts.sponge.utils.items.SkullCreator;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.concurrent.CompletionException;

import static net.impactdev.gts.sponge.utils.Utilities.readMessageConfigOption;

public class SpongeMainMenu implements SpongeMainPageProvider {

	private static final MessageService<Text> PARSER = Utilities.PARSER;

	private final SpongeUI view;
	private final Player viewer;

	private EventSubscription<PlaceholderReadyEvent> subscription;

	public SpongeMainMenu(Player viewer) {
		this.viewer = viewer;
		this.view = this.construct();
		this.view.attachCloseListener(close -> {
			this.subscription.close();
		});
	}

	@Override
	public Player getViewer() {
		return this.viewer;
	}

	public void open() {
		this.view.open(this.viewer);
	}

	private SpongeUI construct() {
		return SpongeUI.builder()
				.title(PARSER.parse(readMessageConfigOption(MsgConfigKeys.UI_MAIN_TITLE), Lists.newArrayList(() -> this.viewer)))
				.dimension(InventoryDimension.of(9, 5))
				.build()
				.define(this.display());
	}

	private SpongeLayout display() {
		SpongeLayout.SpongeLayoutBuilder slb = SpongeLayout.builder();
		slb.dimension(9, 5);
		slb.border();

		TitleLorePair browse = readMessageConfigOption(MsgConfigKeys.UI_MAIN_BROWSER);
		SpongeIcon browser = new SpongeIcon(ItemStack.builder()
				.from(SkullCreator.fromBase64("MmUyY2M0MjAxNWU2Njc4ZjhmZDQ5Y2NjMDFmYmY3ODdmMWJhMmMzMmJjZjU1OWEwMTUzMzJmYzVkYjUwIn19fQ=="))
				.add(Keys.DISPLAY_NAME, PARSER.parse(browse.getTitle(), Lists.newArrayList(() -> this.viewer)))
				.add(Keys.ITEM_LORE, PARSER.parse(browse.getLore(), Lists.newArrayList(() -> this.viewer)))
				.build()
		);
		browser.addListener(clickable -> {
			SpongeListingMenu b = new SpongeListingMenu(this.viewer);
			b.open();
		});
		slb.slot(browser, 13);

		TitleLorePair selling = readMessageConfigOption(MsgConfigKeys.UI_MAIN_SELL);
		SpongeIcon sell = new SpongeIcon(ItemStack.builder()
				.from(SkullCreator.fromBase64("N2UzZGViNTdlYWEyZjRkNDAzYWQ1NzI4M2NlOGI0MTgwNWVlNWI2ZGU5MTJlZTJiNGVhNzM2YTlkMWY0NjVhNyJ9fX0="))
				.add(Keys.DISPLAY_NAME, PARSER.parse(selling.getTitle(), Lists.newArrayList(() -> this.viewer)))
				.add(Keys.ITEM_LORE, PARSER.parse(selling.getLore(), Lists.newArrayList(() -> this.viewer)))
				.build()
		);
		sell.addListener(clickable -> {
			new SpongeEntryTypeSelectionMenu(this.viewer).open();
		});
		slb.slot(sell, 11);

		this.createStashIcon(slb);

		TitleLorePair personal = readMessageConfigOption(MsgConfigKeys.UI_MAIN_VIEW_PERSONAL_LISTINGS);
		SpongeIcon personalIcon = new SpongeIcon(ItemStack.builder()
				.from(SkullCreator.fromBase64("ODJhZTE5MTA3MDg2ZGQzMTRkYWYzMWQ4NjYxOGU1MTk0OGE2ZTNlMjBkOTZkY2ExN2QyMWIyNWQ0MmQyYjI0In19fQ=="))
				.add(Keys.DISPLAY_NAME, PARSER.parse(personal.getTitle(), Lists.newArrayList(() -> this.viewer)))
				.add(Keys.ITEM_LORE, PARSER.parse(personal.getLore(), Lists.newArrayList(() -> this.viewer)))
				.build()
		);
		personalIcon.addListener(clickable -> {
			new SpongeListingMenu(this.viewer, listing -> listing.getLister().equals(this.viewer.getUniqueId())).open();
		});
		slb.slot(personalIcon, 29);

		TitleLorePair cBids = readMessageConfigOption(MsgConfigKeys.UI_MAIN_CURRENT_BIDS_MULTI);
		SpongeIcon bids = new SpongeIcon(ItemStack.builder()
				.itemType(ItemTypes.KNOWLEDGE_BOOK)
				.add(Keys.DISPLAY_NAME, PARSER.parse(cBids.getTitle(), Lists.newArrayList(() -> this.viewer)))
				.add(Keys.ITEM_LORE, PARSER.parse(cBids.getLore(), Lists.newArrayList(
						() -> this.viewer,
						this.viewer::getUniqueId
				)))
				.build()
		);
		this.subscription = Impactor.getInstance().getEventBus().subscribe(PlaceholderReadyEvent.class, event -> {
			if(event.getPlaceholderID().equals("gts:active_bids")) {
				if(event.getSource().equals(this.getViewer().getUniqueId())) {
					GTSPlugin.getInstance().getPluginLogger().debug("Active Bids placeholder marked available");

					int amount = (int) event.getValue();
					GTSPlugin.getInstance().getPluginLogger().debug("Value = " + amount);

					TitleLorePair proper = readMessageConfigOption(
							amount == 1 ? MsgConfigKeys.UI_MAIN_CURRENT_BIDS_SINGLE :
									MsgConfigKeys.UI_MAIN_CURRENT_BIDS_MULTI
					);

					final Text name = PARSER.parse(proper.getTitle(), Lists.newArrayList(() -> this.viewer));
					final List<Text> lore = PARSER.parse(proper.getLore(), Lists.newArrayList(
						() -> this.viewer,
						this.viewer::getUniqueId
					));

					Impactor.getInstance().getScheduler().executeSync(() -> {
						ItemStack result = ItemStack.builder()
								.from(bids.getDisplay())
								.add(Keys.DISPLAY_NAME, name)
								.add(Keys.ITEM_LORE, lore)
								.build();

						SpongeIcon icon = new SpongeIcon(result);
						icon.addListener(clickable -> {
							new SpongeListingMenu(
									this.viewer,
									listing -> {
										if(listing instanceof Auction) {
											Auction auction = (Auction) listing;
											return auction.getBids().containsKey(this.viewer.getUniqueId());
										}

										return false;
									}
							).open();
						});
						this.view.setSlot(33, icon);
					});
				}
			}
		});
		bids.addListener(clickable -> {
			new SpongeListingMenu(
					this.viewer,
					listing -> {
						if(listing instanceof Auction) {
							Auction auction = (Auction) listing;
							return auction.getBids().containsKey(this.viewer.getUniqueId());
						}

						return false;
					}
			).open();
		});
		slb.slot(bids, 33);

		TitleLorePair settings = readMessageConfigOption(MsgConfigKeys.UI_MAIN_PLAYER_SETTINGS);
		SpongeIcon ps = new SpongeIcon(ItemStack.builder()
				.itemType(ItemTypes.ANVIL)
				.add(Keys.DISPLAY_NAME, PARSER.parse(settings.getTitle()))
				.add(Keys.ITEM_LORE, PARSER.parse(settings.getLore()))
				.build()
		);
		ps.addListener(clickable -> {
			new PlayerSettingsMenu(this.viewer).open();
		});
		slb.slot(ps, 31);

		return slb.build();
	}

	private void createStashIcon(SpongeLayout.SpongeLayoutBuilder slb) {
		TitleLorePair stashRef = readMessageConfigOption(MsgConfigKeys.UI_MAIN_STASH);
		ItemStack icon = ItemStack.builder()
				.itemType(ItemTypes.CHEST)
				.add(Keys.DISPLAY_NAME, PARSER.parse(stashRef.getTitle(), Lists.newArrayList(() -> this.viewer)))
				.build();

		SpongeIcon stash = new SpongeIcon(icon);
		this.writeStashIconLore(stash, stashRef.getLore());
		stash.addListener(clickable -> {
			new SpongeStashMenu(this.viewer).open();
		});
		slb.slot(stash, 15);
	}

	private void writeStashIconLore(SpongeIcon icon, List<String> loreBase) {
		List<String> lore = Lists.newArrayList();
		lore.addAll(loreBase);
		lore.add("");
		lore.add(readMessageConfigOption(MsgConfigKeys.UI_MAIN_STASH_CLICK_NOTIF));

		GTSPlugin.getInstance().getStorage().getStash(this.viewer.getUniqueId()).thenAccept(
				stash -> {
					if(!stash.isEmpty()) {
						List<String> updated = Lists.newArrayList();
						updated.addAll(loreBase);
						updated.add("");
						updated.add(readMessageConfigOption(MsgConfigKeys.UI_MENU_MAIN_STASH_STATUS));
						updated.add("");
						updated.add(readMessageConfigOption(MsgConfigKeys.UI_MAIN_STASH_CLICK_NOTIF));

						Impactor.getInstance().getScheduler().executeSync(() -> {
							icon.getDisplay().offer(Keys.ITEM_LORE, PARSER.parse(updated, Lists.newArrayList(() -> this.viewer, () -> stash)));
							this.view.setSlot(15, icon);
						});
					}
				}
		).exceptionally(throwable -> {
			ExceptionWriter.write(throwable);
			throw new CompletionException(throwable);
		});

		icon.getDisplay().offer(Keys.ITEM_LORE, PARSER.parse(lore, Lists.newArrayList(() -> this.viewer)));
	}

	public static class MainMenuCreator implements Creator {

		private Player viewer;

		@Override
		public Creator viewer(Player player) {
			this.viewer = player;
			return this;
		}

		@Override
		public Creator from(SpongeMainPageProvider provider) {
			this.viewer = provider.getViewer();
			return this;
		}

		@Override
		public SpongeMainPageProvider build() {
			return new SpongeMainMenu(this.viewer);
		}

	}
}
