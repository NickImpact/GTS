package com.nickimpact.gts.entries.pixelmon;

import com.nickimpact.gts.configuration.MsgConfigKeys;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;
import java.util.function.Predicate;

@Getter
@AllArgsConstructor
public enum EnumHidableDetail {

	TEXTURE(pokemon -> {
			NBTTagCompound nbt = new NBTTagCompound();
			pokemon.writeToNBT(nbt);

			String texture = nbt.getString(NbtKeys.CUSTOM_TEXTURE);
			return !texture.isEmpty();
		}, MsgConfigKeys.PE_BASE_TEXTURE
	),
	UNBREEDABLE(pokemon -> {
			PokemonSpec unbreedable = new PokemonSpec("unbreedable");
			return unbreedable.matches(pokemon);
		},
		MsgConfigKeys.PE_BASE_UNBREEDABLE
	),
	POKERUS(pokemon -> pokemon.getPokerus().isPresent(), MsgConfigKeys.PE_BASE_POKERUS),
	CLONES(pokemon -> pokemon.getSpecies().equals(EnumPokemon.Mew), MsgConfigKeys.POKEMON_ENTRY_BASE_MEW_CLONES),
	ENCHANTED(pokemon -> PokemonEntry.LakeTrio.isMember(pokemon.getSpecies()), MsgConfigKeys.POKEMON_ENTRY_BASE_LAKE_TRIO),
	;

	private Predicate<EntityPixelmon> condition;
	private ConfigKey<List<String>> field;
}
