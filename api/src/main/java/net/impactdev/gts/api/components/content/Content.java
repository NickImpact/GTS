package net.impactdev.gts.api.components.content;

import net.impactdev.impactor.api.ui.containers.icons.DisplayProvider;
import net.impactdev.impactor.api.utilities.printing.PrettyPrinter;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public interface Content<T> extends PrettyPrinter.IPrettyPrintable {

    /**
     * Provides the actual component represented by this class. This is the actual item
     * offered on the market to other players
     *
     * @return
     */
    T content();

    Component name();

    /**
     * Represents a provider for a component. This should provide the base
     *
     * @return
     */
    DisplayProvider display();

    boolean receive(UUID target);

    boolean take(UUID target);

}
