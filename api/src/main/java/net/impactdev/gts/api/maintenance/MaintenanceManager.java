package net.impactdev.gts.api.maintenance;

/**
 *
 */
public interface MaintenanceManager {

    /**
     * Fetches the current state on a mode for GTS.
     *
     * @param mode The mode we are requesting the state of
     * @return The marked status for this mode, with false indicating the mode is active
     */
    boolean getState(MaintenanceMode mode);

    /**
     * Sets the state of the specified maintenance mode to the desired setting.
     *
     * @param mode The mode to update
     * @param state The state to set the mode to
     */
    void setState(MaintenanceMode mode, boolean state);

    /**
     * Toggles the status state of the specified maintenance mode.
     *
     * @param mode The mode we are toggling
     */
    default void toggleState(MaintenanceMode mode) {
        this.setState(mode, !this.getState(mode));
    }

}
