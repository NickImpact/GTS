package net.impactdev.gts.api.logging;

import net.impactdev.gts.api.elements.Element;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.utility.builders.Builder;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.UUID;

public interface LogEntry {

    static LogEntryBuilder builder() {
        return Impactor.instance().builders().provide(LogEntryBuilder.class);
    }

    /**
     * Gets the instant this particular entry occurred.
     *
     * @return The instant timestamp
     */
    @NotNull Instant timestamp();

    /**
     * Specifies the source of this entry. In other words, this is the user
     * which provoked a new logged element.
     *
     * @return The source of this logged entry
     */
    @NotNull Source source();

    /**
     * Specifies the target of an action performed by the source, and detailed
     * with an associated {@link LogAction}
     *
     * @return The target element
     */
    @NotNull Element target();

    /**
     * The action performed to create the log entry.
     *
     * @return The performed action
     */
    @NotNull LogAction action();

    /**
     * Represents the source of a log entry.
     */
    interface Source {

        /**
         * Gets the source unique id.
         *
         * @return the source unique id
         */
        @NotNull UUID getUniqueId();

        /**
         * Gets the source name.
         *
         * @return the source name
         */
        @NotNull String getName();

    }

    interface LogEntryBuilder extends Builder<LogEntry> {

        LogEntryBuilder timestamp(final @NotNull Instant timestamp);

        LogEntryBuilder source(final @NotNull Source source);

        LogEntryBuilder target(final @NotNull Element target);

        LogEntryBuilder action(final @NotNull LogAction action);

    }

}
