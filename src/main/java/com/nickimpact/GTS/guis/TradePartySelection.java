package com.nickimpact.GTS.guis;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.Enchantments;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.utils.LotCache;
import com.nickimpact.GTS.utils.LotUtils;
import com.nickimpact.GTS.utils.PokeRequest;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import com.pixelmonmod.pixelmon.util.helpers.SpriteHelper;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

public class TradePartySelection extends InventoryBase {

	private Player player;
	private LotCache lot;
	private PokeRequest pr;

	private MainUI main;

	private PlayerStorage storage;

	public TradePartySelection(Player player, LotCache lot, MainUI main) {
		super(5, Text.of(
				TextColors.RED, "GTS", TextColors.DARK_GRAY, " \u00bb ", TextColors.DARK_GREEN, "Select Pokemon"
				));

		this.player = player;
		this.lot = lot;
		this.pr = new Gson().fromJson(lot.getLot().getPokeWanted(), PokeRequest.class);

		this.main = main;

		this.storage = PixelmonStorage.pokeBallManager.getPlayerStorage((EntityPlayerMP)player).orElse(null);
		this.setupDesign();
	}

	private void setupDesign(){
		for(int x = 1, y = 2; x < 8; x++){
			this.addIcon(SharedItems.forgeBorderIcon(x + (9 * y), DyeColors.BLACK));
		}

		for(int i = 0, index = 28; i < 6; i++, index++){
			this.addIcon(getPokemon(this.storage, i, index));
		}

		this.addIcon(new InventoryIcon(11, lot.getLot().getItem().getItem(lot)));
		this.addIcon(mapIcon());
		InventoryIcon cancel = SharedItems.lastMenu(15);
		cancel.addListener(ClickInventoryEvent.class, e -> {
			Sponge.getScheduler().createTaskBuilder().execute(() -> {
				player.closeInventory();
				player.openInventory(this.main.getInventory());
			}).delayTicks(1).submit(GTS.getInstance());
		});

		this.addIcon(cancel);

		this.drawBorder(5, DyeColors.BLACK);
	}

	private InventoryIcon mapIcon() {
		PokeRequest pr = new Gson().fromJson(lot.getLot().getPokeWanted(), PokeRequest.class);

		return new InventoryIcon(13, ItemStack.builder()
				.itemType(ItemTypes.FILLED_MAP)
				.keyValue(Keys.DISPLAY_NAME, Text.of(
						TextColors.DARK_AQUA, TextStyles.BOLD, "Requested Info"
						))
				.keyValue(Keys.ITEM_LORE, Lists.newArrayList(
						Text.of(TextColors.GRAY, "Pokemon: ", TextColors.GREEN, pr.getPokemon()),
						Text.of(TextColors.GRAY, "Level: ", TextColors.YELLOW,
								(pr.getLevel() < PixelmonConfig.maxLevel ? pr.getLevel() + "+" : pr.getLevel())
								),
						Text.of(TextColors.GRAY, "Form: ", TextColors.YELLOW, getFormName(pr.getPokemon(), pr.getForm())),
						Text.of(TextColors.GRAY, "EVs: ", getFromArray(pr.getEvs())),
						Text.of(TextColors.GRAY, "IVs: ", getFromArray(pr.getIvs())),
						Text.of(TextColors.GRAY, "Shiny: ", TextColors.YELLOW, pr.isShiny() ? "Yes" : "No"),
						Text.of(TextColors.GRAY, "Growth: ", TextColors.YELLOW, pr.getGrowth()),
						Text.of(TextColors.GRAY, "Ability: ", TextColors.YELLOW, pr.getAbility()),
						Text.of(TextColors.GRAY, "Nature: ", TextColors.YELLOW, pr.getNature()),
						Text.of(TextColors.GRAY, "Pokeball: ", TextColors.YELLOW, pr.getPokeball())

						))
				.build()
				);
	}

	private String getFormName(String pokemon, int form){
		return form != -1 ?
				LotUtils.capitalize(SpriteHelper.getSpriteExtra(pokemon, form).substring(1)) :
					"N/A";
	}

	private Text getFromArray(int[] array){
		Text result = Text.of(TextColors.YELLOW, array[0], TextColors.GRAY, "/");

		for(int i = 1; i < array.length; i++){
			if(i == array.length - 1){
				result = Text.of(result, TextColors.YELLOW, array[i]);
			} else {
				result = Text.of(result, TextColors.YELLOW, array[i], TextColors.GRAY, "/");
			}
		}

		return result;
	}

	private InventoryIcon getPokemon(PlayerStorage storage, int slot, int index){
		if(storage.partyPokemon[slot] == null){
			return new InventoryIcon(index, ItemStack.builder()
					.itemType(ItemTypes.BARRIER)
					.keyValue(Keys.DISPLAY_NAME, Text.of(
							TextColors.RED, "Empty Slot"
							))
					.build()
					);
		}

		EntityPixelmon poke = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(storage.partyPokemon[slot], (World)player.getWorld());

		InventoryIcon icon = new InventoryIcon(index, ItemStack.builder()
				.from(SharedItems.pokemonDisplay(poke, poke.getForm()))
				.keyValue(Keys.DISPLAY_NAME,
						!poke.isEgg ? Text.of(
								TextColors.AQUA, poke.getName(), TextColors.YELLOW, " Lvl " + poke.getLvl().getLevel()
								) :
									Text.of(
											TextColors.AQUA, "Unknown"
											)
						)
				.build()
				);

		if(LotUtils.isValidTrade(this.pr, poke)){
			icon.getDisplay().offer(Keys.ITEM_ENCHANTMENTS, Lists.newArrayList(
					new ItemEnchantment(Enchantments.UNBREAKING, 0)
					));
			icon.getDisplay().offer(Keys.HIDE_ENCHANTMENTS, true);

			icon.addListener(ClickInventoryEvent.class, e -> {
				Sponge.getScheduler().createTaskBuilder().execute(() -> {
					this.player.closeInventory();

					this.player.openInventory(new LotUI(this.player, this.lot, true, slot).getInventory());
				}).delayTicks(1).submit(GTS.getInstance());
			});
		}

		return icon;
	}
}
