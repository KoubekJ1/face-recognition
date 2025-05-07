package com.koubek;

import com.koubek.gpio.GPIOManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Application class contains static methods used for starting, managing and exiting the app.
 */
public class Application {
    private static boolean debug;
    private static Camera camera;

    private static int frameChangeCount = 5;
    private static int disableDelay = 5;
    private static int maxConfidence = 100;

    /**
     * Starts the program and initializes all necessary objects, then proceeds to start the console thread
     * @param args Program arguments
     */
    public static void start(String[] args) {
        System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "ERROR");
        for (String arg : args) {
            switch (arg) {
                case "debug" -> {
                    debug = true;
                    System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");
                    Log.printMessage("Debug mode enabled!", MessageType.INFO);
                }
            }
        }
        GPIOManager.init();

        File lib = new File("opencv/libopencv_java4110.so");
        System.load(lib.getAbsolutePath());
        
        runConsole();
    }

    /**
     * Shuts down all systems and exits the program
     */
    public static void shutdown() {
        if (camera != null) camera.shutdown();
        GPIOManager.shutdown();
        System.exit(0);
    }

    /**
     * Returns whether the program is in debug mode
     * @return debug mode
     */
    public static boolean isDebug() {
        return debug;
    }

    /**
     * Initializes the camera used for detecting and recognizing faces
     * @param index the index of the desired camera
     */
    public static void initCamera(int index) {
        if (camera != null) camera.shutdown();
        camera = new Camera(index);
    }

    /**
     * Creates, initializes and assigns a new recognizer object
     */
    public static void createRecognizer() {
        if (camera == null)
            throw new RuntimeException("Unable to create recognizer: Camera has not been initialized!");
        camera.createRecognizer();
    }

    /**
     * Returns the currently assigned camera used by the program
     * @return camera
     */
    public static Camera getCamera() {
        return camera;
    }

    /**
     * Loads a recognizer object from storage
     * @param path the relative path of the recognizer directory
     * @throws IOException in case the recognizer was either not found or could not be loaded
     * @throws ClassNotFoundException in case the class was not found
     */
    public static void loadRecognizer(String path) throws IOException, ClassNotFoundException {
        if (camera == null)
            throw new RuntimeException("Unable to load recognizer: Camera has not been initialized!");
        camera.loadRecognizer(path);
    }

    /**
     * Saves the recognizer to the relative path
     * @param path the path of the new recognizer directory
     * @throws IOException in case the recognizer could not be saved
     */
    public static void saveRecognizer(String path) throws IOException {
        if (camera == null)
            throw new RuntimeException("Unable to save recognizer: Camera has not been initialized!");
        if (camera.getRecognizer() == null)
            throw new RuntimeException("Unable to save recognizer: Recognizer has not been initalized!");
        camera.getRecognizer().saveRecognizer(path);
    }

    /**
     * Runs the console thread which enables the console interface
     */
    private static void runConsole() {
        ConsoleThread thread = new ConsoleThread();
        thread.start();
    }

    /**
     * Returns how many frames have to contain an authorized face in succession to trigger GPIO output
     * @return frame change count
     */
    public static int getFrameChangeCount() {
        return frameChangeCount;
    }

    /**
     * Returns a value which represents how many seconds the program has to wait before disabling GPIO state after there are no more recognized faces
     * @return disable delay
     */
    public static int getDisableDelay() {
        return disableDelay;
    }

    /**
     * Returns the maximum value for recognition confidence which still counts as a recognized face
     * @return max confidence
     */
    public static int getMaxConfidence() {
        return maxConfidence;
    }

    /**
     * Sets how many frames have to contain an authorized face in succession to trigger GPIO output
     * @param frameChangeCount frame change count
     */
    public static void setFrameChangeCount(int frameChangeCount) {
        Application.frameChangeCount = frameChangeCount;
    }

    /**
     * Returns a value which represents how many seconds the program has to wait before disabling GPIO state after there are no more recognized faces
     * @param disableDelay disable delay
     */
    public static void setDisableDelay(int disableDelay) {
        Application.disableDelay = disableDelay;
    }

    /**
     * Returns the maximum value for recognition confidence which still counts as a recognized face
     * @param maxConfidence max confidence
     */
    public static void setMaxConfidence(int maxConfidence) {
        Application.maxConfidence = maxConfidence;
    }
}
