package me.nickimpact.gts.pixelmon.entries;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.util.helpers.SpriteHelper;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.GTSInfo;
import me.nickimpact.gts.api.json.Typing;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.entries.Minable;
import me.nickimpact.gts.api.time.Time;
import me.nickimpact.gts.configuration.ConfigKeys;
import me.nickimpact.gts.configuration.MsgConfigKeys;
import me.nickimpact.gts.entries.prices.MoneyPrice;
import me.nickimpact.gts.internal.TextParsingUtils;
import me.nickimpact.gts.pixelmon.ReforgedBridge;
import me.nickimpact.gts.pixelmon.config.PokemonConfigKeys;
import me.nickimpact.gts.pixelmon.config.PokemonMsgConfigKeys;
import me.nickimpact.gts.pixelmon.utils.GsonUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Typing("reforged")
public class ReforgedEntry extends Entry<String, Pokemon> implements Minable {

	private static final PokemonSpec UNTRADABLE = new PokemonSpec("untradeable");

	public ReforgedEntry() {
		super();
	}

	public ReforgedEntry(Pokemon pokemon, MoneyPrice price) {
		super(pokemon.writeToNBT(new NBTTagCompound()).toString(), price);
	}

	private transient Pokemon pokemon;

	public Pokemon getPokemon() {
		return this.decode();
	}

	@Override
	protected Pokemon handle() {
		return this.decode();
	}

	private Pokemon decode() {
		if (pokemon == null) {
			try {
				pokemon = Pixelmon.pokemonFactory.create(JsonToNBT.getTagFromJson(this.element));
			} catch (NBTException e) {
				pokemon = Pixelmon.pokemonFactory.create(GsonUtils.deserialize(this.element));
			}
		}

		return pokemon;
	}

	@Override
	public String getSpecsTemplate() {
		if (this.decode().isEgg()) {
			return ReforgedBridge.getInstance().getMsgConfig().get(PokemonMsgConfigKeys.POKEMON_ENTRY_SPEC_TEMPLATE_EGG);
		}
		return ReforgedBridge.getInstance().getMsgConfig().get(PokemonMsgConfigKeys.POKEMON_ENTRY_SPEC_TEMPLATE);
	}

	@Override
	public String getName() {
		return this.decode().getSpecies().getPokemonName();
	}

	@Override
	public ItemStack baseItemStack(Player player, Listing listing) {
		ItemStack icon = getPicture(this.decode());
		Map<String, Object> variables = Maps.newHashMap();
		variables.put("listing", listing);
		variables.put("pokemon", this.decode());

		icon.offer(Keys.DISPLAY_NAME, TextParsingUtils.fetchAndParseMsg(player, PokemonMsgConfigKeys.POKEMON_ENTRY_BASE_TITLE, null, variables));

		List<String> template = Lists.newArrayList();
		template.addAll(ReforgedBridge.getInstance().getMsgConfig().get(PokemonMsgConfigKeys.POKEMON_ENTRY_BASE_LORE));
		this.addLore(icon, template, player, listing, variables);

		return icon;
	}

	@Override
	public ItemStack confirmItemStack(Player player, Listing listing) {
		ItemStack icon = ItemStack.builder().itemType(ItemTypes.PAPER).build();
		Map<String, Object> variables = Maps.newHashMap();
		variables.put("listing", listing);
		variables.put("pokemon", this.decode());

		icon.offer(Keys.DISPLAY_NAME, TextParsingUtils.fetchAndParseMsg(player, listing.getAucData() == null ? PokemonMsgConfigKeys.POKEMON_ENTRY_CONFIRM_TITLE : PokemonMsgConfigKeys.POKEMON_ENTRY_CONFIRM_TITLE_AUCTION, null, variables));

		List<String> template = Lists.newArrayList();
		template.addAll(ReforgedBridge.getInstance().getMsgConfig().get(listing.getAucData() == null ? PokemonMsgConfigKeys.POKEMON_ENTRY_CONFIRM_LORE : PokemonMsgConfigKeys.POKEMON_ENTRY_CONFIRM_LORE_AUCTION));
		this.addLore(icon, template, player, listing, variables);

		return icon;
	}

	private void addLore(ItemStack icon, List<String> template, Player player, Listing listing, Map<String, Object> variables) {
		for (EnumHidableDetail detail : EnumHidableDetail.values()) {
			if (detail.getCondition().test(this.decode())) {
				template.addAll(ReforgedBridge.getInstance().getMsgConfig().get(detail.getField()));
			}
		}

		if (listing.getAucData() != null) {
			template.addAll(ReforgedBridge.getInstance().getMsgConfig().get(MsgConfigKeys.AUCTION_INFO));
		} else {
			template.addAll(ReforgedBridge.getInstance().getMsgConfig().get(MsgConfigKeys.ENTRY_INFO));
		}

		List<Text> translated = template.stream().map(str -> TextParsingUtils.fetchAndParseMsg(player, str, null, variables)).collect(Collectors.toList());
		icon.offer(Keys.ITEM_LORE, translated);
	}

	@Override
	public boolean supportsOffline() {
		return true;
	}

	@Override
	public boolean giveEntry(User user) {
		PlayerPartyStorage party = Pixelmon.storageManager.getParty(user.getUniqueId());
		party.add(this.decode());
		return true;
	}

