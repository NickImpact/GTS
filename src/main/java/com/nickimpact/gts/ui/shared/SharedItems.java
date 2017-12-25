package com.nickimpact.gts.ui.shared;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.configuration.MsgConfigKeys;
import com.nickimpact.gts.api.gui.Icon;
import com.nickimpact.gts.utils.ItemUtils;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.util.helpers.SpriteHelper;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class SharedItems {

    private static HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();

    /**
     * This method is used to create a border representation of an item
     *
     * @param slot The slot this border piece will be placed in
     * @param color The dye color of the border piece
     * @return An icon for an Inventory display
     */
    public static Icon forgeBorderIcon(int slot, DyeColor color){
        return new Icon(slot, ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .quantity(1)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.BLACK, ""))
                .add(Keys.DYE_COLOR, color)
                .build()
        );
    }

    public static ItemStack pokemonDisplay(EntityPixelmon pokemon, int form){
        net.minecraft.item.ItemStack nativeItem = new net.minecraft.item.ItemStack(PixelmonItems.itemPixelmonSprite);
        NBTTagCompound nbt = new NBTTagCompound();
        String idValue = String.format("%03d", pokemon.baseStats.nationalPokedexNumber);
        if (pokemon.isEgg){
            if (pokemon.getName().equalsIgnoreCase("Manaphy")){
                nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/manaphy1");
            } else if (pokemon.getName().equalsIgnoreCase("Togepi")){
                nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/togepi1");
            } else {
                nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/egg1");
            }
        } else {
            if (pokemon.getIsShiny()) {
                nbt.setString(NbtKeys.SPRITE_NAME,
                              "pixelmon:sprites/shinypokemon/" + idValue + SpriteHelper.getSpriteExtra(
                                      pokemon.baseStats.pixelmonName, form));
            } else {
                nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/pokemon/" + idValue + SpriteHelper.getSpriteExtra(
                        pokemon.baseStats.pixelmonName, form));
            }
        }
        nativeItem.setTagCompound(nbt);

        return ItemStackUtil.fromNative(nativeItem);
    }

    static Icon pageIcon(int slot, boolean nextOrLast, int curr, int next){
        return new Icon(slot, ItemStack.builder()
                .itemType(ItemTypes.DYE)
                .quantity(1)
                .add(Keys.DISPLAY_NAME, nextOrLast ? Text.of(
                        TextColors.GREEN, "\u2192 ", GTS.getInstance().getDefaultMsgConfig().get(MsgConfigKeys.UI_ITEMS_NEXT_PAGE),
                        TextColors.GREEN, " \u2192"
                    ) : Text.of(
                            TextColors.RED, "\u2190 ", GTS.getInstance().getDefaultMsgConfig().get(MsgConfigKeys.UI_ITEMS_LAST_PAGE),
                            TextColors.RED, " \u2190"
                    )
                )
                .add(Keys.DYE_COLOR, nextOrLast ? DyeColors.LIME : DyeColors.RED)
                .add(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "Curr Page: ", TextColors.DARK_AQUA, curr),
                        Text.of(TextColors.GRAY, "Next Page: ", TextColors.DARK_AQUA, next)
                ))
                .build()
        );
    }

    static Icon refreshIcon(Player player, int slot){
    	ItemStack.Builder ib = ItemStack.builder().itemType(ItemTypes.BOOK);
	    try {
	    	ib.add(Keys.DISPLAY_NAME, GTS.getInstance().getTextParsingUtils().parse(
	    			GTS.getInstance().getTextParsingUtils().getTemplate(GTS.getInstance().getDefaultMsgConfig().get(MsgConfigKeys.UI_ITEMS_REFRESH)),
				    player,
				    null,
				    null
		    ));
	    } catch (NucleusException e) {
		    ib.add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, "Refresh Listings"));
	    }

	    return new Icon(slot, ib.build());
    }

    static Icon playerIcon(Player player, int slot){
    	Text title;
    	List<Text> lore;
    	try {
		    title = GTS.getInstance().getTextParsingUtils().parse(
				    GTS.getInstance().getTextParsingUtils().getTemplate(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.UI_ITEMS_PLAYER_TITLE)),
				    player,
				    null,
				    null
		    );
	    } catch (NucleusException e) {
		    title = Text.of();
	    }

	    try {
    		lore = GTS.getInstance().getTextParsingUtils().parse(
    				GTS.getInstance().getTextParsingUtils().getTemplates(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.UI_ITEMS_PLAYER_LORE)),
				    player,
				    null,
				    null
		    );
	    } catch (NucleusException e) {
		    lore = Lists.newArrayList();
	    }

	    return new Icon(slot, ItemUtils.createSkull(player.getUniqueId(), title, lore));
    }

    static Icon balanceIcon(int slot, Player player){
        ItemStack icon = ItemStack.builder()
                .itemType(ItemTypes.GOLD_INGOT)
                .build();

        textOptions.clear();

        if(GTS.getInstance().getEconomy() == null){
            textOptions.put("balance", Optional.of(0));
            //icon.offer(Keys.DISPLAY_NAME, GTS.getInstance().getAPI().getMessage("UI.Items.Balance Icon", textOptions));
        }
        Optional<UniqueAccount> acc = GTS.getInstance().getEconomy().getOrCreateAccount(player.getUniqueId());
        if(acc.isPresent()) {
            textOptions.put("curr_symbol", Optional.of(GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toPlain()));
            textOptions.put("balance", Optional.of(acc.get().getBalance(GTS.getInstance().getEconomy().getDefaultCurrency())));
           // icon.offer(Keys.DISPLAY_NAME, GTS.getInstance().getAPI().getMessage("UI.Items.Balance Icon", textOptions));
        } else {
            textOptions.put("balance", Optional.of(0));
            //icon.offer(Keys.DISPLAY_NAME, GTS.getInstance().getAPI().getMessage("UI.Items.Balance Icon", textOptions));
        }

        return new Icon(slot, icon);
    }

    static Icon playerListingsIcon(int slot){
        return new Icon(slot, ItemStack.builder()
                .itemType(ItemTypes.WRITTEN_BOOK)
                .quantity(1)
                //.add(Keys.DISPLAY_NAME, GTS.getInstance().getAPI().getMessage("UI.Items.Player Listings", null))
                .build()

        );
    }

    public static Icon confirmIcon(int slot){
        return new Icon(slot, ItemStack.builder()
                .itemType(ItemTypes.DYE)
                .quantity(1)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Confirm Action"))
                .add(Keys.DYE_COLOR, DyeColors.LIME)
                .build()
        );
    }

    public static Icon denyIcon(int slot){
        return new Icon(slot, ItemStack.builder()
                .itemType(ItemTypes.DYE)
                .quantity(1)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Cancel Action"))
                .add(Keys.DYE_COLOR, DyeColors.RED)
                .build()
        );
    }

    static Icon lastMenu(int slot){
        return new Icon(slot, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:eject_button").get())
                .quantity(1)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Last Menu"))
                .build()
        );
    }

    public static Icon cancelIcon(int slot){
        return new Icon(slot, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:trash_can").orElse(ItemTypes.BARRIER))
                .add(Keys.DISPLAY_NAME, Text.of(
                        TextColors.RED, TextStyles.BOLD, "Reset Option"
                ))
                .add(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "Click here to reset the"),
                        Text.of(TextColors.GRAY, "current query back to its"),
                        Text.of(TextColors.GRAY, "default search option")
                ))
                .build()
        );
    }
}
