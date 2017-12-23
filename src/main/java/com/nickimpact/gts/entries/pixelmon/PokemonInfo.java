package com.nickimpact.gts.entries.pixelmon;

import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.configuration.MsgConfigKeys;
import com.nickimpact.gts.api.listings.pricing.Price;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.util.helpers.SpriteHelper;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class PokemonInfo {

	public static ItemStack getConfirmDisplay(Player player, EntityPixelmon pokemon) {
		ItemStack button = ItemStack.builder()
				.itemType(ItemTypes.MAP)
				.build();
		button.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GRAY, "More Stats"));

		List<Text> data = new ArrayList<>();
		DecimalFormat df = new DecimalFormat("#0.##");

		int totalIvs = pokemon.stats.IVs.HP + pokemon.stats.IVs.Attack + pokemon.stats.IVs.Defence + pokemon.stats.IVs.SpAtt + pokemon.stats.IVs.SpDef + pokemon.stats.IVs.Speed;
		double ivPercent = totalIvs / 186.0 * 100;

		if (pokemon.isEgg) {
			data.add(Text.EMPTY);
			data.add(Text.of(TextColors.GRAY, "IVs: ", TextColors.YELLOW, totalIvs, TextColors.GRAY, "/", TextColors.YELLOW, "186  ", TextColors.GRAY, "(", TextColors.GREEN, df.format(ivPercent), "%", TextColors.GRAY, ")"));
			data.add(Text.of(TextColors.GRAY, "  HP: ", TextColors.YELLOW, pokemon.stats.IVs.HP));
			data.add(Text.of(TextColors.GRAY, "  ATK: ", TextColors.YELLOW, pokemon.stats.IVs.Attack));
			data.add(Text.of(TextColors.GRAY, "  DEF: ", TextColors.YELLOW, pokemon.stats.IVs.Defence));
			data.add(Text.of(TextColors.GRAY, "  SPATK: ", TextColors.YELLOW, pokemon.stats.IVs.SpAtt));
			data.add(Text.of(TextColors.GRAY, "  SPDEF: ", TextColors.YELLOW, pokemon.stats.IVs.SpDef));
			data.add(Text.of(TextColors.GRAY, "  SPEED: ", TextColors.YELLOW, pokemon.stats.IVs.Speed));
		} else {

			int totalEvs = pokemon.stats.EVs.HP + pokemon.stats.EVs.Attack + pokemon.stats.EVs.Defence + pokemon.stats.EVs.SpecialAttack + pokemon.stats.EVs.SpecialDefence + pokemon.stats.EVs.Speed;
			double evPercent = totalEvs / 510.0 * 100;

			data.add(Text.EMPTY);
			data.add(Text.of(TextColors.GRAY, "EVs: ", TextColors.YELLOW, totalEvs, TextColors.GRAY, "/", TextColors.YELLOW, "510  ", TextColors.GRAY, "(", TextColors.GREEN, df.format(evPercent), "%", TextColors.GRAY, ")"));
			data.add(Text.of(TextColors.GRAY, "IVs: ", TextColors.YELLOW, totalIvs, TextColors.GRAY, "/", TextColors.YELLOW, "186  ", TextColors.GRAY, "(", TextColors.GREEN, df.format(ivPercent), "%", TextColors.GRAY, ")"));
			data.add(Text.of(TextColors.GRAY, "  HP: ", TextColors.YELLOW, pokemon.stats.EVs.HP, TextColors.GRAY, "/", TextColors.YELLOW, pokemon.stats.IVs.HP));
			data.add(Text.of(TextColors.GRAY, "  ATK: ", TextColors.YELLOW, pokemon.stats.EVs.Attack, TextColors.GRAY, "/", TextColors.YELLOW, pokemon.stats.IVs.Attack));
			data.add(Text.of(TextColors.GRAY, "  DEF: ", TextColors.YELLOW, pokemon.stats.EVs.Defence, TextColors.GRAY, "/", TextColors.YELLOW, pokemon.stats.IVs.Defence));
			data.add(Text.of(TextColors.GRAY, "  SPATK: ", TextColors.YELLOW, pokemon.stats.EVs.SpecialAttack, TextColors.GRAY, "/", TextColors.YELLOW, pokemon.stats.IVs.SpAtt));
			data.add(Text.of(TextColors.GRAY, "  SPDEF: ", TextColors.YELLOW, pokemon.stats.EVs.SpecialDefence, TextColors.GRAY, "/", TextColors.YELLOW, pokemon.stats.IVs.SpDef));
			data.add(Text.of(TextColors.GRAY, "  SPEED: ", TextColors.YELLOW, pokemon.stats.EVs.Speed, TextColors.GRAY, "/", TextColors.YELLOW, pokemon.stats.IVs.Speed));
			data.add(Text.EMPTY);
			data.add(Text.of(TextColors.GRAY, "Moves:"));
			if(pokemon.getMoveset().attacks[0] != null) data.add(Text.of(TextColors.YELLOW, "  - " + pokemon.getMoveset().attacks[0].baseAttack.getLocalizedName()));
			if(pokemon.getMoveset().attacks[1] != null) data.add(Text.of(TextColors.YELLOW, "  - " + pokemon.getMoveset().attacks[1].baseAttack.getLocalizedName()));
			if(pokemon.getMoveset().attacks[2] != null) data.add(Text.of(TextColors.YELLOW, "  - " + pokemon.getMoveset().attacks[2].baseAttack.getLocalizedName()));
			if(pokemon.getMoveset().attacks[3] != null) data.add(Text.of(TextColors.YELLOW, "  - " + pokemon.getMoveset().attacks[3].baseAttack.getLocalizedName()));
		}
		button.offer(Keys.ITEM_LORE, data);
		return button;
	}
}
