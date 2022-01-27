package net.impactdev.gts.api.commands;

import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.api.listings.ui.EntrySelection;

import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Function;

/**
 * This is a special executor, to be attached to the commands of sell and auction, where the components
 * don't return a command result, but rather an entry or a price. This is determined by the sub-interface
 * of this executor.
 */
public interface CommandGenerator<T> {

    /**
     * Represents the access codes for the generator. These are what a user must type on the command
     * line in order to access the generator.
     *
     * @return An array of string aliases
     */
    String[] getAliases();

    /**
     * Parses the command line to create the type represented by this generator. The system provides
     * the source casting the command through their UUID, the argument string represented by a queue,
     * and additional context that might be needed for a redirection if necessary. Redirection
     * should only be necessary if the argument string cannot supply the argument correctly. Typical
     * design would have it such that redirection only occurs if the arguments are empty at the time of
     * invoking.
     *
     * If redirection is necessary, you need to invoke {@link Context#redirected()} to allow the command
     * executor to understand that redirection has occurred.
     *
     * @param source The source that is casting the command
     * @param args The arguments still remaining on the argument queue
     * @param context Contextual information already filled by other components of the command
     * @return The object created by parsing the command line, or null if redirected.
     * @throws Exception If any component of the argument string should result in a command parsing exception
     */
    T create(UUID source, Queue<String> args, Context context) throws Exception;

    /**
     * Attempts to locate the next argument on the queue, and attempts to parse it using the translator
     * if available. Otherwise, if no argument is available, returns an empty optional.
     *
     * @param args The queue of arguments remaining on the command tree
     * @param translator The translator that will be used to parse the next argument if available
     * @param <E> The type the translator will attempt to decode the argument into
     * @return An optional wrapping the translated argument, or empty
     */
    default <E> Optional<E> next(Queue<String> args, Function<String, E> translator) {
        if(!args.isEmpty()) {
            return Optional.of(translator.apply(args.poll()));
        }

        return Optional.empty();
    }

    /**
     * Attempts to construct a translated argument, with the condition that an argument should be available
     * in the queue to translate from. If none are available, an exception will be raised to indicate the
     * failure of the requirement.
     *
     * @param args The queue of arguments remaining on the command context
     * @param translator The translator to convert from raw input to the desired type
     * @param <E> The type to create
     * @return A type representing the translation via the argument requirement
     * @throws IllegalStateException If no arguments are available in the queue
     */
    default <E> E require(Queue<String> args, Function<String, E> translator) throws IllegalStateException {
        return this.next(args, translator).orElseThrow(() -> new IllegalStateException("No available arguments"));
    }

    interface EntryGenerator<V extends EntrySelection<? extends Entry<?, ?>>> extends CommandGenerator<V> { }

    interface PriceGenerator<P extends Price<?, ?, ?>> extends CommandGenerator<P> { }

    /**
     * Indicates context regarding the current command processing for /gts sell or /gts auction.
     */
    interface Context {

        /**
         * Indicates the type of listing being created via this context.
         *
         * @return The type of listing being created, and how to apply it
         */
        Class<? extends Listing> type();

        /**
         * Represents the entry currently assigned to the command context, if set. This will only
         * be set if the command correctly parses an entry from the command line, and is not
         * redirected beforehand.
         *
         * This will never be set during entry processing of these commands.
         *
         * @return The entry selection for the command context
         */
        Optional<EntrySelection<?>> entry();

        /**
         * Sets the entry selection for the given context.
         *
         * @param entry The entry to set for the context
         */
        void entry(EntrySelection<?> entry);

        /**
         * Specifies how long the listing will be applied for. If not set, this will default to the
         * mid-range config time value.
         *
         * @return The amount of time the listing will be placed on the market for.
         */
        long time();

        /**
         * Sets the time indicating how long the listing will stay on the market for before expiring.
         *
         * @param time The time to list the listing for
         */
        void time(long time);


        /**
         * Checks if the command has been redirected to a UI prompt for the player. This will
         * happen if the user supplies a typing, but does not provide additional arguments.
         *
         * @return <code>true</code> if a prompt has been activated, <code>false</code> otherwise
         */
        boolean redirect();

        /**
         * Sets the context to a redirected state. This allows the command processor to know of the
         * redirection to avoid further processing on the command stack, and rather provide the context
         * to create the necessary components for the UI.
         */
        void redirected();

        /**
         * Represents additional context for an auction based listing.
         */
        interface AuctionContext extends Context {

            /**
             * Indicates the starting price set for an auction via the command line.
             *
             * @return The starting price for an auction
             */
            double start();

            /**
             * Sets the price an auction should start at when the auction is processed entirely off the
             * command line.
             *
             * @param start The price to start an auction at
             */
            void start(double start);

            /**
             * Indicates the percentage increment subsequent bids will need to abide by in order
             * to be placed. This simply acts as a minimum input and ensures the price rises in a steady
             * manner, rather than outbidding by a penny.
             *
             * @return The percentage increment for the auction
             */
            float increment();

            /**
             * Sets the percentage increment that an auction will adjust following bid requirements
             * by.
             *
             * @param increment The increment for the auction
             */
            void increment(float increment);

        }

    }

}
