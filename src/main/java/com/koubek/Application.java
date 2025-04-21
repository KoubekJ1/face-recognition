package com.koubek;

import com.koubek.gpio.GPIOManager;

import java.io.File;
import java.io.IOException;

public class Application {
    private static boolean debug;
    private static Camera camera;

    private static int frameChangeCount = 5;
    private static int disableDelay = 5;

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

        File lib = new File("src/main/resources/opencv/libopencv_java4110.so");
        System.load(lib.getAbsolutePath());
        
        runConsole();
    }

    public static void shutdown() {
        if (camera != null) camera.shutdown();
        GPIOManager.shutdown();
        System.exit(0);
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void initCamera(int index) {
        if (camera != null) camera.shutdown();
        camera = new Camera(index);
    }

    public static void createRecognizer() {
        if (camera == null)
            throw new RuntimeException("Unable to create recognizer: Camera has not been initialized!");
        camera.createRecognizer();
    }

    public static Camera getCamera() {
        return camera;
    }

    public static void loadRecognizer(String path) throws IOException, ClassNotFoundException {
        if (camera == null)
            throw new RuntimeException("Unable to load recognizer: Camera has not been initialized!");
        camera.loadRecognizer(path);
    }

    public static void saveRecognizer(String path) throws IOException {
        if (camera == null)
            throw new RuntimeException("Unable to save recognizer: Camera has not been initialized!");
        if (camera.getRecognizer() == null)
            throw new RuntimeException("Unable to save recognizer: Recognizer has not been initalized!");
        camera.getRecognizer().saveRecognizer(path);
    }

    private static void runConsole() {
        ConsoleThread thread = new ConsoleThread();
        thread.start();
    }

    public static int getFrameChangeCount() {
        return frameChangeCount;
    }

    public static int getDisableDelay() {
        return disableDelay;
    }

    /*public static void faceRecognitionExample() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // Images used for training the algorithm
        ArrayList<Mat> images = new ArrayList<>();
        // Images used to test the algorithm (last image in the directory)
        ArrayList<Mat> testImages = new ArrayList<>();
        // The person ID for each image
        int[] labels = new int[360];
        // Current label index
        int index = 0;

        // Loading the test data
        File[] facesDirectories = new File("resources/att_faces").listFiles((current, name) -> new File(current, name).isDirectory());
        for (int i = 0; i < facesDirectories.length; i++) {
            File[] personImages = facesDirectories[i].listFiles();
            for (int j = 0; j < personImages.length - 1; j++) {
                images.add(Imgcodecs.imread(personImages[j].getAbsolutePath(), 0));
                labels[index] = i;
                index++;
            }
            testImages.add(Imgcodecs.imread(personImages[personImages.length - 1].getAbsolutePath(), 0));
        }

        // Training the recognizer
        EigenFaceRecognizer recognizer = EigenFaceRecognizer.create();
        recognizer.train(images, new MatOfInt(labels));

        // Testing the recognizer
        int[] predictedLabel = new int[1];
        double[] confidence = new double[1];
        for (int i = 0; i < testImages.size(); i++) {
            long startTime = System.currentTimeMillis();
            recognizer.predict(testImages.get(i), predictedLabel, confidence);
            long finishTime = System.currentTimeMillis();
            Log.printMessage("Face " + i + " recognized as person " + predictedLabel[0] + " in " + ((finishTime - startTime) / 1000.0) + "s; Confidence: " + confidence[0], MessageType.INFO);
        }
    }*/

    /*private static void servoMotorExample() {
        PWMDevice device = new PWMDevice(0, 50);
        device.setState(true);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        device.setState(false);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        GPIOManager.shutdown();
    }*/
}
