package me.nickimpact.gts.pixelmon;

import lombok.Builder;
import lombok.Getter;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.UUID;

public interface GTSPokemon<Pokemon, Species, Nature, Growth, Gender, Pokeball, SpecialTexture> {

	Pokemon construct();

	Species getSpecies();
	int getLevel();
	int getForm();
	boolean isShiny();

	String getNickname();

	String getAbility();
	int getAbilitySlot();

	Pokeball getPokeball();
	Nature getNature();
	Growth getGrowth();
	Gender getGender();

	StatWrapper getStats();
	StatWrapper getEVs();
	StatWrapper getIVs();

	short getStatus();

	SpecialTexture getSpecialTexture();
	String getCustomTexture();

	int getHealth();
	List<Integer> getRelearnableMoves();

	boolean doesLevel();
	EggData getEggData();

	ItemStack getHeldItem();

	UUID getOriginalTrainerID();
	String getOriginalTrainerName();

	AttackWrapper[] getMoveset();

	@Getter
	@Builder
	class StatWrapper {

		private int hp;
		private int attack;
		private int defence;
		private int spatk;
		private int spdef;
		private int speed;

	}

	@Getter
	@Builder
	class EggData {

		private Integer eggCycles;
		private Integer eggSteps;

	}

	@Getter
	@Builder
	class AttackWrapper {

		private int moveID;
		private int pp;
		private int ppLevel;

	}
}
