package com.nickimpact.GTS.utils;

import net.minecraft.nbt.NBTTagCompound;

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
    private int stPrice;
    private int increment;

    private boolean pokemon = false;
    private String pokeWanted = "";

    private String note = "";

    // Legacy Constructor
    public Lot(int lotID, UUID owner, NBTTagCompound nbt, PokemonItem item, int price){
        this.lotID = lotID;
        this.owner = owner;
        this.nbt = GsonUtils.serialize(nbt);
        this.pkItem = item;
        this.price = price;

        // Initialize new values
        this.expires = true;
        this.auction = false;
        this.highBidder = null;
        this.stPrice = -1;
        this.increment = -1;
        this.pokeWanted = "None";
        this.note = null;
    }

    public Lot(int lotID, UUID owner, NBTTagCompound nbt, PokemonItem item, int price, boolean expires, String note){
        this(lotID, owner, nbt, item, price, expires, false, null, -1, -1, note);
    }

    public Lot(int lotID, UUID owner, NBTTagCompound nbt, PokemonItem item, int price, boolean expires, boolean auction, UUID highBidder, int stPrice, int increment, String note){
        this.lotID = lotID;
        this.owner = owner;
        this.nbt = GsonUtils.serialize(nbt);
        this.pkItem = item;
        this.price = price;
        this.expires = expires;
        this.auction = auction;
        this.highBidder = highBidder;
        this.stPrice = stPrice;
        this.increment = increment;
        this.note = note;
    }

    public Lot(int lotID, UUID owner, NBTTagCompound nbt, PokemonItem item, boolean pokemon, String pokeWanted, String note){
        this.lotID = lotID;
        this.owner = owner;
        this.nbt = GsonUtils.serialize(nbt);
        this.pkItem = item;
        this.pokemon = pokemon;
        this.pokeWanted = pokeWanted;
        this.note = note;
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

    public boolean isTrade() {
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
