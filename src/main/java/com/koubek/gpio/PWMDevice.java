package com.koubek.gpio;

import com.koubek.Log;
import com.koubek.MessageType;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmType;

public class PWMDevice implements IDevice {

    private Pwm pwm;

    private int frequency;
    private double dutyCycleOn;
    private double dutyCycleOff;

    private boolean oldState;

    public PWMDevice(int channel, int frequency, double dutyCycleOn, double dutyCycleOff)
    {
        if (frequency < 0) throw new IllegalArgumentException("Frequency must be higher than 0!");

        this.frequency = frequency;
        this.dutyCycleOn = dutyCycleOn;
        this.dutyCycleOff = dutyCycleOff;

        pwm = GPIOManager.getContext().create(Pwm.newConfigBuilder(GPIOManager.getContext()).address(channel).pwmType(PwmType.HARDWARE).provider("linuxfs-pwm").initial(0).shutdown(0).build());
    }

    @Override
    public void setState(boolean newState) {
        if (newState == oldState) return;
        oldState = newState;

        if (newState) {
            pwm.on(dutyCycleOn, frequency);
        } else {
            pwm.on(dutyCycleOff, frequency);
        }
    }
}
