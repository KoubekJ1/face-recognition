package com.koubek.gpio;

import java.io.NotActiveException;
import java.util.LinkedList;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.exception.InitializeException;

public abstract class GPIOManager {
    private static Context pi4j;
    private static LinkedList<IDevice> devices = new LinkedList<>();

    public static void init() {
        pi4j = Pi4J.newAutoContext();
        System.out.println();
    }

    public static void addDevice(IDevice device) {
        if (pi4j == null) throw new RuntimeException("Pi4J has not been initialized!");
        devices.add(device);
    }

    public static Context getContext()
    {
        if (pi4j == null) throw new RuntimeException("Pi4J has not been initialized!");
        return pi4j;
    }

    public static void setState(boolean newState) {
        if (pi4j == null) throw new RuntimeException("Pi4J has not been initialized!");
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
