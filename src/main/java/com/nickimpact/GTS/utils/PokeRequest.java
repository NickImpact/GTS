package com.nickimpact.GTS.utils;

public class PokeRequest {

    private String pokemon;

    private int level;
    private int form;
    private int[] evs;
    private int[] ivs;

    private boolean shiny;

    private String pokeball;
    private String growth;
    private String gender;
    private String ability;
    private String nature;

    public PokeRequest(String pokemon, int level, int form, int[] evs, int[] ivs,
                       boolean shiny,
                       String pokeball, String growth, String gender, String ability, String nature){

        this.pokemon = pokemon;
        this.level = level;
        this.form = form;
        this.evs = evs;
        this.ivs = ivs;
        this.shiny = shiny;
        this.pokeball = pokeball;
        this.growth = growth;
        this.gender = gender;
        this.ability = ability;
        this.nature = nature;
    }

    public String getPokemon() {
        return pokemon;
    }

    public int getLevel() {
        return level;
    }

    public int getForm() {
        return form;
    }

    public int[] getEvs() {
        return evs;
    }

    public int[] getIvs() {
        return ivs;
    }

    public boolean isShiny() {
        return shiny;
    }

    public String getPokeball() {
        return pokeball;
    }

    public String getGrowth() {
        return growth;
    }

    public String getAbility() {
        return ability;
    }

    public String getNature() {
        return nature;
    }

    public String getGender() {
        return gender;
    }
}
