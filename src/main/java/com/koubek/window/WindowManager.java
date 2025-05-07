package com.koubek.window;

/**
 * WindowManager serves as a singleton way to access the face recognition window used by the program
 */
public class WindowManager {
    private static FaceRecognitionWindow window;

    /**
     * Returns the face recognition window.
     * If the window was not yet created, it gets created.
     * @return the face recognition window.
     */
    public static FaceRecognitionWindow getWindow()
    {
        if (window == null) window = new FaceRecognitionWindow();
        return window;
    }

    /**
     * Returns whether the face recognition window is currently displayed.
     * @return is window visible
     */
    public static boolean isWindowVisible() {
        return window != null && window.isVisible() == true;
    }
}
