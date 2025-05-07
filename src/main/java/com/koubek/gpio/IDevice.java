package com.koubek.gpio;

/**
 * IDevice instances serve as representations of GPIO devices
 * All GPIO devices then have this core functionality
 */
public interface IDevice {
    /**
     * Sets the state of the GPIO device
     * @param newState the new state
     */
    public void setState(boolean newState);

    /**
     * Returns the current state of the GPIO device
     * @return the current state
     */
    public boolean getState();

    /**
     * Returns whether the device is used for signaling imminent device disabling
     * @return blinking device
     */
    public boolean isBlinkingDevice();
}
