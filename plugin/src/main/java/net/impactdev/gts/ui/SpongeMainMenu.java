package net.impactdev.gts.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.impactdev.gts.api.stashes.Stash;
import net.impactdev.gts.api.ui.GTSMenu;
import net.impactdev.gts.sponge.utils.items.ProvidedIcons;
import net.impactdev.gts.ui.submenu.SpongeListingMenu;
import net.impactdev.gts.ui.submenu.settings.PlayerSettingsMenu;
import net.impactdev.gts.ui.submenu.stash.SpongeStashMenu;
import net.impactdev.gts.api.events.placeholders.PlaceholderReadyEvent;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.sponge.listings.ui.SpongeMainPageProvider;
import net.impactdev.gts.sponge.listings.ui.creator.SpongeEntryTypeSelectionMenu;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.event.EventSubscription;
import net.impactdev.impactor.api.placeholders.PlaceholderSources;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.config.wrappers.TitleLorePair;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.gts.sponge.utils.items.SkullCreator;
import net.impactdev.impactor.api.ui.containers.ImpactorUI;
import net.impactdev.impactor.api.ui.containers.icons.DisplayProvider;
import net.impactdev.impactor.api.ui.containers.icons.Icon;
import net.impactdev.impactor.api.ui.containers.layouts.Layout;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.server.query.QueryServerEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionException;

public class SpongeMainMenu implements GTSMenu, SpongeMainPageProvider {

	private static final MessageService PARSER = Utilities.PARSER;

	private final ImpactorUI view;
	private final PlatformPlayer viewer;

	private EventSubscription<PlaceholderReadyEvent> subscription;

	public SpongeMainMenu(PlatformPlayer viewer) {
		this.viewer = viewer;
		this.view = this.construct();
	}

	@Override
	public PlatformPlayer getViewer() {
		return this.viewer;
	}

	public void open() {
		this.view.open(this.viewer);
	}

