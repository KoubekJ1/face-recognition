package com.koubek.gpio;

import java.util.LinkedList;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;

public abstract class GPIOManager {
    private static Context pi4j;
    private static LinkedList<IDevice> devices = new LinkedList<>();

    public static void init() {
        pi4j = Pi4J.newAutoContext();
        System.out.println();
    }

    public static void addDevice(IDevice device) {
        devices.add(device);
    }

    public static Context getContext()
    {
        return pi4j;
    }

    public static void setState(boolean newState) {
        for (IDevice device : devices) {
            device.setState(newState);
        }
    }

    public static void shutdown() {
        if (pi4j != null) {
            setState(false);
            pi4j.shutdown();
        }
    }
}
