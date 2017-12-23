package com.nickimpact.gts.trades;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class PokeRequest {

    private final String    name;
    private final String    ability;
    private final String    nature;
    private final String    growth;
    private final String    gender;
    private final String    pokeball;
    private final int       level;
    private final int       form;
    private final int[]     evs;
    private final int[]     ivs;
    private final boolean   shiny;

    public PokeRequest(Builder builder) {
        this.name = builder.name;
        this.ability = builder.ability;
        this.nature = builder.nature;
        this.growth = builder.growth;
        this.gender = builder.gender;
        this.pokeball = builder.pokeball;
        this.level = builder.level;
        this.form = builder.form;
        this.evs = builder.evs;
        this.ivs = builder.ivs;
        this.shiny = builder.shiny;
    }

    public String getPokemon() {
        return name;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;

        private String ability;

        private String nature;

        private String growth;

        private String gender;

        private String pokeball;

        private int level;

        private int form;

        private int[] evs;

        private int[] ivs;

        private boolean shiny;

        public Builder pokemon(String name) {
            this.name = name;
            return this;
        }

        public Builder ability(String ability) {
            this.ability = ability;
            return this;
        }

        public Builder nature(String nature) {
            this.nature = nature;
            return this;
        }

        public Builder growth(String growth) {
            this.growth = growth;
            return this;
        }

        public Builder gender(String gender) {
            this.gender = gender;
            return this;
        }

        public Builder pokeball(String pokeball) {
            this.pokeball = pokeball;
            return this;
        }

        public Builder level(int level) {
            this.level = level;
            return this;
        }

        public Builder form(int form) {
            this.form = form;
            return this;
        }

        public Builder evs(int[] evs) {
            this.evs = evs;
            return this;
        }

        public Builder ivs(int[] ivs) {
            this.ivs = ivs;
            return this;
        }

        public Builder shiny(boolean shiny) {
            this.shiny = shiny;
            return this;
        }

        public PokeRequest build() {
            return new PokeRequest(this);
        }
    }
}
