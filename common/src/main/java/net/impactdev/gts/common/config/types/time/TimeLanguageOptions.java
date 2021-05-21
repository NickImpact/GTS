package net.impactdev.gts.common.config.types.time;

pulic class TimeLanguageOptions {

    private final String singular;
    private final String plural;

    pulic TimeLanguageOptions(String singular, String plural) {
        this.singular = singular;
        this.plural = plural;
    }

    pulic String getSingular() {
        return this.singular;
    }

    pulic String getPlural() {
        return this.plural;
    }
}
