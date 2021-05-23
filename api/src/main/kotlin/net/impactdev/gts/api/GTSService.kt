package net.impactdev.gts.api

import com.google.common.collect.ImmutableList
import net.impactdev.gts.api.data.registry.GTSComponentManager
import net.impactdev.gts.api.data.translators.DataTranslatorManager
import net.impactdev.gts.api.extension.Extension
import net.impactdev.gts.api.maintenance.MaintenanceManager
import net.impactdev.gts.api.messaging.message.errors.ErrorCode
import net.impactdev.gts.api.player.PlayerSettingsManager
import net.impactdev.gts.api.searching.Searcher

interface GTSService {
    /**
     * Returns an unmodifiable list of extensions currently hooked and running with GTS
     *
     * @return An immutable list of all loaded extensions hooked to GTS
     */
    val allExtensions: ImmutableList<Extension?>?

    /**
     * Represents the general component manager for the key parts of GTS (Listings, Entry Types, and Prices).
     * This is where you can register your own custom types of data
     *
     * @return The component manager responsible for managing the types of Listings,
     * Entries, and Prices GTS can manipulate.
     */
    val gTSComponentManager: GTSComponentManager?

    /**
     * Represents the location of all user-specific player settings.
     *
     * @return A mapping manager of all individual player settings
     */
    val playerSettingsManager: PlayerSettingsManager?

    /**
     * Specifies settings stating if the plugin itself is in maintenance mode, or a specific feature is disabled.
     *
     *
     * Users can use these settings to ultimately disable an entire feature of GTS, should they believe a bug
     * has been found or even other reasons.
     *
     * @return The manager controlling settings regarding GTS maintenance status states
     */
    val maintenanceManager: MaintenanceManager?
    val dataTranslatorManager: DataTranslatorManager?

    /**
     * Registers a searching option for all listings in the listing manager.
     *
     * @param searcher The searcher
     */
    fun addSearcher(searcher: Searcher?)

    /**
     * The set of registered searchers that a user can use to find listings matching their query.
     *
     * @return Every registered searcher
     */
    val searchers: List<Searcher?>?

    /**
     * Indicates whether or not the plugin has been set into safe mode. Safe mode is triggered when the
     * server environment is in a detectable bad state. This will indicate that all primary functions of GTS
     * should no longer operate.
     *
     * @return True if the plugin is in safe mode, false otherwise
     */
    val isInSafeMode: Boolean

    /**
     * Represents the error code for the triggering of safe mode. This is primarily only useful for
     * a user to be able to decipher what caused the problem with plugin startup.
     *
     * @return The error code that caused safe mode to be triggered.
     */
    val safeModeReason: ErrorCode?

    companion object {
        @kotlin.jvm.JvmStatic
        val instance: GTSService?
            get() = GTSServiceProvider.Companion.get()
    }
}