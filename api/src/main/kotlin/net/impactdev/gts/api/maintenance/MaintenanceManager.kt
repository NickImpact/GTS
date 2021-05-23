package net.impactdev.gts.api.maintenance

/**
 *
 */
interface MaintenanceManager {
    /**
     * Fetches the current state on a mode for GTS.
     *
     * @param mode The mode we are requesting the state of
     * @return The marked status for this mode, with false indicating the mode is active
     */
    fun getState(mode: MaintenanceMode?): Boolean

    /**
     * Sets the state of the specified maintenance mode to the desired setting.
     *
     * @param mode The mode to update
     * @param state The state to set the mode to
     */
    fun setState(mode: MaintenanceMode?, state: Boolean)

    /**
     * Toggles the status state of the specified maintenance mode.
     *
     * @param mode The mode we are toggling
     */
    fun toggleState(mode: MaintenanceMode?) {
        setState(mode, !getState(mode))
    }
}