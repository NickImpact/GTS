package com.nickimpact.GTS.utils;

import com.nickimpact.GTS.GTS;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.util.helpers.SpriteHelper;
import net.minecraft.nbt.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================*
 * |   Project Name: GTS
 * |   Author: Nick (NickImpact)
 * |
 * |   Date of File Creation: 10/23/2016
 * =============================================================================
 */

public class PokemonItem {
    private String owner;
    private int cost = -1;
    private int startPrice = -1;
    private int increment = -1;

    private int id;
    private int form;
    private String name;
    private String nickname;
    private String ability;
    private String m1;
    private String m2;
    private String m3;
    private String m4;
    private int lvl;
    private String growth;
    private String gender;
    private boolean shiny;
    private String nature;
    private int hp;
    private int atk;
    private int def;
    private int spd;
    private int spatk;
    private int spdef;
    private int evhp;
    private int evatk;
    private int evdef;
    private int evspd;
    private int evspatk;
    private int evspdef;
    private int id1;
    private int id2;
    private short clones = -1;
    private boolean isEgg;
    private String heldItem = "Nothing";

    public PokemonItem(EntityPixelmon pokemon, String owner, Object... options) {
        this.owner = owner;

        if(options.length == 1 && options[0] instanceof Integer) {
            this.cost = (Integer) options[0];
            this.startPrice = 0;
            this.increment = 0;
        } else if(options.length == 2) {
            this.cost = 0;
            this.startPrice = (Integer) options[0];
            this.increment = (Integer) options[1];
        } else {
            this.cost = 0;
            this.startPrice = 0;
            this.increment = 0;
        }

        this.id = pokemon.baseStats.nationalPokedexNumber;
        this.form = pokemon.getEntityData().getInteger(NbtKeys.FORM);
        this.name = pokemon.getPokemonName();
        this.nickname = pokemon.getNickname();
        this.ability = pokemon.getAbility().getName();
        this.heldItem = pokemon.getItemHeld() != null ? pokemon.getItemHeld().getHeldItemType().name() : "Nothing";
        this.m1 = pokemon.getMoveset().get(0) != null ? pokemon.getMoveset().get(0).baseAttack.getLocalizedName() : "Empty";
        this.m2 = pokemon.getMoveset().get(1) != null ? pokemon.getMoveset().get(1).baseAttack.getLocalizedName() : "Empty";
        this.m3 = pokemon.getMoveset().get(2) != null ? pokemon.getMoveset().get(2).baseAttack.getLocalizedName() : "Empty";
        this.m4 = pokemon.getMoveset().get(3) != null ? pokemon.getMoveset().get(3).baseAttack.getLocalizedName() : "Empty";
        this.isEgg = pokemon.isEgg;
        this.lvl = pokemon.getLvl().getLevel();
        this.hp = pokemon.stats.IVs.HP;
        this.atk = pokemon.stats.IVs.Attack;
        this.def = pokemon.stats.IVs.Defence;
        this.spd = pokemon.stats.IVs.Speed;
        this.spatk = pokemon.stats.IVs.SpAtt;
        this.spdef = pokemon.stats.IVs.SpDef;
        this.evhp = pokemon.stats.EVs.HP;
        this.evatk = pokemon.stats.EVs.Attack;
        this.evdef = pokemon.stats.EVs.Defence;
        this.evspd = pokemon.stats.EVs.Speed;
        this.evspatk = pokemon.stats.EVs.SpecialAttack;
        this.evspdef = pokemon.stats.EVs.SpecialDefence;
        this.nature = pokemon.getNature().name();
        this.gender = pokemon.gender.name();
        this.shiny = pokemon.getIsShiny();
        this.growth = pokemon.getGrowth().name();
        this.id1 = pokemon.getPokemonId()[0];
        this.id2 = pokemon.getPokemonId()[1];

        NBTTagCompound nbt = pokemon.getEntityData();
        if (pokemon.getName().equalsIgnoreCase("Mew")) {
            this.clones = nbt.getShort("NumCloned");
        }
    }

