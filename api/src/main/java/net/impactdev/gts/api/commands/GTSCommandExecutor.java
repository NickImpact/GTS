package net.impactdev.gts.api.commands;

import com.google.common.collect.Lists;
import net.impactdev.gts.api.commands.annotations.Alias;
import net.impactdev.gts.api.commands.annotations.Permission;
import net.impactdev.impactor.api.Impactor;

import java.util.List;

public interface GTSCommandExecutor<E, F, S> {

    default S register() {
        return this.build();
    }

    default List<String> aliases() {
        return Lists.newArrayList(this.getClass().getAnnotation(Alias.class).value());
    }

    E[] arguments();

    F[] flags();

    GTSCommandExecutor<E, F, S>[] children();

    S build();

    default boolean hasNeededAnnotations() {
        return this.getClass().isAnnotationPresent(Alias.class) && this.getClass().isAnnotationPresent(Permission.class);
    }

}
