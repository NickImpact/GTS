package com.nickimpact.GTS.guis;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.GTSInfo;
import com.nickimpact.GTS.guis.builder.BuilderBase;
import com.nickimpact.GTS.utils.LotCache;
import com.nickimpact.GTS.utils.LotUtils;
import com.nickimpact.GTS.utils.PokeRequest;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.Enchantments;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class TradePartySelection extends InventoryBase {

    private Player player;
    private LotCache lot;
    private PokeRequest pr;

    private PlayerStorage storage;

    public TradePartySelection(Player player, LotCache lot) {
        super(5, Text.of(
                TextColors.RED, "GTS", TextColors.DARK_GRAY, " \u00bb ", TextColors.DARK_GREEN, "Select Pokemon"
        ));

        this.player = player;
        this.lot = lot;
        this.pr = new Gson().fromJson(lot.getLot().getPokeWanted(), PokeRequest.class);

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

        this.drawBorder(5, DyeColors.BLACK);
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
                    this.player.closeInventory(Cause.of(NamedCause.source(GTS.getInstance())));

                    this.player.openInventory(new LotUI(this.player, this.lot, true, slot).getInventory(),
                                              Cause.of(NamedCause.source(GTS.getInstance())));
                }).delayTicks(1).submit(GTS.getInstance());
            });
        }

        return icon;
    }
}
