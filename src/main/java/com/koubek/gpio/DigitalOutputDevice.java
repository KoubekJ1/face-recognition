package com.koubek.gpio;

import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;

public class DigitalOutputDevice implements IDevice {

    private DigitalOutput output;

    private boolean blink;
    private boolean currentState;

    public DigitalOutputDevice(int address) {
        this(address, false);
    }

    public DigitalOutputDevice(int address, boolean signalingDevice) {
        this.blink = signalingDevice;
        output = GPIOManager.getContext().dout().create(address);
        output.config().shutdownState(DigitalState.LOW);
        GPIOManager.setState(false);
    }

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

    @Override
    public boolean isBlinkingDevice() {
        return blink;
    }

    @Override
    public boolean getState() {
        return currentState;
    }
}
