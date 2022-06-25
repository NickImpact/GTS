package net.impactdev.gts.api.listings.entries;

import com.google.common.collect.ImmutableMap;
import io.leangen.geantyref.TypeToken;
import net.impactdev.gts.api.commands.CommandGenerator;
import net.impactdev.gts.api.data.ResourceManager;
import net.impactdev.gts.api.listings.ui.EntrySelection;
import net.impactdev.gts.api.listings.ui.EntryUI;
import net.impactdev.gts.api.util.Version;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;

import java.util.function.Supplier;

public interface EntryManager<T> extends ResourceManager<T> {

    default TypeToken<T> type() {
        return new TypeToken<T>() {};
    }

    /**
     * For use in configuration, determines how the blacklist should represent blacklisted options
     * for this entry typing.
     *
     * @return The type of class this entry validates against
     */
    Class<?> getBlacklistType();

    /**
     * The UI that a user will use to create a listing specific to the particular Entry type. These
     * allow for customization of the selling menu, but it is advised you keep the components of the UI
     * similar so a player is not easily confused.
     *
     * @return The UI responsible for creating a new listing based on the type managed by this Entry Manager
     */
    Supplier<EntryUI<?>> getSellingUI(PlatformPlayer player);

    /**
     * Supplies a set of deserializer options for the given entry type. This is where you can allow for multiple
     * versions of deserialization based on the data being read through JSON.
     */
    void supplyDeserializers();

    /**
     * Represents the executor that will handle processing of creating an entry from a command context.
     * This will be queried and attached as a child to the sell command at time of construction for the sell command.
     * To ensure readiness, this should be available before enable/initialization.
     *
     * @return The executor for the entry type when combined with /gts sell
     * @since 6.1.8
     */
    CommandGenerator.EntryGenerator<? extends EntrySelection<? extends Entry<?, ?>>> getEntryCommandCreator();

    /**
     * Checks to see if the incoming data is supported on the current game platform.
     *
     * @param game
     * @param content
     * @return
     */
    boolean supports(Version game, int content);

}