	@Override
	public boolean doTakeAway(Player player) {
		if (BattleRegistry.getBattle((EntityPlayer) player) != null) {
			player.sendMessage(Text.of(GTSInfo.ERROR, TextColors.GRAY, "You are in battle, you can't sell any pokemon currently..."));
			return false;
		}

		if (UNTRADABLE.matches(this.decode())) {
			player.sendMessage(Text.of(GTSInfo.ERROR, TextColors.GRAY, "This pokemon is marked as untradeable, and cannot be sold..."));
			return false;
		}

		if (ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.BLACKLISTED).stream().anyMatch(name -> name.equalsIgnoreCase(this.decode().getSpecies().getPokemonName()))) {
			player.sendMessage(Text.of(GTSInfo.ERROR, TextColors.GRAY, "Sorry, but ", TextColors.YELLOW, this.getName(), TextColors.GRAY, " has been blacklisted from the GTS..."));
			return false;
		}

		PlayerPartyStorage party = Pixelmon.storageManager.getParty(player.getUniqueId());
		party.retrieveAll();
		party.set(party.getPosition(this.decode()), null);
		return true;
	}

	private static ItemStack getPicture(Pokemon pokemon) {
		net.minecraft.item.ItemStack item = new net.minecraft.item.ItemStack(PixelmonItems.itemPixelmonSprite);
		NBTTagCompound nbt = new NBTTagCompound();
		String idValue = String.format("%03d", pokemon.getBaseStats().nationalPokedexNumber);
		if (pokemon.isEgg()) {
			switch (pokemon.getSpecies()) {
				case Manaphy:
				case Togepi:
					nbt.setString(NbtKeys.SPRITE_NAME,
							String.format("pixelmon:sprites/eggs/%s1", pokemon.getSpecies().name.toLowerCase()));
					break;
				default:
					nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/egg1");
					break;
			}
		} else if (pokemon.isShiny()) {
			nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/shinypokemon/" + idValue + SpriteHelper.getSpriteExtra(pokemon.getSpecies().name, pokemon.getForm()));
		} else {
			nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/pokemon/" + idValue + SpriteHelper.getSpriteExtra(pokemon.getSpecies().name, pokemon.getForm()));
		}

		item.setTagCompound(nbt);
		return (ItemStack) (Object) item;
	}

	@Override
	public MoneyPrice calcMinPrice() {
		MoneyPrice price = new MoneyPrice(ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_BASE));
		Pokemon pokemon = this.decode();
		boolean isLegend = EnumSpecies.legendaries.contains(pokemon.getSpecies().getPokemonName());
		if (isLegend && pokemon.isShiny()) {
			price.add(new MoneyPrice(ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_LEGEND) + ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_SHINY)));
		} else if (isLegend) {
			price.add(new MoneyPrice(ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_LEGEND)));
		} else if (pokemon.isShiny()) {
			price.add(new MoneyPrice(ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_SHINY)));
		}

		for (int iv : pokemon.getStats().ivs.getArray()) {
			if (iv >= ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_IVS_MINVAL)) {
				price.add(new MoneyPrice(ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_IVS_PRICE)));
			}
		}

		if (pokemon.getAbilitySlot() == 2) {
			price.add(new MoneyPrice(ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_HA)));
		}

		return price;
	}

	public static CommandResult handleCommand(CommandSource src, String[] args) {
		if(args.length < 2) {
			return CommandResult.empty();
		}

		Player player = (Player) src;

		if(!player.hasPermission("gts.command.sell.pokemon")) {
			player.sendMessage(Text.of(TextColors.RED, "You don't have permission to use this command!"));
			return CommandResult.success();
		}

		int pos = Integer.parseInt(args[0]) - 1;
		BigDecimal price = new BigDecimal(Double.parseDouble(args[1]));
		Time time = null;
		if(args.length == 3) {
			time = new Time(args[2]);
			if(time.getTime() > GTS.getInstance().getConfig().get(ConfigKeys.LISTING_MAX_TIME)) {
				time = new Time(GTS.getInstance().getConfig().get(ConfigKeys.LISTING_MAX_TIME).longValue());
			}
		}

		if(price.signum() <= 0) {
			player.sendMessage(Text.of(TextColors.RED, "Invalid price! Price values must be positive!"));
			return CommandResult.empty();
		}

		PlayerPartyStorage storage = Pixelmon.storageManager.getParty(player.getUniqueId());
		Pokemon pokemon = storage.get(pos);
		if(pokemon == null) {
			player.sendMessage(Text.of(TextColors.RED, "Unable to find a pokemon in the specified slot..."));
			return CommandResult.success();
		}

		if(storage.getTeam().size() == 1) {
			if(!pokemon.isEgg()) {
				player.sendMessage(Text.of(TextColors.RED, "You can't sell your last non-egg party member..."));
				return CommandResult.success();
			}
		}

		MoneyPrice mp = new MoneyPrice(price);
		if(!mp.isLowerOrEqual()) {
			player.sendMessage(Text.of(TextColors.RED, "Your request is above the max amount of ", new MoneyPrice(mp.getMax()).getText()));
			return CommandResult.success();
		}

		Listing listing = Listing.builder()
				.player(player)
				.entry(new ReforgedEntry(pokemon, mp))
				.doesExpire()
				.expiration(time != null ? time.getTime() : GTS.getInstance().getConfig().get(ConfigKeys.LISTING_TIME))
				.build();
		listing.publish(player);

		return CommandResult.success();
	}
}
