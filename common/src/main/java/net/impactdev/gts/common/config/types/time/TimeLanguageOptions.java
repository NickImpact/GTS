package net.impactdev.gts.common.config.types.time;

public class TimeLanguageOptions {

    private final String singular;
    private final String plural;

    public TimeLanguageOptions(String singular, String plural) {
        this.singular = singular;
        this.plural = plural;
    }

    public String getSingular() {
        return this.singular;
    }

    public String getPlural() {
        return this.plural;
    }
}
