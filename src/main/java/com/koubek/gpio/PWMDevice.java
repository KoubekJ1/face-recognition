package com.koubek.gpio;

import com.koubek.Log;
import com.koubek.MessageType;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmType;


/**
 * PWMDevice instances serve as representations of GPIO PWM devices
 */
public class PWMDevice implements IDevice {

    private Pwm pwm;

    private int frequency;
    private double dutyCycleOn;
    private double dutyCycleOff;

    private boolean currentState;

    private boolean blink;

    /**
     * Constructs a new PWM device with the given parameters
     * This constructor sets the signaling device value as false
     * @param channel PWM channel of the pin
     * @param frequency PWM frequency
     * @param dutyCycleOn duty cycle for on state
     * @param dutyCycleOff duty cycle for off state
     */
    public PWMDevice(int channel, int frequency, double dutyCycleOn, double dutyCycleOff)
    {
        this(channel, frequency, dutyCycleOn, dutyCycleOff, false);
    }

    /**
     * Constructs a new PWM device with the given parameters
     * @param channel PWM channel of the pin
     * @param frequency PWM frequency
     * @param dutyCycleOn duty cycle for on state
     * @param dutyCycleOff duty cycle for off state
     * @param signalingDevice whether the device is used for signaling imminent device disabling
     */
    public PWMDevice(int channel, int frequency, double dutyCycleOn, double dutyCycleOff, boolean signalingDevice)
    {
        if (frequency < 0) throw new IllegalArgumentException("Frequency must be higher than 0!");

        this.frequency = frequency;
        this.dutyCycleOn = dutyCycleOn;
        this.dutyCycleOff = dutyCycleOff;
        this.blink = signalingDevice;

        pwm = GPIOManager.getContext().create(Pwm.newConfigBuilder(GPIOManager.getContext()).address(channel).pwmType(PwmType.HARDWARE).provider("linuxfs-pwm").initial(0).shutdown(0).build());
    }

    /**
     * Sets the state of the PWM device
     */
    @Override
    public void setState(boolean newState) {
        if (newState == currentState) return;
        currentState = newState;

        if (newState) {
            pwm.on(dutyCycleOn, frequency);
        } else {
            pwm.on(dutyCycleOff, frequency);
        }
    }

    /**
     * Returns whether the device is used for signaling imminent device disabling
     * @return blinking device
     */
    @Override
    public boolean isBlinkingDevice() {
        return blink;
    }

    /**
     * Returns the current state of the GPIO device
     * @return the current state
     */
    @Override
    public boolean getState() {
        return currentState;
    }
}
