package com.nickimpact.GTS.storage.rework;

import com.nickimpact.GTS.utils.LotCache;

import java.util.List;

public interface Storage {

    /**
     * This method is to initialize our storage provider, loading anything necessary
     * about the provider.
     *
     * @throws Exception Occurs on any failure to properly load during initialization
     */
    void init() throws Exception;

    /**
     * This method is to ensure we properly close our connection when the plugin is either
     * no longer in use or in the middle of server shutdown.
     *
     * @throws Exception Occurs on any failure to properly close off the storage provider
     */
    void shutdown() throws Exception;

    /**
     * Fetches a list of all lots from the storage provider, and forms them into a list
     * to represent the cached listings.
     */
    void getLots();

    /**
     * This method is meant to clean out the GTS, along with logs if the passed variable
     * is <code>true</code>.
     *
     * @return <code>true</code> on successful purge, <code>false</code> otherwise
     */
    boolean purge(boolean logs);

    /**
     * Fetches all logs within the storage data, and adds them to the GTS log cache
     */
    void getLogs();

    /**
     * Attempts to save all data awaiting an update to the storage provider. However, for flatfile,
     * we will need to ensure we have all data present at time of save to ensure all is saved
     * properly.
     *
     * @return <code>true</code> on successful save, <code>false</code> otherwise
     */
    boolean save();
}
