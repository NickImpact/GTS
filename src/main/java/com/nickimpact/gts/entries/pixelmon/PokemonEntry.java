package com.nickimpact.gts.entries.pixelmon;

import com.google.common.collect.Lists;
import com.nickimpact.gts.api.json.Typing;
import com.nickimpact.gts.api.listings.entries.Entry;
import com.nickimpact.gts.api.listings.pricing.Price;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import com.pixelmonmod.pixelmon.util.helpers.SpriteHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.List;
import java.util.Optional;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Typing("Pokemon")
public class PokemonEntry extends Entry<Pokemon> {

	public PokemonEntry(EntityPixelmon pokemon, Price price) {
		this(new Pokemon(pokemon), price);
	}

	public PokemonEntry(Pokemon pokemon, Price price) {
		super(pokemon, price);
	}

	@Override
	public String getName() {
		return this.getElement().getPokemon().getName();
	}

	@Override
	public ItemStack baseItemStack(Player player) {
		return getPicture(this.getElement().getPokemon());
	}

	@Override
	public String baseTitleTemplate() {
		return "&e{{pokemon}} {{shiny:s}}&7| &bLvl {{level}}";
	}

	@Override
	public List<String> baseLoreTemplate() {
		return Lists.newArrayList(
				"&7Listing ID: &e{{id}}",
				"&7Seller: &e{{seller}}",
				"",
				"&7Ability: &e{{ability}}",
				"&7Gender: &e{{gender}}",
				"&7Nature: &e{{nature}}",
				"&7Size: &e{{growth}}",
				"",
				"&7Price: &e{{price}}",
				"&7Time Left: &e{{time_left}}"
		);
	}

	@Override
	public ItemStack confirmItemStack(Player player) {
		return getPicture(this.getElement().getPokemon());
	}

	@Override
	public String confirmTitleTemplate() {
		return "&ePurchase {{pokemon}}?";
	}

	@Override
	public List<String> confirmLoreTemplate() {
		return Lists.newArrayList(
				"Here's some additional info:",
				"",
				"&7EVs: &e{{evs_total}}&7/&e510 &7(&a{{evs_percent}}&7)",
				"&7IVs: &e{{ivs_total}}&7/&e186 &7(&a{{ivs_percent}}&7)",
				"",
				"&7Move Set:",
				"  &7 - &e{{moves_1}}",
				"  &7 - &e{{moves_2}}",
				"  &7 - &e{{moves_3}}",
				"  &7 - &e{{moves_4}}"
		);
	}

	@Override
	public boolean giveEntry(User user) {
		Optional<PlayerStorage> optStorage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID(
				(MinecraftServer) Sponge.getServer(), user.getUniqueId()
		);

		if (!optStorage.isPresent())
			return false;

		PlayerStorage storage = optStorage.get();
		storage.addToParty(this.getElement().getPokemon());
		storage.sendUpdatedList();

		return false;
	}

	@Override
	public boolean doTakeAway(Player player) {
		PlayerStorage ps = PixelmonStorage.pokeBallManager.getPlayerStorage((EntityPlayerMP)player).orElse(null);
		if(ps == null)
			return false;

		ps.recallAllPokemon();
		ps.removeFromPartyPlayer(ps.getPosition(this.getElement().getPokemon().getPokemonId()));
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
				default:
					nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/egg1");
			}
		} else if (pokemon.getIsShiny()) {
			nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/shinypokemon/" + idValue + SpriteHelper.getSpriteExtra(pokemon.getSpecies().name, pokemon.getForm()));
		} else {
			nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/pokemon/" + idValue + SpriteHelper.getSpriteExtra(pokemon.getSpecies().name, pokemon.getForm()));
		}

		item.setTagCompound(nbt);
		return ItemStackUtil.fromNative(item);
	}
}
