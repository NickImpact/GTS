package com.nickimpact.gts.entries.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.GTSInfo;
import com.nickimpact.gts.api.json.Typing;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.entries.Entry;
import com.nickimpact.gts.api.listings.pricing.Price;
import com.nickimpact.gts.configuration.ConfigKeys;
import com.nickimpact.gts.configuration.MsgConfigKeys;
import com.nickimpact.gts.entries.prices.MoneyPrice;
import com.nickimpact.gts.utils.ListingUtils;
import com.nickimpact.impactor.api.commands.SpongeCommand;
import com.nickimpact.impactor.api.commands.SpongeSubCommand;
import com.nickimpact.impactor.api.commands.annotations.Aliases;
import com.nickimpact.impactor.api.commands.annotations.Permission;
import com.nickimpact.impactor.api.plugins.SpongePlugin;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.InventoryTransformation;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents an ItemStack that can be added as an element into the GTS market. The generified typing
 * here represents the lower-level phase of the ItemStack, as an ItemStack in a generic setup
 * like such produces a MalformedParameterizedTypeException, prevention serialization. Luckily
 * for us, Sponge provides a way to go from this low-level back up, so we can simply store the
 * data, then cache the ItemStack once it is recreated.
 *
 * @author NickImpact
 */
@Typing("Item")
public class ItemEntry extends Entry<DataContainer> {

	/** The cached version of the ItemStack */
	private transient ItemStack item;

	private String name;

	private static Map<UUID, Integer> amounts = Maps.newHashMap();

	public ItemEntry() {
		super();
	}

	public ItemEntry(ItemStack element, Price price) {
		super(element.toContainer(), price);
		this.item = element;
		this.name = element.getTranslation().get();
	}

	private ItemStack decode() {
		return item != null ? item : (item = ItemStack.builder().fromContainer(this.element).build());
	}

	@Override
	public SpongeSubCommand commandSpec(boolean isAuction) {
		return new ItemSub(GTS.getInstance(), isAuction);
	}