	private ImpactorUI construct() {
		PlaceholderSources sources = PlaceholderSources.builder()
				.append(PlatformPlayer.class, () -> this.viewer)
				.build();

		return ImpactorUI.builder()
				.provider(Key.key("gts:main-menu"))
				.title(PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_MAIN_TITLE), sources))
				.layout(this.display())
				.onClose(context -> {
					try {
						this.subscription.close();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}

					return true;
				})
				.build();
	}

	private Layout display() {
		Layout.LayoutBuilder builder = Layout.builder();
		builder.size(5);
		builder.border(ProvidedIcons.BORDER);

		PlaceholderSources sources = PlaceholderSources.builder()
				.append(PlatformPlayer.class, () -> this.viewer)
				.build();

		TitleLorePair browse = Utilities.readMessageConfigOption(MsgConfigKeys.UI_MAIN_BROWSER);
		Icon<ItemStack> browser = Icon.builder(ItemStack.class)
				.display(new DisplayProvider.Constant<>(ItemStack.builder()
						.from(SkullCreator.fromBase64("MmUyY2M0MjAxNWU2Njc4ZjhmZDQ5Y2NjMDFmYmY3ODdmMWJhMmMzMmJjZjU1OWEwMTUzMzJmYzVkYjUwIn19fQ=="))
						.add(Keys.CUSTOM_NAME, PARSER.parse(browse.getTitle(), sources))
						.add(Keys.LORE, PARSER.parse(browse.getLore(), sources))
						.build()
				))
				.listener(context -> {
					SpongeListingMenu menu = new SpongeListingMenu(this.viewer, false);
					menu.open();
					return false;
				})
				.build();
		builder.slot(browser, 13);

		TitleLorePair selling = Utilities.readMessageConfigOption(MsgConfigKeys.UI_MAIN_SELL);
		Icon<ItemStack> sell = Icon.builder(ItemStack.class)
				.display(new DisplayProvider.Constant<>(ItemStack.builder()
						.from(SkullCreator.fromBase64("N2UzZGViNTdlYWEyZjRkNDAzYWQ1NzI4M2NlOGI0MTgwNWVlNWI2ZGU5MTJlZTJiNGVhNzM2YTlkMWY0NjVhNyJ9fX0="))
						.add(Keys.CUSTOM_NAME, PARSER.parse(selling.getTitle(), sources))
						.add(Keys.LORE, PARSER.parse(selling.getLore(), sources))
						.build()
				))
				.listener(context -> {
					new SpongeEntryTypeSelectionMenu(this.viewer).open();
					return false;
				})
				.build();
		builder.slot(sell, 11);
		this.createStashIcon(builder);

		TitleLorePair personal = Utilities.readMessageConfigOption(MsgConfigKeys.UI_MAIN_VIEW_PERSONAL_LISTINGS);
		Icon<ItemStack> personalIcon = Icon.builder(ItemStack.class)
				.display(new DisplayProvider.Constant<>(ItemStack.builder()
						.from(SkullCreator.fromBase64("ODJhZTE5MTA3MDg2ZGQzMTRkYWYzMWQ4NjYxOGU1MTk0OGE2ZTNlMjBkOTZkY2ExN2QyMWIyNWQ0MmQyYjI0In19fQ=="))
						.add(Keys.CUSTOM_NAME, PARSER.parse(personal.getTitle(), sources))
						.add(Keys.LORE, PARSER.parse(personal.getLore(), sources))
						.build()
				))
				.listener(context -> {
					new SpongeListingMenu(this.viewer, false, Sets.newHashSet(listing -> listing.getLister().equals(this.viewer.uuid())), null).open();
					return false;
				})
				.build();
		builder.slot(personalIcon, 29);

		TitleLorePair cBids = Utilities.readMessageConfigOption(MsgConfigKeys.UI_MAIN_CURRENT_BIDS_MULTI);
		Icon<ItemStack> bids = Icon.builder(ItemStack.class)
				.display(new DisplayProvider.Constant<>(ItemStack.builder()
						.itemType(ItemTypes.KNOWLEDGE_BOOK)
						.add(Keys.CUSTOM_NAME, PARSER.parse(cBids.getTitle(), sources))
						.add(Keys.LORE, PARSER.parse(cBids.getLore(), sources.append(UUID.class, this.viewer::uuid)))
						.build()
				))
				.listener(context -> {
					new SpongeListingMenu(
							this.viewer,
							false,
							Sets.newHashSet(listing -> {
								if(listing instanceof Auction) {
									Auction auction = (Auction) listing;
									return auction.getBids().containsKey(this.viewer.uuid());
								}

								return false;
							}),
							null
					).open();

					return false;
				})
				.build();
		this.subscription = Impactor.getInstance().getEventBus().subscribe(GTSPlugin.instance().metadata(), PlaceholderReadyEvent.class, event -> {
			if(event.getPlaceholderID().equals("gts:active_bids")) {
				if(event.getSource().equals(this.getViewer().uuid())) {
					int amount = (int) event.getValue();
					TitleLorePair proper = Utilities.readMessageConfigOption(
							amount == 1 ? MsgConfigKeys.UI_MAIN_CURRENT_BIDS_SINGLE :
									MsgConfigKeys.UI_MAIN_CURRENT_BIDS_MULTI
					);

					final Component name = PARSER.parse(proper.getTitle(), sources);
					final List<Component> lore = PARSER.parse(proper.getLore(), sources);

					Impactor.getInstance().getScheduler().executeSync(() -> {
						ItemStack result = ItemStack.builder()
								.from(bids.display().provide())
								.add(Keys.CUSTOM_NAME, name)
								.add(Keys.LORE, lore)
								.build();

						Icon<ItemStack> icon = Icon.builder(ItemStack.class)
								.display(new DisplayProvider.Constant<>(result))
								.listener(context -> {
									new SpongeListingMenu(
											this.viewer,
											false,
											Sets.newHashSet(listing -> {
												if(listing instanceof Auction) {
													Auction auction = (Auction) listing;
													return auction.getBids().containsKey(this.viewer.uuid());
												}

												return false;
											}),
											null
									).open();

									return false;
								})
								.build();

						this.view.set(icon, 33);
					});
				}
			}
		});
		builder.slot(bids, 33);

		TitleLorePair settings = Utilities.readMessageConfigOption(MsgConfigKeys.UI_MAIN_PLAYER_SETTINGS);
		Icon<ItemStack> ps = Icon.builder(ItemStack.class)
				.display(new DisplayProvider.Constant<>(ItemStack.builder()
						.itemType(ItemTypes.ANVIL)
						.add(Keys.CUSTOM_NAME, PARSER.parse(settings.getTitle()))
						.add(Keys.LORE, PARSER.parse(settings.getLore()))
						.build()
				))
				.listener(context -> {
					new PlayerSettingsMenu(this.viewer).open(this.viewer);
					return false;
				})
				.build();
		builder.slot(ps, 31);
		return builder.build();
	}

	private void createStashIcon(Layout.LayoutBuilder slb) {
		PlaceholderSources sources = PlaceholderSources.builder()
				.append(PlatformPlayer.class, () -> this.viewer)
				.build();

		TitleLorePair stashRef = Utilities.readMessageConfigOption(MsgConfigKeys.UI_MAIN_STASH);
		ItemStack icon = ItemStack.builder()
				.itemType(ItemTypes.CHEST)
				.add(Keys.CUSTOM_NAME, PARSER.parse(stashRef.getTitle(), sources))
				.build();

		Icon<ItemStack> stash = Icon.builder(ItemStack.class)
				.display(new DisplayProvider.Constant<>(icon))
				.listener(context -> {
					new SpongeStashMenu(this.viewer).open();
					return false;
				})
				.build();
		this.writeStashIconLore(stash, stashRef.getLore());
		slb.slot(stash, 15);
	}

	private void writeStashIconLore(Icon<ItemStack> icon, List<String> loreBase) {
		List<String> lore = Lists.newArrayList();
		lore.addAll(loreBase);
		lore.add("");
		lore.add(Utilities.readMessageConfigOption(MsgConfigKeys.UI_MAIN_STASH_CLICK_NOTIF));

		GTSPlugin.instance().storage().getStash(this.viewer.uuid()).thenAccept(
				stash -> {
					if(!stash.isEmpty()) {
						List<String> updated = Lists.newArrayList();
						updated.addAll(loreBase);
						updated.add("");
						updated.add(Utilities.readMessageConfigOption(MsgConfigKeys.UI_MENU_MAIN_STASH_STATUS));
						updated.add("");
						updated.add(Utilities.readMessageConfigOption(MsgConfigKeys.UI_MAIN_STASH_CLICK_NOTIF));

						Impactor.getInstance().getScheduler().executeSync(() -> {
							PlaceholderSources sources = PlaceholderSources.builder()
									.append(PlatformPlayer.class, () -> this.viewer)
									.append(Stash.class, () -> stash)
									.build();

							icon.display().provide().offer(Keys.LORE, PARSER.parse(updated, sources));
							this.view.set(icon, 15);
						});
					}
				}
		).exceptionally(throwable -> {
			ExceptionWriter.write(throwable);
			throw new CompletionException(throwable);
		});

		icon.display().provide().offer(Keys.LORE, PARSER.parse(lore, PlaceholderSources.builder()
				.append(PlatformPlayer.class, () -> this.viewer)
				.build())
		);
	}

	public static class MainMenuCreator implements Creator {

		private PlatformPlayer viewer;

		@Override
		public Creator viewer(PlatformPlayer player) {
			this.viewer = player;
			return this;
		}

		@Override
		public SpongeMainPageProvider build() {
			return new SpongeMainMenu(this.viewer);
		}

	}
}
