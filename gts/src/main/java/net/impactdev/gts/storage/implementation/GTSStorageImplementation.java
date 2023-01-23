package net.impactdev.gts.storage.implementation;

import net.impactdev.gts.api.elements.listings.Listing;
import net.impactdev.impactor.api.utility.printing.PrettyPrinter;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public interface GTSStorageImplementation {

    PrettyPrinter.IPrettyPrintable meta();

    List<Listing> listings() throws Exception;

    void publishListing(Listing listing) throws Exception;

    void deleteListing(UUID uuid) throws Exception;

}
