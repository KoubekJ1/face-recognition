package com.koubek;

import com.koubek.exceptions.CameraNotInitializedException;
import com.koubek.exceptions.RecognizerNotInitializedException;
import com.koubek.gpio.GPIOManager;
import com.koubek.gpio.PWMDevice;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.face.EigenFaceRecognizer;
import org.opencv.face.FaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

public class Application {
    private static boolean debug;
    private static Camera camera;

    public static void start(String[] args) {
        for (String arg : args) {
            switch (arg) {
                case "debug" -> {
                    debug = true;
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
        System.exit(0);
        GPIOManager.shutdown();
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
            throw new CameraNotInitializedException("Unable to create recognizer: Camera has not been initialized!");
        camera.createRecognizer();
    }

    public static Camera getCamera() {
        return camera;
    }

    public static void loadRecognizer(String path) throws IOException, ClassNotFoundException {
        if (camera == null)
            throw new CameraNotInitializedException("Unable to load recognizer: Camera has not been initialized!");
        camera.loadRecognizer(path);
    }

    public static void saveRecognizer(String path) throws IOException {
        if (camera == null)
            throw new CameraNotInitializedException("Unable to save recognizer: Camera has not been initialized!");
        if (camera.getRecognizer() == null)
            throw new RecognizerNotInitializedException("Unable to save recognizer: Recognizer has not been initalized!");
        camera.getRecognizer().saveRecognizer(path);
    }

    private static void runConsole() {
        ConsoleThread thread = new ConsoleThread();
        thread.start();
    }

    public static void faceRecognitionExample() {
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
    }

    /*private static void ServoMotorExample() {
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
