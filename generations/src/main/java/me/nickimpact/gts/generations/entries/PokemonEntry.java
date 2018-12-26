package me.nickimpact.gts.generations.entries;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import com.pixelmonmod.pixelmon.util.helpers.SpriteHelper;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.GTSInfo;
import me.nickimpact.gts.api.json.Typing;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.entries.Minable;
import me.nickimpact.gts.configuration.ConfigKeys;
import me.nickimpact.gts.configuration.MsgConfigKeys;
import me.nickimpact.gts.entries.prices.MoneyPrice;
import me.nickimpact.gts.internal.TextParsingUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Typing("Pokemon")

public class PokemonEntry extends Entry<Pokemon> implements Minable {

	private static final PokemonSpec UNTRADABLE = new PokemonSpec("untradeable");

	public PokemonEntry() {
		super();
	}

	public PokemonEntry(EntityPixelmon pokemon, MoneyPrice price) {
		this(new Pokemon(pokemon), price);
	}

	public PokemonEntry(Pokemon pokemon, MoneyPrice price) {
		super(pokemon, price);
	}

	@Override
	public String getSpecsTemplate() {
		if(this.getEntry().getPokemon().isEgg) {
			return GTS.getInstance().getMsgConfig().get(MsgConfigKeys.POKEMON_ENTRY_SPEC_TEMPLATE_EGG);
		}
		return GTS.getInstance().getMsgConfig().get(MsgConfigKeys.POKEMON_ENTRY_SPEC_TEMPLATE);
	}

	@Override
	public List<String> getLogTemplate() {
		return GTS.getInstance().getMsgConfig().get(MsgConfigKeys.LOGS_ENTRIES_POKEMON);
	}

	@Override
	public String getName() {
		return this.getEntry().getPokemon().getName();
	}

	@Override
	public ItemStack baseItemStack(Player player, Listing listing) {
		ItemStack icon = getPicture(this.getEntry().getPokemon());
		Map<String, Object> variables = Maps.newHashMap();
		variables.put("listing", listing);

		icon.offer(Keys.DISPLAY_NAME, TextParsingUtils.fetchAndParseMsg(player, MsgConfigKeys.POKEMON_ENTRY_BASE_TITLE, null, variables));

		List<String> template = Lists.newArrayList();
		template.addAll(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.POKEMON_ENTRY_BASE_LORE));
		this.addLore(icon, template, player, listing, variables);

