package com.koubek.gpio;

import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;

public class DigitalOutputDevice implements IDevice {

    private DigitalOutput output;

    public DigitalOutputDevice(int address) {
        output = GPIOManager.getContext().dout().create(address);
        output.config().shutdownState(DigitalState.HIGH);
        GPIOManager.setState(false);
    }

    @Override
    public void setState(boolean newState) {
        if (newState) {
            output.state(DigitalState.HIGH);
        } else {
            output.state(DigitalState.LOW);
        }
    }
}
