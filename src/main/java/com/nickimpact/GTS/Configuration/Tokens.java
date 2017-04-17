package com.nickimpact.GTS.Configuration;

/**
 * Created by nickd on 4/16/2017.
 */
public enum Tokens {

    // General Info
    Player("player"),
    Balance("balance"),
    Lot_Type("lot_type"),
    Expires("expires"),
    Price("price"),
    Currency_Symbol("curr_symbol"),
    StartingPrice("start_price"),
    Increment("increment"),
    PokeLookedFor("poke_looked_for"),
    Tax("tax"),
    Buyer("buyer"),
    Seller("seller"),
    Max_Pokemon("max_pokemon"),
    Slot("slot"),
    Cleared("cleared"),
    MoneyDiff("money_diff"),

    // Pokemon Info
    Pokemon("pokemon"),
    Nickname("nickname"),
    Ability("ability"),
    Gender("gender"),
    Growth("growth"),
    Nature("nature"),
    Level("level"),
    EV_Percentage("EV%"),
    EVTotal("evtotal"),
    EVHP("evhp"),
    EVAtk("evatk"),
    EVDef("evdef"),
    EVSpAtk("evspatk"),
    EVSpDef("evspdef"),
    EVSpeed("evspeed"),
    IV_Percentage("IV%"),
    IVTotal("ivtotal"),
    IVHP("ivhp"),
    IVAtk("ivatk"),
    IVDef("ivdef"),
    IVSpAtk("ivspatk"),
    IVSpDef("ivspdef"),
    IVSpeed("ivspeed"),
    Form("form"),
    Shiny("shiny"),
    Halloween("halloween"),
    Roasted("roasted"),

    // Inventory Tokens
    Page("page");

    private String token;

    private Tokens(String token){
        this.token = token;
    }

    public String getToken(){
        return this.token;
    }
}
