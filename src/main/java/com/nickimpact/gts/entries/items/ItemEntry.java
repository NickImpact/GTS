package com.nickimpact.gts.entries.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.GTSInfo;
import com.nickimpact.gts.api.commands.SpongeCommand;
import com.nickimpact.gts.api.commands.SpongeSubCommand;
import com.nickimpact.gts.api.commands.annotations.CommandAliases;
import com.nickimpact.gts.api.configuration.ConfigKey;
import com.nickimpact.gts.api.json.Typing;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.data.AuctionData;
import com.nickimpact.gts.api.listings.entries.Entry;
import com.nickimpact.gts.api.listings.pricing.Price;
import com.nickimpact.gts.configuration.ConfigKeys;
import com.nickimpact.gts.configuration.MsgConfigKeys;
import com.nickimpact.gts.entries.prices.MoneyPrice;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.item.inventory.adapter.impl.comp.HotbarAdapter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents an ItemStack that can be added as an entry into the GTS market. The generified typing
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

	private static Map<UUID, Integer> amounts = Maps.newHashMap();

	public ItemEntry() {
		super();
	}

	public ItemEntry(ItemStack element, Price price) {
		super(element.toContainer(), price);
		this.item = element;
	}

	private ItemStack decode() {
		return item != null ? item : (item = ItemStack.builder().fromContainer(this.element).build());
	}

	@Override
	public SpongeSubCommand commandSpec(boolean isAuction) {
		return new ItemSub(isAuction);
	}

	@Override
	public String getSpecsTemplate() {
		return "{{item_title}}";
	}

	@Override
	public String getName() {
		return this.decode().getTranslation().get();
	}

	@Override
	protected ItemStack baseItemStack(Player player) {
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
	protected ItemStack confirmItemStack(Player player) {
		return baseItemStack(player);
	}

	@Override
	public String confirmTitleTemplate() {
		return GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_CONFIRM_TITLE);
	}

	@Override
	protected List<String> confirmLoreTemplate() {
		List<String> lore = Lists.newArrayList();
		lore.addAll(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_CONFIRM_LORE));

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
		if(player.getInventory().query(Hotbar.class, GridInventory.class).size() == 36) {
			player.sendMessage(Text.of("Your inventory is full, so we can't award you this listing..."));
			return false;
		}

		player.getInventory().offer(this.decode());
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

	@CommandAliases("item")
	public class ItemSub extends SpongeSubCommand {

		private final Text argAmount = Text.of("amount");
		private final Text argPrice = Text.of("price");

		private final boolean isAuction;

		public ItemSub(boolean isAuction) {
			this.isAuction = isAuction;
		}

		@Override
		public CommandElement[] getArgs() {
			return new CommandElement[]{
					GenericArguments.integer(argAmount),
					GenericArguments.integer(argPrice)
			};
		}

		@Override
		public Text getDescription() {
			return Text.of("Handles items");
		}

		@Override
		public Text getUsage() {
			return Text.of("/gts sell item <amount> <price>");
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
				if(item.isPresent()) {
					int amount = args.<Integer>getOne(argAmount).get();
					if(amount >= item.get().getQuantity()) {
						amount = item.get().getQuantity();
					}
					ItemStack entry = ItemStack.builder().from(item.get()).quantity(amount).build();
					amounts.put(player.getUniqueId(), amount);
					Listing.Builder lb = Listing.builder()
							.player(player)
							.entry(new ItemEntry(entry, new MoneyPrice(args.<Integer>getOne(argPrice).get())))
							.doesExpire()
							.expiration(GTS.getInstance().getConfig().get(ConfigKeys.LISTING_TIME));

					if(isAuction) {
						lb = lb.auction();
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