	@Override
	public String getSpecsTemplate() {
		return GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_SPEC_TEMPLATE);
	}

	@Override
	public List<String> getLogTemplate() {
		return Lists.newArrayList();
	}

	@Override
	public String getName() {
		if(name == null) {
			ItemStack item = this.decode();
			return item.get(Keys.DISPLAY_NAME).orElse(Text.of(item.getTranslation().get())).toPlain();
		} else {
			return name;
		}
	}

	@Override
	protected ItemStack baseItemStack() {
		return ItemStack.builder()
				.itemType(this.decode().getType())
				.quantity(this.decode().getQuantity())
				.add(Keys.ITEM_ENCHANTMENTS, this.decode().get(Keys.ITEM_ENCHANTMENTS).orElse(Lists.newArrayList()))
				.build();
	}

	@Override
	protected String baseTitleTemplate() {
		return GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_BASE_TITLE);
	}

	@Override
	protected List<String> baseLoreTemplate(boolean auction) {
		List<String> lore = Lists.newArrayList();
		lore.addAll(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_BASE_LORE));

		this.decode().get(Keys.ITEM_LORE).ifPresent(l -> {
			if(l.size() > 0) {
				lore.add("&aItem Lore:");
				lore.addAll(l.stream().map(Text::toPlain).collect(Collectors.toList()));
			}
		});

		if(auction) {
			lore.addAll(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.AUCTION_INFO));
		} else {
			lore.addAll(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ENTRY_INFO));
		}

		return lore;
	}

	@Override
	protected ItemStack confirmItemStack() {
		return ItemStack.builder().itemType(ItemTypes.PAPER).build();
	}

	@Override
	protected String confirmTitleTemplate(boolean auction) {
		return !auction ? GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_CONFIRM_TITLE) :
				GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_CONFIRM_TITLE_AUCTION);
	}

	@Override
	protected List<String> confirmLoreTemplate(boolean auction) {
		List<String> lore = Lists.newArrayList();
		if(!auction) {
			lore.addAll(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_CONFIRM_LORE));
		} else {
			lore.addAll(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_CONFIRM_LORE_AUCTION));
		}

		this.decode().get(Keys.ITEM_LORE).ifPresent(l -> {
			if(l.size() > 0) {
				lore.add("&aItem Lore:");
				lore.addAll(l.stream().map(Text::toPlain).collect(Collectors.toList()));
			}
		});

		return lore;
	}

	@Override
	public boolean supportsOffline() {
		return false;
	}

	@Override
	public boolean giveEntry(User user) {
		// User will always be a player here due to the offline support check
		Player player = (Player)user;
		if(player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class)).size() == 36) {
			player.sendMessage(Text.of("Your inventory is full, so we can't award you this listing..."));
			return false;
		}

		player.getInventory().transform(InventoryTransformation.of(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class), QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class))).offer(this.decode());
		return true;
	}

	@Override
	public boolean doTakeAway(Player player) {
		Optional<ItemStack> item = player.getItemInHand(HandTypes.MAIN_HAND);
		if(item.isPresent()) {
			if(!GTS.getInstance().getConfig().get(ConfigKeys.CUSTOM_NAME_ALLOWED)) {
				if(item.get().get(Keys.DISPLAY_NAME).isPresent()) {
					return false;
				}
			}

			int amount = amounts.get(player.getUniqueId());
			item.get().setQuantity(item.get().getQuantity() - amount);
			player.setItemInHand(HandTypes.MAIN_HAND, item.get());
			return true;
		}
		return false;
	}

	@Aliases("item")
	@Permission(prefix = "sell")
	public class ItemSub extends SpongeSubCommand {

		private final Text argAmount = Text.of("amount");
		private final Text argPrice = Text.of("price");

		private final boolean isAuction;
		private final Text argIncrement = Text.of("increment");

		public ItemSub(SpongePlugin plugin, boolean isAuction) {
			super(plugin);
			this.isAuction = isAuction;
		}

		@Override
		public CommandElement[] getArgs() {
			return new CommandElement[]{
					GenericArguments.integer(argAmount),
					GenericArguments.integer(argPrice),
					GenericArguments.optional(GenericArguments.doubleNum(argIncrement))
			};
		}

		@Override
		public Text getDescription() {
			return Text.of("Handles items");
		}

		@Override
		public Text getUsage() {
			return Text.of("/gts sell/auc item <amount> <price> (increment - auctions only)");
		}

		@Override
		public SpongeCommand[] getSubCommands() {
			return new SpongeCommand[0];
		}

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if(!GTS.getInstance().getConfig().get(ConfigKeys.ITEMS_ENABLED)) {
				throw new CommandException(Text.of("The selling of items is disabled..."));
			}

			if(src instanceof Player) {
				Player player = (Player)src;
				int price = args.<Integer>getOne(argPrice).get();
				if(price <= 0) {
					throw new CommandException(Text.of("Price must be a positive integer!"));
				}

				Optional<ItemStack> item = player.getItemInHand(HandTypes.MAIN_HAND);
				if(item.isPresent() && !item.get().equalTo(ItemStack.empty())) {
					int amount = args.<Integer>getOne(argAmount).get();
					if(amount < 1) {
						throw new CommandException(Text.of("Amount must be positive"));
					}

					if(amount > item.get().getQuantity()) {
						player.sendMessage(Text.of(GTSInfo.WARNING, TextColors.GRAY, "The quantity specified is too high, lowering to the amount you possess..."));
						amount = item.get().getQuantity();
					}
					ItemStack entry = ItemStack.builder().from(item.get()).quantity(amount).build();
					amounts.put(player.getUniqueId(), amount);
					Listing.Builder lb = Listing.builder()
							.player(player)
							.entry(new ItemEntry(entry, new MoneyPrice(args.<Integer>getOne(argPrice).get())))
							.doesExpire()
							.expiration(
									!isAuction ? GTS.getInstance().getConfig().get(ConfigKeys.LISTING_TIME) :
											GTS.getInstance().getConfig().get(ConfigKeys.AUC_TIME)
							);

					if(isAuction) {
						Optional<Double> optInc = args.getOne(argIncrement);
						if(!optInc.isPresent()) {
							throw new CommandException(Text.of("You must supply an increment..."));
						}
						MoneyPrice increment = new MoneyPrice(optInc.get());
						if(increment.getPrice().compareTo(new BigDecimal(0)) < 0) {
							throw new CommandException(Text.of("Increment must be a positive value..."));
						}
						lb = lb.auction(increment);
					}

					lb.build();
					return CommandResult.success();
				}

				throw new CommandException(Text.of("Unable to find an item in the specified inventory position..."));
			}

			throw new CommandException(Text.of("Only players may use this command..."));
		}
	}
}
