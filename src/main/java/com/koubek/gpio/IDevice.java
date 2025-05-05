package com.koubek.gpio;

public interface IDevice {
    public void setState(boolean newState);
    public boolean getState();
    public boolean isBlinkingDevice();
}
