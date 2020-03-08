package me.nickimpact.gts.listings;

import com.google.common.collect.Maps;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeLayout;
import com.nickimpact.impactor.sponge.ui.SpongeUI;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.sponge.SpongeListing;
import me.nickimpact.gts.sponge.SpongePlugin;
import me.nickimpact.gts.sponge.TextParsingUtils;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class SpongeItemUI implements EntryUI<Player> {

	private SpongeUI display;
	private Player viewer;
	private ItemStack selection;

	private int size = 1;

	private double amount = 1;

	public SpongeItemUI() {}

	private SpongeItemUI(Player player) {
		this.viewer = player;
		this.display = createUI();
		this.display.define(this.forgeDisplay());
	}

	@Override
	public EntryUI createFor(Player player) {
		return new SpongeItemUI(player);
	}

	@Override
	public SpongeUI getDisplay() {
		return this.display;
	}

	private SpongeUI createUI() {
		return (SpongeUI) SpongeUI.builder()
				.title(GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, MsgConfigKeys.UI_TITLES_ITEMS, null, null))
				.dimension(InventoryDimension.of(9, 6))
				.build()
				.attachListener((pl, event) -> {
					event.getTransactions().forEach(transaction -> {
						transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
							if(slot.getValue() >= 54) {
								if(!transaction.getOriginal().getType().equals(ItemTypes.AIR)) {
									ItemStack clicked = transaction.getOriginal().createStack();
									if (this.selection != null && this.selection.equalTo(clicked)) {
										return;
									}

									this.selection = clicked;
									if(this.size > this.selection.getQuantity()) {
										this.size = this.selection.getQuantity();
									}
									this.display.setSlot(13, new SpongeIcon(clicked));
									this.display.setSlot(30, this.moneyIcon());
									this.display.setSlot(48, this.amountIcon());
								}
							}
						});
					});
				});
	}

	private SpongeLayout forgeDisplay() {
		SpongeLayout.SpongeLayoutBuilder slb = SpongeLayout.builder();

		slb.rows(SpongeIcon.BORDER, 0, 2);
		slb.slots(SpongeIcon.BORDER, 9, 17, 34, 43, 52);

		slb.slot(new SpongeIcon(
				ItemStack.builder()
						.itemType(ItemTypes.BARRIER)
						.add(Keys.DISPLAY_NAME, GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, MsgConfigKeys.UI_ITEMS_SELLOPTION_NOT_SELECTED, null, null))
						.build()
		), 13);

		slb.slot(this.moneyDecIcon(), 28);
		slb.slot(this.moneyIcon(), 30);
		slb.slot(this.moneyIncIcon(), 32);

		slb.slot(this.amountDecIcon(), 46);
		slb.slot(this.amountIcon(), 48);
		slb.slot(this.amountIncIcon(), 50);

		TextParsingUtils parser = GTS.getInstance().getTextParsingUtils();

		SpongeIcon confirm = new SpongeIcon(ItemStack.builder().itemType(ItemTypes.DYE)
				.add(Keys.DISPLAY_NAME, parser.fetchAndParseMsg(this.viewer, MsgConfigKeys.CONFIRM_SELECTION, null, null))
				.add(Keys.DYE_COLOR, DyeColors.LIME)
				.build()
		);
		confirm.addListener(clickable -> {
			if(this.selection != null) {
				this.display.close(clickable.getPlayer());

				SpongeListing listing = SpongeListing.builder()
						.entry(new SpongeItemEntry(this.selection))
						.price(this.amount)
						.id(UUID.randomUUID())
						.owner(clickable.getPlayer().getUniqueId())
						.expiration(LocalDateTime.now().plusSeconds(GTS.getInstance().getConfiguration().get(ConfigKeys.LISTING_TIME)))
						.build();

				listing.publish(PluginInstance.getInstance(), clickable.getPlayer().getUniqueId());
			}
		});
		slb.slot(confirm, 35);

		SpongeIcon cancel = new SpongeIcon(ItemStack.builder().itemType(ItemTypes.DYE)
				.add(Keys.DISPLAY_NAME, parser.fetchAndParseMsg(this.viewer, MsgConfigKeys.CANCEL, null, null))
				.add(Keys.DYE_COLOR, DyeColors.RED)
				.build()
		);
		cancel.addListener(clickable -> {
			this.display.close(clickable.getPlayer());
		});
		slb.slot(cancel, 53);

		return slb.build();
	}

	private SpongeIcon moneyIncIcon() {
		Config config = GTS.getInstance().getConfig();
		TextParsingUtils parser = GTS.getInstance().getTextParsingUtils();
		ItemStack inc = ItemStack.builder()
				.itemType(ItemTypes.DYE)
				.add(Keys.DYE_COLOR, DyeColors.LIME)
				.add(Keys.DISPLAY_NAME, ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils().fetchAndParseMsg(
						viewer, MsgConfigKeys.BUTTONS_INCREASE_CURRENCY_TITLE, null, null
				))
				.add(Keys.ITEM_LORE,
						parser.fetchAndParseMsgs(this.viewer, MsgConfigKeys.BUTTONS_INCREASE_CURRENCY_LORE, this.getCurrencyTokens(), null)
				)
				.build();
		SpongeIcon icon = new SpongeIcon(inc);
		icon.addListener(clickable -> {
			ClickInventoryEvent event = clickable.getEvent();
			if(event instanceof ClickInventoryEvent.Shift) {
				if(event instanceof ClickInventoryEvent.Shift.Secondary) {
					this.amount = Math.min(PluginInstance.getInstance().getConfiguration().get(ConfigKeys.MAX_MONEY_PRICE), this.amount + config.get(ConfigKeys.PRICING_RIGHTCLICK_SHIFT));
				} else {
					this.amount = Math.min(PluginInstance.getInstance().getConfiguration().get(ConfigKeys.MAX_MONEY_PRICE), this.amount + config.get(ConfigKeys.PRICING_LEFTCLICK_SHIFT));
				}
			} else {
				if(event instanceof ClickInventoryEvent.Secondary) {
					this.amount = Math.min(PluginInstance.getInstance().getConfiguration().get(ConfigKeys.MAX_MONEY_PRICE), this.amount + config.get(ConfigKeys.PRICING_RIGHTCLICK_BASE));
				} else {
					this.amount = Math.min(PluginInstance.getInstance().getConfiguration().get(ConfigKeys.MAX_MONEY_PRICE), this.amount + config.get(ConfigKeys.PRICING_LEFTCLICK_BASE));
				}
			}
			this.display.setSlot(30, this.moneyIcon());
		});

		return icon;
	}

	private SpongeIcon moneyIcon() {
		Config msgConfig = PluginInstance.getInstance().getMsgConfig();
		TextParsingUtils parser = ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils();
		ItemStack inc = ItemStack.builder()
				.itemType(ItemTypes.GOLD_INGOT)
				.add(Keys.DISPLAY_NAME, ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils().fetchAndParseMsg(
						viewer, PluginInstance.getInstance().getMsgConfig(), MsgConfigKeys.PRICE_DISPLAY_TITLE, null, null
				))
				.add(Keys.ITEM_LORE,
						parser.fetchAndParseMsgs(this.viewer, msgConfig, MsgConfigKeys.PRICE_DISPLAY_LORE, this.getCurrencyTokens(), null)
				)
				.build();
		return new SpongeIcon(inc);
	}

	private SpongeIcon moneyDecIcon() {
		Config config = GTS.getInstance().getConfig();
		TextParsingUtils parser = GTS.getInstance().getTextParsingUtils();
		ItemStack inc = ItemStack.builder()
				.itemType(ItemTypes.DYE)
				.add(Keys.DYE_COLOR, DyeColors.RED)
				.add(Keys.DISPLAY_NAME, ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils().fetchAndParseMsg(
						viewer, MsgConfigKeys.BUTTONS_DECREASE_CURRENCY_TITLE, null, null
				))
				.add(Keys.ITEM_LORE,
						parser.fetchAndParseMsgs(this.viewer, MsgConfigKeys.BUTTONS_DECREASE_CURRENCY_LORE, this.getCurrencyTokens(), null)
				)
				.build();
		SpongeIcon icon = new SpongeIcon(inc);
		icon.addListener(clickable -> {
			ClickInventoryEvent event = clickable.getEvent();
			if(event instanceof ClickInventoryEvent.Shift) {
				if(event instanceof ClickInventoryEvent.Shift.Secondary) {
					this.amount = Math.max(1, this.amount - config.get(ConfigKeys.PRICING_RIGHTCLICK_SHIFT));
				} else {
					this.amount = Math.max(1, this.amount - config.get(ConfigKeys.PRICING_LEFTCLICK_SHIFT));
				}
			} else {
				if(event instanceof ClickInventoryEvent.Secondary) {
					this.amount = Math.max(1, this.amount - config.get(ConfigKeys.PRICING_RIGHTCLICK_BASE));
				} else {
					this.amount = Math.max(1, this.amount - config.get(ConfigKeys.PRICING_LEFTCLICK_BASE));
				}
			}
			this.display.setSlot(30, this.moneyIcon());
		});

		return icon;
	}

	private Map<String, Function<CommandSource, Optional<Text>>> getCurrencyTokens() {
		EconomyService economy = GTS.getInstance().getEconomy();
		Config config = GTS.getInstance().getConfiguration();

		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		tokens.put("gts_button_currency_left_click", src -> Optional.of(economy.getDefaultCurrency().format(new BigDecimal(config.get(ConfigKeys.PRICING_LEFTCLICK_BASE)))));
		tokens.put("gts_button_currency_right_click", src -> Optional.of(economy.getDefaultCurrency().format(new BigDecimal(config.get(ConfigKeys.PRICING_RIGHTCLICK_BASE)))));
		tokens.put("gts_button_currency_shift_left_click", src -> Optional.of(economy.getDefaultCurrency().format(new BigDecimal(config.get(ConfigKeys.PRICING_LEFTCLICK_SHIFT)))));
		tokens.put("gts_button_currency_shift_right_click", src -> Optional.of(economy.getDefaultCurrency().format(new BigDecimal(config.get(ConfigKeys.PRICING_RIGHTCLICK_SHIFT)))));
		tokens.put("gts_min_price", src -> Optional.of(economy.getDefaultCurrency().format(new BigDecimal(1))));
		tokens.put("gts_max_price", src -> Optional.of(economy.getDefaultCurrency().format(new BigDecimal(config.get(ConfigKeys.MAX_MONEY_PRICE)))));
		tokens.put("gts_price", src -> Optional.of(economy.getDefaultCurrency().format(new BigDecimal(this.amount))));

		return tokens;
	}

	@SuppressWarnings("DuplicateExpressions")
	private Map<String, Function<CommandSource, Optional<Text>>> getAmountTokens() {
		TextParsingUtils parser = GTS.getInstance().getTextParsingUtils();
		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		tokens.put("gts_button_amount_left_click", src -> Optional.of(Text.of(1)));
		tokens.put("gts_button_amount_right_click", src -> Optional.of(Text.of(5)));
		tokens.put("gts_button_amount_shift_left_click", src -> Optional.of(Text.of(10)));
		tokens.put("gts_button_amount_shift_right_click", src -> Optional.of(parser.fetchAndParseMsg(viewer, MsgConfigKeys.ITEMS_MAX_STACK, null, null)));
		tokens.put("gts_min_amount", src -> Optional.of(Text.of(1)));
		tokens.put("gts_max_amount", src -> Optional.of(Text.of(this.selection == null ? 64 : this.selection.getMaxStackQuantity())));
		tokens.put("gts_amount", src -> Optional.of(Text.of(this.size)));

		return tokens;
	}

	private SpongeIcon amountIncIcon() {
		TextParsingUtils parser = GTS.getInstance().getTextParsingUtils();

		SpongeIcon icon = new SpongeIcon(ItemStack.builder()
				.itemType(ItemTypes.DYE)
				.add(Keys.DYE_COLOR, DyeColors.LIME)
				.add(Keys.DISPLAY_NAME, ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils().fetchAndParseMsg(
						viewer, MsgConfigKeys.BUTTONS_INCREASE_AMOUNT_TITLE, this.getAmountTokens(), null
				))
				.add(Keys.ITEM_LORE,
						parser.fetchAndParseMsgs(this.viewer, MsgConfigKeys.BUTTONS_INCREASE_AMOUNT_LORE, this.getAmountTokens(), null)
				)
				.build());
		icon.addListener(clickable -> {
			int max = this.selection != null ? this.selection.getQuantity() : 64;

			if(clickable.getEvent() instanceof ClickInventoryEvent.Shift) {
				if(clickable.getEvent() instanceof ClickInventoryEvent.Shift.Primary) {
					this.size = Math.min(max, this.size + 10);
				} else {
					this.size = max;
				}
			} else if (clickable.getEvent() instanceof ClickInventoryEvent.Primary) {
				this.size = Math.min(max, this.size + 1);
			} else if(clickable.getEvent() instanceof ClickInventoryEvent.Secondary) {
				this.size = Math.min(max, this.size + 5);
			}
			this.display.setSlot(48, this.amountIcon());
		});
		return icon;
	}

	private SpongeIcon amountIcon() {
		TextParsingUtils parser = GTS.getInstance().getTextParsingUtils();

		return new SpongeIcon(ItemStack.builder()
				.itemType(ItemTypes.PAPER)
				.add(Keys.DISPLAY_NAME, parser.fetchAndParseMsg(this.viewer, MsgConfigKeys.AMOUNT_DISPLAY_TITLE, null, null))
				.add(Keys.ITEM_LORE, parser.fetchAndParseMsgs(this.viewer, MsgConfigKeys.AMOUNT_DISPLAY_LORE, this.getAmountTokens(), null))
				.build());
	}

	private SpongeIcon amountDecIcon() {
		TextParsingUtils parser = GTS.getInstance().getTextParsingUtils();

		SpongeIcon icon = new SpongeIcon(ItemStack.builder()
				.itemType(ItemTypes.DYE)
				.add(Keys.DYE_COLOR, DyeColors.RED)
				.add(Keys.DISPLAY_NAME, ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils().fetchAndParseMsg(
						viewer, MsgConfigKeys.BUTTONS_DECREASE_AMOUNT_TITLE, this.getAmountTokens(), null
				))
				.add(Keys.ITEM_LORE,
						parser.fetchAndParseMsgs(this.viewer, MsgConfigKeys.BUTTONS_DECREASE_AMOUNT_LORE, this.getAmountTokens(), null)
				)
				.build());
		icon.addListener(clickable -> {
			if(clickable.getEvent() instanceof ClickInventoryEvent.Shift) {
				if(clickable.getEvent() instanceof ClickInventoryEvent.Shift.Primary) {
					this.size = Math.max(1, this.size - 10);
				} else {
					this.size = 1;
				}
			} else if (clickable.getEvent() instanceof ClickInventoryEvent.Primary) {
				this.size = Math.max(1, this.size - 1);
			} else if(clickable.getEvent() instanceof ClickInventoryEvent.Secondary) {
				this.size = Math.max(1, this.size - 5);
			}
			this.display.setSlot(48, this.amountIcon());
		});
		return icon;
	}
}