    private String getHiddenPower() {
        int intPower = 0;
        int a = this.hp % 2;
        int b = this.atk % 2;
        int c = this.def % 2;
        int d = this.spd % 2;
        int e = this.spatk % 2;
        int f = this.spdef % 2;
        double fedbca = 32 * f + 16 * e + 8 * d + 4 * c + 2 * b + a;
        int intTypeIndex = (int)Math.floor(fedbca * 15.0 / 63.0);
        String[] strTypes = new String[]{"Fighting", "Flying", "Poison", "Ground", "Rock", "Bug", "Ghost", "Steel", "Fire", "Water", "Grass", "Electric", "Psychic", "Ice", "Dragon", "Dark"};
        int u = 0;
        int v = 0;
        int w = 0;
        int x = 0;
        int y = 0;
        int z = 0;
        int tmp = this.hp % 4;
        if (tmp == 2 || tmp == 3) {
            u = 1;
        }
        if ((tmp = this.atk % 4) == 2 || tmp == 3) {
            v = 1;
        }
        if ((tmp = this.def % 4) == 2 || tmp == 3) {
            w = 1;
        }
        if ((tmp = this.spd % 4) == 2 || tmp == 3) {
            x = 1;
        }
        if ((tmp = this.spatk % 4) == 2 || tmp == 3) {
            y = 1;
        }
        if ((tmp = this.spdef % 4) == 2 || tmp == 3) {
            z = 1;
        }
        intPower = (int)Math.floor((double)(u + 2 * v + 4 * w + 8 * x + 16 * y + 32 * z) * 40.0 / 63.0 + 30.0);
        return String.valueOf(intPower) + " | " + strTypes[intTypeIndex];
    }

    public ItemStack getItem(Lot lot) {
        if(this.isEgg){
            ItemStack item = ItemStack.builder().itemType(ItemTypes.EGG).build();
            this.setItemData(item, lot.getLotID(), lot);
            return item;
        } else {
            net.minecraft.item.ItemStack nativeItem = new net.minecraft.item.ItemStack(PixelmonItems.itemPixelmonSprite);
            ItemStack item = setPicture(nativeItem);
            this.setItemData(item, lot.getLotID(), lot);
            return item;
        }
    }

