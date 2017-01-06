package com.nickimpact.GTS.Utils;

/**
 * Created by Nick on 12/15/2016.
 */
public class Lot {

    private int lotID = -1;

    private String owner;
    private String nbt;

    private PokemonItem pkItem;

    private int price;

    public Lot(int lotID, String owner, String nbt, PokemonItem item, int price){
        this.lotID = lotID;
        this.owner = owner;
        this.nbt = nbt.replace("CustomName:\"\"", "CustomName:").replace("Nickname:\"\"", "Nickname:");
        this.pkItem = item;
        this.price = price;
    }

    public int getPrice(){
        return this.price;
    }

    public String getOwner(){
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
}
