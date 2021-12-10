package net.impactdev.gts.api.commands;

import com.google.common.collect.Lists;
import net.impactdev.gts.api.commands.annotations.Alias;
import net.impactdev.gts.api.commands.annotations.Permission;

import java.util.List;

public interface GTSCommandExecutor<E, S> {

    void register();

    default List<String> getAliases() {
        return Lists.newArrayList(this.getClass().getAnnotation(Alias.class).value());
    }

    E[] getArguments();

    GTSCommandExecutor<E, S>[] getSubCommands();

    S build();

    default boolean hasNeededAnnotations() {
        return this.getClass().isAnnotationPresent(Alias.class) && this.getClass().isAnnotationPresent(Permission.class);
    }

}