    public EntityPixelmon getPokemon(Lot lot) {
        try {
            return (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(JsonToNBT.getTagFromJson(lot.getNBT()), FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld());
        } catch (NBTException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ItemStack setPicture(net.minecraft.item.ItemStack item){
        NBTTagCompound nbt = new NBTTagCompound();
        String idValue = String.format("%03d", id);
        if (this.shiny) {
            nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/shinypokemon/" + idValue + SpriteHelper.getSpriteExtra(this.name, this.form));
        } else {
            nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/pokemon/" + idValue + SpriteHelper.getSpriteExtra(this.name, this.form));
        }
        item.setTagCompound(nbt);
        return ItemStackUtil.fromNative(item);
    }

    private void setItemData(ItemStack item, int id, Lot lot) {
        if (!this.isEgg) {
            if (this.shiny) {
                item.offer(Keys.DISPLAY_NAME, Text.of(TextColors.AQUA, this.name + " ", TextColors.GRAY, "| ", TextColors.YELLOW, "Lvl " + this.lvl + " ", TextColors.GRAY, "(", TextColors.GOLD, "Shiny", TextColors.GRAY, ")"));
            } else {
                item.offer(Keys.DISPLAY_NAME, Text.of(TextColors.AQUA, this.name + " ", TextColors.GRAY, "| ", TextColors.YELLOW, "Lvl " + this.lvl));
            }
        } else {
            item.offer(Keys.DISPLAY_NAME, Text.of(TextColors.AQUA, "Unknown"));
        }

        List<Text> data = new ArrayList<>();
        if (this.isEgg) {
            data.add(Text.of(TextColors.GRAY, "Lot ID: ", TextColors.YELLOW, id));
            data.add(Text.of(TextColors.GRAY, "Owner: ", TextColors.YELLOW, this.owner));
            data.add(Text.of(TextColors.GREEN, "Click on this slot for more info"));
            data.add(Text.EMPTY);
            data.add(Text.of(TextColors.GRAY, "Ability: ", TextColors.YELLOW, "???"));
            data.add(Text.of(TextColors.GRAY, "Nature: ", TextColors.YELLOW, "???"));
            data.add(Text.EMPTY);
            data.add(Text.of(TextColors.GRAY, "Gender: ", TextColors.YELLOW, "???"));
            data.add(Text.of(TextColors.GRAY, "Size: ", TextColors.YELLOW, "???"));
            data.add(Text.EMPTY);

            if(this.cost > 0 && this.startPrice <= 0)
                data.add(Text.of(TextColors.GRAY, "Cost: ", TextColors.YELLOW, GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toPlain() + this.cost));
            else if(this.cost <= 0 && this.startPrice > 0) {
                data.add(Text.of(TextColors.GRAY, "Current Bid: ", TextColors.YELLOW, GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toPlain() + this.startPrice));
                data.add(Text.of(TextColors.GRAY, "Increment: ", TextColors.YELLOW, GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toPlain() + this.increment));
                data.add(Text.of(TextColors.GRAY, "High Bidder: ", TextColors.YELLOW, lot.getHighBidder() != null ?
                        Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(lot.getOwner()).get().getName()
                        : "N/A"));
            } else {
                data.add(Text.of(TextColors.GRAY, "Looking for: ", TextColors.YELLOW, lot.getPokeWanted()));
            }

            if(!lot.canExpire() && lot.getPokeWanted() != null)
                data.add(Text.of(TextColors.GRAY, "Expires: ", TextColors.YELLOW, "Never"));
            else
                data.add(Text.of(TextColors.GRAY, "Time Left: ", TextColors.YELLOW, LotUtils.getTime(GTS.getInstance().getSql()
                    .getEnd(lot.getLotID()).toInstant().toEpochMilli() - Instant.now().toEpochMilli())));
        } else {
            data.add(Text.of(TextColors.GRAY, "Lot ID: ", TextColors.YELLOW, id));
            data.add(Text.of(TextColors.GRAY, "Owner: ", TextColors.YELLOW, this.owner));
            if(!this.name.equals(this.nickname)) {
                data.add(Text.EMPTY);
                data.add(Text.join(Text.of(TextColors.GRAY, "Nickname: ", TextColors.YELLOW), TextSerializers.LEGACY_FORMATTING_CODE.deserialize(this.nickname)));
            }
            data.add(Text.of(TextColors.GREEN, "Click on this slot for more info"));
            if(lot.getNote() != null && !lot.getNote().equals("")){
                if(lot.getNote().length() > 16){
                    String[] note = lot.getNote().split(" ");
                    String line = "";

                    int index = 0;
                    while(line.length() < 17){
                        line += note[index] + " ";
                        index++;
                    }
                    data.add(Text.of(TextColors.GRAY, "Note: ", TextColors.YELLOW, line));
                    while(index < note.length){
                        line = "";
                        while(line.length() < 17){
                            if(index == note.length) break;

                            line += note[index] + " ";
                            index++;
                        }
                        data.add(Text.of(TextColors.GRAY, "      ", TextColors.YELLOW, line));
                    }
                } else {
                    data.add(Text.of(TextColors.GRAY, "Note: ", TextColors.YELLOW, lot.getNote()));
                }
            }
            data.add(Text.EMPTY);
            data.add(Text.of(TextColors.GRAY, "Ability: ", TextColors.YELLOW, this.ability));
            data.add(Text.of(TextColors.GRAY, "Nature: ", TextColors.YELLOW, this.nature));
            data.add(Text.EMPTY);
            data.add(Text.of(TextColors.GRAY, "Holding: ", TextColors.YELLOW, this.heldItem));
            data.add(Text.of(TextColors.GRAY, "Gender: ", TextColors.YELLOW, this.gender));
            data.add(Text.of(TextColors.GRAY, "Size: ", TextColors.YELLOW, this.growth));
            if(this.name.equalsIgnoreCase("Mew")){
                data.add(Text.EMPTY);
                data.add(Text.of(TextColors.GRAY, "Clones: ", TextColors.YELLOW, this.clones + " times"));
            }
            data.add(Text.EMPTY);
            if(this.cost > 0 && this.startPrice <= 0)
                data.add(Text.of(TextColors.GRAY, "Cost: ", TextColors.YELLOW, GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toPlain() + this.cost));
            else if(this.cost <= 0 && this.startPrice > 0) {
                data.add(Text.of(TextColors.GRAY, "Current Bid: ", TextColors.YELLOW, GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toPlain() + this.startPrice));
                data.add(Text.of(TextColors.GRAY, "Increment: ", TextColors.YELLOW, GTS.getInstance().getEconomy().getDefaultCurrency().getSymbol().toPlain() + this.increment));
                data.add(Text.of(TextColors.GRAY, "High Bidder: ", TextColors.YELLOW, lot.getHighBidder() != null ?
                        Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(lot.getOwner()).get().getName()
                        : "N/A"));
            } else {
                data.add(Text.of(TextColors.GRAY, "Looking for: ", TextColors.YELLOW, lot.getPokeWanted()));
            }
            if(!lot.canExpire() && lot.getPokeWanted() != null)
                data.add(Text.of(TextColors.GRAY, "Expires: ", TextColors.YELLOW, "Never"));
            else
                data.add(Text.of(TextColors.GRAY, "Time Left: ", TextColors.YELLOW, LotUtils.getTime(GTS.getInstance().getSql()
                        .getEnd(lot.getLotID()).toInstant().toEpochMilli() - Instant.now().toEpochMilli())));
        }

        item.offer(Keys.ITEM_LORE, data);
    }

    public ItemStack setStats(){
        ItemStack button = ItemStack.builder()
                .itemType(ItemTypes.MAP)
                .build();
        button.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GRAY, "More Stats"));

        List<Text> data = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("##0.00");

        if (this.isEgg) {
            int totalIvs = this.hp + this.atk + this.def + this.spatk + this.spdef + this.spd;
            double ivPercent = totalIvs / 186.0 * 100;

            data.add(Text.EMPTY);
            data.add(Text.of(TextColors.GRAY, "IVs: ", TextColors.YELLOW, totalIvs, TextColors.GRAY, "/", TextColors.YELLOW, "186  ", TextColors.GRAY, "(", TextColors.GREEN, df.format(ivPercent), "%", TextColors.GRAY, ")"));
            data.add(Text.of(TextColors.GRAY, "     HP     = ", TextColors.YELLOW, this.hp));
            data.add(Text.of(TextColors.GRAY, "     ATK    = ", TextColors.YELLOW, this.atk));
            data.add(Text.of(TextColors.GRAY, "     DEF    = ", TextColors.YELLOW, this.def));
            data.add(Text.of(TextColors.GRAY, "     SPATK = ", TextColors.YELLOW, this.spatk));
            data.add(Text.of(TextColors.GRAY, "     SPDEF = ", TextColors.YELLOW, this.spdef));
            data.add(Text.of(TextColors.GRAY, "     SPEED = ", TextColors.YELLOW, this.spd));
        } else {

            int totalEvs = this.evhp + this.evatk + this.evdef + this.evspatk + this.evspdef + this.evspd;
            int totalIvs = this.hp + this.atk + this.def + this.spatk + this.spdef + this.spd;

            double evPercent = totalEvs / 510.0 * 100;
            double ivPercent = totalIvs / 186.0 * 100;

            data.add(Text.EMPTY);
            data.add(Text.of(TextColors.GRAY, "EVs: ", TextColors.YELLOW, totalEvs, TextColors.GRAY, "/", TextColors.YELLOW, "510  ", TextColors.GRAY, "(", TextColors.GREEN, df.format(evPercent), "%", TextColors.GRAY, ")"));
            data.add(Text.of(TextColors.GRAY, "IVs: ", TextColors.YELLOW, totalIvs, TextColors.GRAY, "/", TextColors.YELLOW, "186  ", TextColors.GRAY, "(", TextColors.GREEN, df.format(ivPercent), "%", TextColors.GRAY, ")"));
            data.add(Text.of(TextColors.GRAY, "     HP     = ", TextColors.YELLOW, this.evhp, TextColors.GRAY, "/", TextColors.YELLOW, this.hp));
            data.add(Text.of(TextColors.GRAY, "     ATK    = ", TextColors.YELLOW, this.evatk, TextColors.GRAY, "/", TextColors.YELLOW, this.atk));
            data.add(Text.of(TextColors.GRAY, "     DEF    = ", TextColors.YELLOW, this.evdef, TextColors.GRAY, "/", TextColors.YELLOW, this.def));
            data.add(Text.of(TextColors.GRAY, "     SPATK = ", TextColors.YELLOW, this.evspatk, TextColors.GRAY, "/", TextColors.YELLOW, this.spatk));
            data.add(Text.of(TextColors.GRAY, "     SPDEF = ", TextColors.YELLOW, this.evspdef, TextColors.GRAY, "/", TextColors.YELLOW, this.spdef));
            data.add(Text.of(TextColors.GRAY, "     SPEED = ", TextColors.YELLOW, this.evspd, TextColors.GRAY, "/", TextColors.YELLOW, this.spd));
            data.add(Text.EMPTY);
            data.add(Text.of(TextColors.GRAY, "Moves:"));
            if(!this.m1.equals("Empty")) data.add(Text.of(TextColors.YELLOW, "    " + this.m1));
            if(!this.m2.equals("Empty")) data.add(Text.of(TextColors.YELLOW, "    " + this.m2));
            if(!this.m3.equals("Empty")) data.add(Text.of(TextColors.YELLOW, "    " + this.m3));
            if(!this.m4.equals("Empty")) data.add(Text.of(TextColors.YELLOW, "    " + this.m4));
        }
        button.offer(Keys.ITEM_LORE, data);
        return button;
    }

    int[] getId() {
        return new int[]{this.id1, this.id2};
    }

    public void setName(String s){
        this.name = s;
    }

    public String getOwner() {
        return owner;
    }

    public String getName(){
        return name;
    }

    public void setPrice(int price){
        this.cost = price;
    }

    public void setStPrice(int stPrice){
        this.startPrice = stPrice;
    }

    public void setIncrement(int increment){
        this.increment = increment;
    }

}