package com.nickimpact.GTS.Utils;

import com.google.common.collect.Lists;
import org.spongepowered.api.entity.living.player.Player;

import java.util.List;
import java.util.UUID;

/**
 * Created by Nick on 12/15/2016.
 */
public class Lot {

    private int lotID = -1;

    private UUID owner;
    private String nbt;

    private PokemonItem pkItem;

    private int price;

    private boolean expires = true;

    private boolean auction = false;
    private UUID highBidder = null;
    private List<Player> aucListeners = Lists.newArrayList();
    private int stPrice;
    private int increment;

    private boolean pokemon = false;
    private String pokeWanted = "";

    public Lot(int lotID, UUID owner, String nbt, PokemonItem item, int price, boolean expires){
        this(lotID, owner, nbt, item, price, expires, false, null, -1, -1);
    }

    public Lot(int lotID, UUID owner, String nbt, PokemonItem item, int price, boolean expires, boolean auction, UUID highBidder, int stPrice, int increment){
        this.lotID = lotID;
        this.owner = owner;
        this.nbt = nbt.replace("CustomName:\"\"", "CustomName:").replace("Nickname:\"\"", "Nickname:");
        this.pkItem = item;
        this.price = price;
        this.expires = expires;
        this.auction = auction;
        this.highBidder = highBidder;
        this.stPrice = stPrice;
        this.increment = increment;
    }

    public Lot(int lotID, UUID owner, String nbt, PokemonItem item, boolean pokemon, String pokeWanted){
        this.lotID = lotID;
        this.owner = owner;
        this.nbt = nbt.replace("CustomName:\"\"", "CustomName:").replace("Nickname:\"\"", "Nickname:");
        this.pkItem = item;
        this.pokemon = pokemon;
        this.pokeWanted = pokeWanted;
    }

    public int getPrice(){
        return this.price;
    }

    public UUID getOwner(){
        return this.owner;
    }

    public PokemonItem getItem(){
        return this.pkItem;
    }

    public String getNBT(){
        return this.nbt;
    }

    public int getLotID(){
        return this.lotID;
    }

    public boolean isAuction() {
        return auction;
    }

    public boolean isPokemon() {
        return pokemon;
    }

    public String getPokeWanted() {
        return pokeWanted;
    }

    public UUID getHighBidder() {
        return highBidder;
    }

    public void setHighBidder(UUID highBidder) {
        this.highBidder = highBidder;
    }

    public List<Player> getAucListeners() {
        return aucListeners;
    }

    public void setAucListeners(List<Player> aucListeners) {
        this.aucListeners = aucListeners;
    }

    public int getStPrice() {
        return stPrice;
    }

    public void setStPrice(int stPrice) {
        this.stPrice = stPrice;
    }

    public int getIncrement() {
        return increment;
    }

    public void setIncrement(int increment) {
        this.increment = increment;
    }

    public boolean canExpire() {
        return expires;
    }
}
