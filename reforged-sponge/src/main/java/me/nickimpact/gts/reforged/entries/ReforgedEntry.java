package me.nickimpact.gts.reforged.entries;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.impactor.api.json.JsonTyping;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.util.helpers.SpriteHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.reforged.ReforgedBridge;
import me.nickimpact.gts.reforged.config.PokemonMsgConfigKeys;
import me.nickimpact.gts.reforged.utils.Flags;
import me.nickimpact.gts.sponge.SpongeEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JsonTyping("pokemon")
public class ReforgedEntry extends SpongeEntry<String, Pokemon> {

	private transient Pokemon pokemon;

//	private static final Function<Pokemon, String> TO_BASE_64 = poke -> {
//		ByteBuf buffer = Unpooled.buffer();
//		poke.writeToByteBuffer(buffer, EnumUpdateType.ALL);
//		return Base64.getEncoder().encodeToString(buffer.array());
//	};
//
//	private static final Function<String, Pokemon> FROM_BASE_64 = base64 -> {
//		byte[] bytes = Base64.getDecoder().decode(base64);
//		ByteBuf buffer = Unpooled.copiedBuffer(bytes);
//		Pokemon out = Pixelmon.pokemonFactory.create(EnumSpecies.Bidoof);
//		out.readFromByteBuffer(buffer, EnumUpdateType.ALL);
//		return out;
//	};

	public ReforgedEntry(Pokemon element) {
		super(element.writeToNBT(new NBTTagCompound()).toString());
		this.pokemon = element;
	}

	@Override
	public Pokemon getEntry() {
		try {
			return pokemon != null ? pokemon : (pokemon = Pixelmon.pokemonFactory.create(JsonToNBT.getTagFromJson(this.element)));
		} catch (NBTException e) {
			e.printStackTrace();
			return null;
		}
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
		return this.getEntry().getSpecies().getLocalizedName();
	}

	@Override
	public List<String> getDetails() {
		Pokemon pokemon = Pixelmon.pokemonFactory.create(EnumSpecies.Bidoof);
		ByteBuf buffer = Unpooled.buffer();
		pokemon.writeToByteBuffer(buffer);
		Base64.getEncoder().encodeToString(buffer.array());

		// TODO
		return Lists.newArrayList();
	}

	@Override
	public ItemStack baseItemStack(Player player, Listing listing) {
		ItemStack icon = this.getPicture(this.getEntry());
		Map<String, Object> variables = Maps.newHashMap();
		variables.put("listing", listing);
		variables.put("pokemon", this.getEntry());

		icon.offer(Keys.DISPLAY_NAME, ReforgedBridge.getInstance().getTextParsingUtils().fetchAndParseMsg(player, PokemonMsgConfigKeys.POKEMON_ENTRY_BASE_TITLE, null, variables));

		List<String> template = Lists.newArrayList();
		template.addAll(ReforgedBridge.getInstance().getMsgConfig().get(PokemonMsgConfigKeys.POKEMON_ENTRY_BASE_LORE));
		this.addLore(icon, template, player, variables);

		return icon;
	}

	@Override
	public ItemStack confirmItemStack(Player player, Listing listing) {
		return ItemStack.builder().itemType(ItemTypes.AIR).build();
	}

	private void addLore(ItemStack icon, List<String> template, Player player, Map<String, Object> variables) {
		for (EnumHidableDetail detail : EnumHidableDetail.values()) {
			if (detail.getCondition().test(this.getEntry())) {
				template.addAll(ReforgedBridge.getInstance().getMsgConfig().get(detail.getField()));
			}
		}


		template.addAll(ReforgedBridge.getInstance().getMsgConfig().get(MsgConfigKeys.ENTRY_INFO));
		List<Text> translated = template.stream().map(str -> ReforgedBridge.getInstance().getTextParsingUtils().fetchAndParseMsg(player, str, null, variables)).collect(Collectors.toList());
		icon.offer(Keys.ITEM_LORE, translated);
	}

	@Override
	public boolean supportsOffline() {
		return true;
	}

	@Override
	public boolean giveEntry(User user) {
		PlayerPartyStorage storage = Pixelmon.storageManager.getParty(user.getUniqueId());
		storage.add(this.getEntry());
		return true;
	}

	@Override
	public boolean doTakeAway(Player player) {
		if(BattleRegistry.getBattle((EntityPlayer) player) != null) {
			// TODO - Message
			return false;
		}

		if(Flags.UNTRADABLE.matches(this.getEntry())) {
			// TODO - Message
			return false;
		}

		// TODO - Check for blacklist

		PlayerPartyStorage storage = Pixelmon.storageManager.getParty(player.getUniqueId());
		storage.retrieveAll();
		storage.set(storage.getPosition(this.getEntry()), null);

		return true;
	}

	private ItemStack getPicture(Pokemon pokemon) {
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
