package net.impactdev.gts.sponge.utils;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;

public interface SpongeMenuOpener {

    default void open(Runnable runnable) {
        Sponge.server().scheduler().submit(Task.builder()
                .delay(Ticks.single())
                .plugin(Sponge.pluginManager().plugin("gts").orElseThrow(IllegalStateException::new))
                .execute(runnable)
                .build()
        );
    }

}
