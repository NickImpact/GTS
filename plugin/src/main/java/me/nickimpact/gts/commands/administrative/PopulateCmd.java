package me.nickimpact.gts.commands.administrative;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.commands.SpongeCommand;
import com.nickimpact.impactor.api.commands.annotations.Aliases;
import com.nickimpact.impactor.api.commands.annotations.Permission;
import com.nickimpact.impactor.api.plugins.SpongePlugin;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.entries.items.ItemEntry;
import me.nickimpact.gts.entries.prices.MoneyPrice;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Aliases({"populate"})
@Permission(admin = true)
public class PopulateCmd extends SpongeCommand {

	private static final Text TYPE = Text.of("type");
	private static final Text AMOUNT = Text.of("amount");

	public PopulateCmd() {
		super(GTS.getInstance());
	}

	@Override
	public CommandElement[] getArgs() {
		return new CommandElement[] {
				GenericArguments.integer(AMOUNT)
		};
	}

	@Override
	public Text getDescription() {
		return null;
	}

	@Override
	public Text getUsage() {
		return null;
	}

	@Override
	public SpongeCommand[] getSubCommands() {
		return new SpongeCommand[0];
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		int amount = args.<Integer>getOne(AMOUNT).get();
		Collection<ItemType> types = Sponge.getRegistry().getAllOf(ItemType.class);
		List<ItemType> list = Lists.newArrayList(types);
		Random rng = new Random();
		for(int i = 0; i < amount; ++i) {
			ItemType type = list.get(rng.nextInt(list.size()));
			ItemStack item = ItemStack.builder().itemType(type).build();
			Listing listing = Listing.builder()
					.player(UUID.randomUUID(), "Console")
					.entry(new ItemEntry(item, new MoneyPrice(new BigDecimal(5000))))
					.doesExpire()
					.expiration(600)
					.build();
			listing.publish((Player) src);
		}

		return CommandResult.success();
	}
}
