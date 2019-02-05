package me.nickimpact.gts.pixelmon.entries.removable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import me.nickimpact.gts.GTSInfo;
import me.nickimpact.gts.api.json.Typing;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.entries.Minable;
import me.nickimpact.gts.configuration.MsgConfigKeys;
import me.nickimpact.gts.pixelmon.ReforgedBridge;
import me.nickimpact.gts.pixelmon.config.PokemonConfigKeys;
import me.nickimpact.gts.pixelmon.config.PokemonMsgConfigKeys;
import me.nickimpact.gts.entries.prices.MoneyPrice;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.util.helpers.SpriteHelper;
import me.nickimpact.gts.internal.TextParsingUtils;
import me.nickimpact.gts.pixelmon.entries.EnumHidableDetail;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
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
public class PokemonEntry extends Entry<Pokemon, com.pixelmonmod.pixelmon.api.pokemon.Pokemon> implements Minable {

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
	protected com.pixelmonmod.pixelmon.api.pokemon.Pokemon handle() {
		return this.element.getPokemon();
	}

	@Override
	public String getSpecsTemplate() {
		if(this.getEntry().isEgg()) {
			return ReforgedBridge.getInstance().getMsgConfig().get(PokemonMsgConfigKeys.POKEMON_ENTRY_SPEC_TEMPLATE_EGG);
		}
		return ReforgedBridge.getInstance().getMsgConfig().get(PokemonMsgConfigKeys.POKEMON_ENTRY_SPEC_TEMPLATE);
	}

	@Override
	public String getName() {
		return this.getEntry().getDisplayName();
	}

	@Override
	public ItemStack baseItemStack(Player player, Listing listing) {
		ItemStack icon = getPicture(this.getEntry());
		Map<String, Object> variables = Maps.newHashMap();
		variables.put("listing", listing);
		variables.put("pokemon", this.element.getPokemon());

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
		variables.put("pokemon", this.element.getPokemon());

		icon.offer(Keys.DISPLAY_NAME, TextParsingUtils.fetchAndParseMsg(player, listing.getAucData() == null ? PokemonMsgConfigKeys.POKEMON_ENTRY_CONFIRM_TITLE : PokemonMsgConfigKeys.POKEMON_ENTRY_CONFIRM_TITLE_AUCTION, null, variables));

		List<String> template = Lists.newArrayList();
		template.addAll(ReforgedBridge.getInstance().getMsgConfig().get(listing.getAucData() == null ? PokemonMsgConfigKeys.POKEMON_ENTRY_BASE_LORE : PokemonMsgConfigKeys.POKEMON_ENTRY_CONFIRM_LORE_AUCTION));
		this.addLore(icon, template, player, listing, variables);

		return icon;
	}

	private void addLore(ItemStack icon, List<String> template, Player player, Listing listing, Map<String, Object> variables) {
		for(EnumHidableDetail detail : EnumHidableDetail.values()) {
			if(detail.getCondition().test(this.getEntry())) {
				template.addAll(ReforgedBridge.getInstance().getMsgConfig().get(detail.getField()));
			}
		}

		if(listing.getAucData() != null) {
			template.addAll(ReforgedBridge.getInstance().getMsgConfig().get(MsgConfigKeys.AUCTION_INFO));
		} else {
			template.addAll(ReforgedBridge.getInstance().getMsgConfig().get(MsgConfigKeys.ENTRY_INFO));
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
		Optional<PlayerPartyStorage> optStorage = Optional.ofNullable(Pixelmon.storageManager.getParty(user.getUniqueId()));

		if (!optStorage.isPresent())
			return false;

		optStorage.get().add(this.getEntry());

		return true;
	}

	@Override
	public boolean doTakeAway(Player player) {
		if(BattleRegistry.getBattle((EntityPlayer) player) != null) {
			player.sendMessage(Text.of(GTSInfo.ERROR, TextColors.GRAY, "You are in battle, you can't sell any pokemon currently..."));
			return false;
		}

		if(UNTRADABLE.matches(this.getEntry())) {
			player.sendMessage(Text.of(GTSInfo.ERROR, TextColors.GRAY, "This pokemon is marked as untradeable, and cannot be sold..."));
			return false;
		}

		if(ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.BLACKLISTED).stream().anyMatch(name -> name.equalsIgnoreCase(this.getEntry().getSpecies().getPokemonName()))){
			player.sendMessage(Text.of(GTSInfo.ERROR, TextColors.GRAY, "Sorry, but ", TextColors.YELLOW, this.getName(), TextColors.GRAY, " has been blacklisted from the GTS..."));
			return false;
		}

		PlayerPartyStorage ps = Pixelmon.storageManager.getParty(player.getUniqueId());
		if(ps == null)
			return false;

		ps.retrieveAll();
		ps.set(ps.getPosition(this.getEntry()), null);

		return true;
	}

	private static ItemStack getPicture(com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon) {
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
		com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon = this.getEntry();
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

	public enum LakeTrio {
		Mesprit(EnumSpecies.Mesprit),
		Azelf(EnumSpecies.Azelf),
		Uxie(EnumSpecies.Uxie);

		private EnumSpecies species;

		LakeTrio(EnumSpecies species) {
			this.species = species;
		}

		public static boolean isMember(EnumSpecies species) {
			for(LakeTrio guardian : values()) {
				if(guardian.species == species) {
					return true;
				}
			}

			return false;
		}
	}
}
