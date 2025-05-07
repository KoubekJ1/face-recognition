package com.koubek.gpio;

import java.io.NotActiveException;
import java.util.LinkedList;

import javax.swing.Timer;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.exception.InitializeException;

/**
 * GPIO manager serves as a way to statically manage all GPIO devices
 */
public abstract class GPIOManager {
    private static Context pi4j;
    private static LinkedList<IDevice> devices = new LinkedList<>();
    private static Timer blinkingTimer;
    private static boolean currentState;

    /**
     * Initializes the GPIO manager
     */
    public static void init() {
        pi4j = Pi4J.newAutoContext();
        System.out.println();
        blinkingTimer = new Timer(250, e -> {
            for (IDevice device : devices) {
                if (device.isBlinkingDevice()) device.setState(!device.getState());
            }
        } );
    }

    /**
     * Adds a new GPIO device to the manager.
     * @param device GPIO device
     */
    public static void addDevice(IDevice device) {
        if (pi4j == null) throw new RuntimeException("Pi4J has not been initialized!");
        devices.add(device);
    }

    /**
     * Returns the static Pi4J context
     * @return Pi4J
     */
    public static Context getContext()
    {
        if (pi4j == null) throw new RuntimeException("Pi4J has not been initialized!");
        return pi4j;
    }

    /**
     * Sets the state of all added GPIO devices
     * @param newState the new state
     */
    public static void setState(boolean newState) {
        if (pi4j == null) throw new RuntimeException("Pi4J has not been initialized!");
        currentState = newState;
        for (IDevice device : devices) {
            device.setState(newState);
        }
    }

    /**
     * Sets whether signaling devices should be signaling
     * @param signal
     */
    public static void setBlinkDevices(boolean signal) {
        if (signal) {
             blinkingTimer.start();
        } else {
            for (IDevice device : devices) {
                device.setState(currentState);
            }
            blinkingTimer.stop();
        }
    }

    /**
     * Shuts down the GPIO manager
     */
    public static void shutdown() {
        if (pi4j != null) {
            setState(false);
            try {
                Thread.sleep(3000);
            } catch (Exception e) {}
            pi4j.shutdown();
        }
    }
}
