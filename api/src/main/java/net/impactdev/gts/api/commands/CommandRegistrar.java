package net.impactdev.gts.api.commands;

public interface CommandRegistrar<C, E extends GTSCommandExecutor<?, ?, C>> {

    void register(E executor);

}
