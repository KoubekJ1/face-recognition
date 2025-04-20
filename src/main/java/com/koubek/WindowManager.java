package com.koubek;

public class WindowManager {
    private static FaceRecognitionWindow window;

    public static FaceRecognitionWindow getWindow() {
        if (window == null) window = new FaceRecognitionWindow();
        return window;
    }
}
