package com.koubek.gpio;

import com.koubek.Log;
import com.koubek.MessageType;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmType;

public class PWMDevice implements IDevice {

    private Pwm pwm;

    private int channel;
    private int frequency;

    private boolean oldState;

    public PWMDevice(int channel, int frequency)
    {
        if (frequency < 0) throw new IllegalArgumentException("Frequency must be higher than 0!");

        this.channel = channel;
        this.frequency = frequency;

        pwm = GPIOManager.getContext().create(Pwm.newConfigBuilder(GPIOManager.getContext()).address(channel).pwmType(PwmType.HARDWARE).provider("linuxfs-pwm").initial(0).shutdown(0).build());
    }

    @Override
    public void setState(boolean newState) {
        if (newState == oldState) return;
        oldState = newState;

        if (newState) {
            pwm.on(8.2, frequency);
        } else {
            pwm.on(13.2, frequency);
        }
    }
}
