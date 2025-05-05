package com.koubek.window;

public class WindowManager {
    private static FaceRecognitionWindow window;

    public static FaceRecognitionWindow getWindow()
    {
        if (window == null) window = new FaceRecognitionWindow();
        return window;
    }

    public static boolean isWindowVisible() {
        return window != null && window.isVisible() == true;
    }
}
