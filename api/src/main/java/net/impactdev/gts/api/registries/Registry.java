package net.impactdev.gts.api.registries;

public interface Registry {

    String name();

    void init() throws Exception;

}
