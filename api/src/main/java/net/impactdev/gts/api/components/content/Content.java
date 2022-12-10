package net.impactdev.gts.api.components.content;

import net.impactdev.gts.api.storage.serialization.StorableContent;
import net.impactdev.impactor.api.items.ImpactorItemStack;
import net.impactdev.impactor.api.platform.sources.PlatformPlayer;
import net.impactdev.impactor.api.utilities.printing.PrettyPrinter;
import net.kyori.adventure.text.ComponentLike;

public interface Content<T> extends StorableContent, ComponentLike, PrettyPrinter.IPrettyPrintable {

    /**
     * Provides the actual component represented by this class. This is the actual item
     * offered on the market to other players.
     *
     * @return The instance of the object available to players.
     */
    T content();

    /**
     * Represents the means of displaying the content to the end user. For a listing, the content
     * acts as the front-facing display, and as such, content is expected to define how it is to
     * appear to a client.
     *
     * @return A provider responsible for creating the client-facing display
     */
    ImpactorItemStack display();

    /**
     * Attempts to reward the target player with the content provided by this object. Due to possible
     * limitations, this call can fail if the listing cannot be completely rewarded to the target.
     *
     * @param target The player that should receive the content
     * @return <code>true</code> if the content fulfillment was complete, <code>false</code> otherwise
     */
    boolean reward(PlatformPlayer target);

    /**
     * Attempts to find a matching element against the target player, and then removes the content
     * from their storage. If the target element can not be matched within the target's inventory,
     * this call will fail and return a <code>false</code> value to indicate this failure.
     *
     * @param target The player that should have the specified content removed from their storage
     * @return <code>true</code> if the content was removed successfully, <code>false</code> otherwise
     */
    boolean take(PlatformPlayer target);

}
