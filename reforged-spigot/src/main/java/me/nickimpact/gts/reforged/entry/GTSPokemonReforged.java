package me.nickimpact.gts.reforged.entry;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.EnumSpecialTexture;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.items.EnumPokeballs;
import me.nickimpact.gts.pixelmon.GTSPokemon;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.UUID;

public class GTSPokemonReforged implements GTSPokemon<Pokemon, EnumSpecies, EnumNature, EnumGrowth, Gender, EnumPokeballs, EnumSpecialTexture> {

	private EnumSpecies species;
	private int level;
	private int form;
	private boolean shiny;
	private String nickname;
	private String ability;
	private int abilitySlot;
	private EnumPokeballs pokeball;
	private EnumNature nature;
	private EnumGrowth growth;
	private Gender gender;
	private StatWrapper stats;
	private StatWrapper evs;
	private StatWrapper ivs;
	private short status;
	private EnumSpecialTexture specialTexture;
	private String customTexture;
	private int health;
	private List<Integer> relearnableMoves;
	private boolean doesLevel;
	private EggData eggData;
	private ItemStack heldItem;
	private UUID originalTrainerID;
	private String originalTrainerName;
	private AttackWrapper[] attacks;

	private GTSPokemonReforged(Pokemon pokemon) {

	}

	public static GTSPokemonReforged from(Pokemon pokemon) {
		return new GTSPokemonReforged(pokemon);
	}

	@Override
	public Pokemon construct() {
		Pokemon pokemon = Pixelmon.pokemonFactory.create(this.species);
		return null;
	}

	@Override
	public EnumSpecies getSpecies() {
		return species;
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public int getForm() {
		return form;
	}

	@Override
	public boolean isShiny() {
		return shiny;
	}

	@Override
	public String getNickname() {
		return nickname;
	}

	@Override
	public String getAbility() {
		return ability;
	}

	@Override
	public int getAbilitySlot() {
		return abilitySlot;
	}

	@Override
	public EnumPokeballs getPokeball() {
		return pokeball;
	}

	@Override
	public EnumNature getNature() {
		return nature;
	}

	@Override
	public EnumGrowth getGrowth() {
		return growth;
	}

	@Override
	public Gender getGender() {
		return gender;
	}

	@Override
	public StatWrapper getStats() {
		return stats;
	}

	@Override
	public StatWrapper getEVs() {
		return null;
	}

	@Override
	public StatWrapper getIVs() {
		return null;
	}

	@Override
	public short getStatus() {
		return status;
	}

	@Override
	public EnumSpecialTexture getSpecialTexture() {
		return specialTexture;
	}

	@Override
	public String getCustomTexture() {
		return customTexture;
	}

	@Override
	public int getHealth() {
		return health;
	}

	@Override
	public List<Integer> getRelearnableMoves() {
		return relearnableMoves;
	}

	public boolean doesLevel() {
		return doesLevel;
	}

	@Override
	public EggData getEggData() {
		return eggData;
	}

	@Override
	public ItemStack getHeldItem() {
		return heldItem;
	}

	@Override
	public UUID getOriginalTrainerID() {
		return originalTrainerID;
	}

	@Override
	public String getOriginalTrainerName() {
		return originalTrainerName;
	}

	@Override
	public AttackWrapper[] getMoveset() {
		return attacks;
	}
}