		return icon;
	}

	@Override
	public ItemStack confirmItemStack(Player player, Listing listing) {
		ItemStack icon = ItemStack.builder().itemType(ItemTypes.PAPER).build();
		Map<String, Object> variables = Maps.newHashMap();
		variables.put("listing", listing);

		icon.offer(Keys.DISPLAY_NAME, TextParsingUtils.fetchAndParseMsg(player, listing.getAucData() == null ? MsgConfigKeys.POKEMON_ENTRY_CONFIRM_TITLE : MsgConfigKeys.POKEMON_ENTRY_CONFIRM_TITLE_AUCTION, null, variables));

		List<String> template = Lists.newArrayList();
		template.addAll(GTS.getInstance().getMsgConfig().get(listing.getAucData() == null ? MsgConfigKeys.POKEMON_ENTRY_BASE_LORE : MsgConfigKeys.POKEMON_ENTRY_CONFIRM_LORE_AUCTION));
		this.addLore(icon, template, player, listing, variables);

		return icon;
	}

	private void addLore(ItemStack icon, List<String> template, Player player, Listing listing, Map<String, Object> variables) {
		for(EnumHidableDetail detail : EnumHidableDetail.values()) {
			if(detail.getCondition().test(this.getEntry().getPokemon())) {
				template.addAll(GTS.getInstance().getMsgConfig().get(detail.getField()));
			}
		}

		if(listing.getAucData() != null) {
			template.addAll(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.AUCTION_INFO));
		} else {
			template.addAll(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ENTRY_INFO));
		}

		List<Text> translated = template.stream().map(str -> TextParsingUtils.fetchAndParseMsg(player, str, null, variables)).collect(Collectors.toList());
		icon.offer(Keys.ITEM_LORE, translated);
	}

	@Override
	public boolean supportsOffline() {
		return false;
	}

	@Override
	public boolean giveEntry(User user) {
		Optional<PlayerStorage> optStorage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID(
				(MinecraftServer) Sponge.getServer(),
				user.getUniqueId()
		);

		if (!optStorage.isPresent())
			return false;

		optStorage.get().addToParty(this.getEntry().getPokemon());
		optStorage.get().sendUpdatedList();

		return true;
	}

	@Override
	public boolean doTakeAway(Player player) {
		if(BattleRegistry.getBattle((EntityPlayer) player) != null) {
			player.sendMessage(Text.of(GTSInfo.ERROR, TextColors.GRAY, "You are in battle, you can't sell any pokemon currently..."));
			return false;
		}

		if(UNTRADABLE.matches(this.getEntry().getPokemon())) {
			player.sendMessage(Text.of(GTSInfo.ERROR, TextColors.GRAY, "This pokemon is marked as untradeable, and cannot be sold..."));
			return false;
		}

		if(GTS.getInstance().getConfig().get(ConfigKeys.BLACKLISTED_POKEMON).stream().anyMatch(name -> name.equalsIgnoreCase(this.getEntry().getPokemon().getName()))){
			player.sendMessage(Text.of(GTSInfo.ERROR, TextColors.GRAY, "Sorry, but ", TextColors.YELLOW, this.getName(), TextColors.GRAY, " has been blacklisted from the GTS..."));
			return false;
		}

		PlayerStorage ps = PixelmonStorage.pokeBallManager.getPlayerStorage((EntityPlayerMP)player).orElse(null);
		if(ps == null)
			return false;

		ps.recallAllPokemon();
		ps.removeFromPartyPlayer(ps.getPosition(this.getEntry().getPokemon().getPokemonId()));
		ps.sendUpdatedList();

		return true;
	}

	private static ItemStack getPicture(EntityPixelmon pokemon) {
		net.minecraft.item.ItemStack item = new net.minecraft.item.ItemStack(PixelmonItems.itemPixelmonSprite);
		NBTTagCompound nbt = new NBTTagCompound();
		String idValue = String.format("%03d", pokemon.baseStats.nationalPokedexNumber);
		if (pokemon.isEgg) {
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
		} else if (pokemon.getIsShiny()) {
			nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/shinypokemon/" + idValue + SpriteHelper.getSpriteExtra(pokemon.getSpecies().name, pokemon.getForm()));
		} else {
			nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/pokemon/" + idValue + SpriteHelper.getSpriteExtra(pokemon.getSpecies().name, pokemon.getForm()));
		}

		item.setTagCompound(nbt);
		return (ItemStack) (Object) item;
	}

	@Override
	public MoneyPrice calcMinPrice() {
		MoneyPrice price = new MoneyPrice(GTS.getInstance().getConfig().get(ConfigKeys.MIN_PRICING_POKEMON_BASE));
		EntityPixelmon pokemon = this.getEntry().getPokemon();
		boolean isLegend = EnumPokemon.legendaries.contains(pokemon.getName());
		if (isLegend && pokemon.getIsShiny()) {
			price.add(new MoneyPrice(GTS.getInstance().getConfig().get(ConfigKeys.MIN_PRICING_POKEMON_LEGEND) + GTS.getInstance().getConfig().get(ConfigKeys.MIN_PRICING_POKEMON_SHINY)));
		} else if (isLegend) {
			price.add(new MoneyPrice(GTS.getInstance().getConfig().get(ConfigKeys.MIN_PRICING_POKEMON_LEGEND)));
		} else if (pokemon.getIsShiny()) {
			price.add(new MoneyPrice(GTS.getInstance().getConfig().get(ConfigKeys.MIN_PRICING_POKEMON_SHINY)));
		}

		for (int iv : pokemon.stats.IVs.getArray()) {
			if (iv >= GTS.getInstance().getConfig().get(ConfigKeys.MIN_PRICING_POKEMON_IVS_MINVAL)) {
				price.add(new MoneyPrice(GTS.getInstance().getConfig().get(ConfigKeys.MIN_PRICING_POKEMON_IVS_PRICE)));
			}
		}

		if (pokemon.getAbilitySlot() == 2) {
			price.add(new MoneyPrice(GTS.getInstance().getConfig().get(ConfigKeys.MIN_PRICING_POKEMON_HA)));
		}

		return price;
	}

	public enum LakeTrio {
		Mesprit(EnumPokemon.Mesprit),
		Azelf(EnumPokemon.Azelf),
		Uxie(EnumPokemon.Uxie);

		private EnumPokemon species;

		LakeTrio(EnumPokemon species) {
			this.species = species;
		}

		public static boolean isMember(EnumPokemon species) {
			for(LakeTrio guardian : values()) {
				if(guardian.species == species) {
					return true;
				}
			}

			return false;
		}
	}
}
