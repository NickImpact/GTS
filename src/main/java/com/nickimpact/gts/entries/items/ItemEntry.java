package com.nickimpact.gts.entries.items;

import com.google.common.collect.Lists;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.GTSInfo;
import com.nickimpact.gts.api.commands.SpongeCommand;
import com.nickimpact.gts.api.commands.SpongeSubCommand;
import com.nickimpact.gts.api.commands.annotations.CommandAliases;
import com.nickimpact.gts.api.json.Typing;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.entries.Entry;
import com.nickimpact.gts.api.listings.pricing.Price;
import com.nickimpact.gts.configuration.ConfigKeys;
import com.nickimpact.gts.entries.prices.MoneyPrice;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;
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
	public SpongeSubCommand commandSpec() {
		return new ItemSub();
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
		return "{{item_title}}";
	}

	@Override
	protected List<String> baseLoreTemplate() {
		List<String> output = Lists.newArrayList(
				"&7Listing ID: &e{{id}}",
				"&7Seller: &e{{seller}}",
				"&7Price: &e{{price}}",
				"&7Time Left: &e{{time_left}}"
		);

		this.decode().get(Keys.ITEM_LORE).ifPresent(lore -> {
			if(lore.size() > 0) {
				GTS.getInstance().getConsole().ifPresent(console -> console.sendMessages(
						Text.of(GTSInfo.DEBUG_PREFIX, "Item Lore:")
				));
				for(Text line : lore)
					GTS.getInstance().getConsole().ifPresent(console -> console.sendMessages(
							Text.of(GTSInfo.DEBUG_PREFIX, line)
					));

				output.add("&aItem Lore:");
				output.addAll(lore.stream().map(Text::toPlain).collect(Collectors.toList()));
			}
		});

		return output;
	}

	@Override
	protected ItemStack confirmItemStack(Player player) {
		return baseItemStack(player);
	}

	@Override
	public String confirmTitleTemplate() {
		return "&ePurchase {{item_title}}?";
	}

	@Override
	protected List<String> confirmLoreTemplate() {
		List<String> output = Lists.newArrayList(
				"&7Listing ID: &e{{id}}",
				"&7Seller: &e{{seller}}",
				"&7Price: &e{{price}}"
		);

		this.decode().get(Keys.ITEM_LORE).ifPresent(lore -> {
			if(lore.size() > 0) {
				GTS.getInstance().getConsole().ifPresent(console -> console.sendMessages(
						Text.of(GTSInfo.DEBUG_PREFIX, "Item Lore:")
				));
				for(Text line : lore)
					GTS.getInstance().getConsole().ifPresent(console -> console.sendMessages(
							Text.of(GTSInfo.DEBUG_PREFIX, line)
					));

				output.add("&aItem Lore:");
				output.addAll(lore.stream().map(Text::toPlain).collect(Collectors.toList()));
			}
		});

		return output;
	}

	@Override
	public boolean supportsOffline() {
		return false;
	}

	@Override
	public boolean giveEntry(User user) {
		// User will always be a player here due to the offline support check
		Player player = (Player)user;
		if(player.getInventory().query(Hotbar.class, GridInventory.class).size() == 36)
			return false;

		player.getInventory().offer(this.decode());
		return true;
	}

	@Override
	public boolean doTakeAway(Player player) {
		Optional<ItemStack> opt = player.getInventory()
				.query(
						QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class),
						QueryOperationTypes.INVENTORY_TYPE.of(GridInventory.class)
				)
				.query(QueryOperationTypes.ITEM_STACK_IGNORE_QUANTITY.of(this.decode()))
				.poll(this.decode().getQuantity());

		return opt.isPresent();
	}

	@CommandAliases("item")
	public class ItemSub extends SpongeSubCommand {

		private final Text argSlot = Text.of("invSlot");
		private final Text argAmount = Text.of("amount");
		private final Text argPrice = Text.of("price");

		@Override
		public CommandElement[] getArgs() {
			return new CommandElement[]{
					GenericArguments.integer(argSlot),
					GenericArguments.integer(argAmount),
					GenericArguments.integer(argPrice)
			};
		}

		@Override
		public Text getDescription() {
			return Text.of("Handles item entries for the GTS");
		}

		@Override
		public Text getUsage() {
			return Text.of("/gts sell item <inv slot> <amount> <price>");
		}

		@Override
		public SpongeCommand[] getSubCommands() {
			return new SpongeCommand[0];
		}

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if(src instanceof Player) {
				Player player = (Player)src;
				int invSlot = args.<Integer>getOne(argSlot).orElse(1) - 1;
				int price = args.<Integer>getOne(argPrice).get();
				if(price <= 0) {
					throw new CommandException(Text.of("Price must be a positive integer!"));
				}

				Optional<ItemStack> item = player.getInventory()
						.query(
								QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class),
								QueryOperationTypes.INVENTORY_TYPE.of(GridInventory.class)
						)
						.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(invSlot)))
						.peek(args.<Integer>getOne(argAmount).orElse(1));

				if(item.isPresent()) {
					Listing.builder()
							.player(player)
							.entry(new ItemEntry(item.get(), new MoneyPrice(args.<Integer>getOne(argPrice).get())))
							.doesExpire()
							.expiration(GTS.getInstance().getConfig().get(ConfigKeys.LISTING_TIME))
							.build();

					return CommandResult.success();
				}

				throw new CommandException(Text.of("Unable to find an item in the specified inventory position..."));
			}

			throw new CommandException(Text.of("Only players may use this command..."));
		}
	}
}
