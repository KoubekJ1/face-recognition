package com.koubek.gpio;

import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;

/**
 * DigitalOutputDevice instances serve as representations of digital output devices plugged into the GPIO pins.
 */
public class DigitalOutputDevice implements IDevice {

    private DigitalOutput output;

    private boolean blink;
    private boolean currentState;

    /**
     * Constructs the GPIO device with the given BCM pin address
     * This constructor sets the signaling device value as false
     * @param address BCM pin address
     */
    public DigitalOutputDevice(int address) {
        this(address, false);
    }

    /**
     * Constructs the GPIO device with the given BCM pin address and sets the signaling device value
     * @param address BCM pin address
     * @param signalingDevice signaling device
     */
    public DigitalOutputDevice(int address, boolean signalingDevice) {
        this.blink = signalingDevice;
        output = GPIOManager.getContext().dout().create(address);
        output.config().shutdownState(DigitalState.LOW);
        GPIOManager.setState(false);
    }

    /**
     * Sets the state of the digital state device
     */
    @Override
    public void setState(boolean newState) {
        if (newState == currentState) return;
        currentState = newState;
        if (newState) {
            output.state(DigitalState.HIGH);
        } else {
            output.state(DigitalState.LOW);
        }
    }

    /**
     * Returns whether the device is used for signaling imminent state disabling
     */
    @Override
    public boolean isBlinkingDevice() {
        return blink;
    }

    /**
     * Returns the current device state
     */
    @Override
    public boolean getState() {
        return currentState;
    }
}
