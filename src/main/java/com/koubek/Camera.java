package com.koubek;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import com.koubek.gpio.GPIOManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.LinkedList;

public class Camera implements ActionListener {
    private VideoCapture videoCapture;
    private Timer captureTimer;
    private Timer disableTimer;
    private Mat frame;
    private LinkedList<Mat> trackedFaceImages;

    private CascadeClassifier faceCascade;
    private CascadeClassifier smileCascade;

    private Recognizer recognizer;

    private Mat loadedImage;

    private int absoluteFaceSize;

    private boolean currentState = false;
    private int currentTolerance = 0;
    private float currentDisableDelay = 0;

    public Camera() {
        init(18, 0);
    }

    public Camera(int index) {
        init(18, index);
    }

    public void createRecognizer() {
        recognizer = new Recognizer();
    }

    public void loadRecognizer(String path) throws IOException, ClassNotFoundException {
        recognizer = Recognizer.loadRecognizer(path);
    }

    public Recognizer getRecognizer() {
        return recognizer;
    }

    public Mat getCurrentFrame() {
        return frame;
    }

    public void trackFace() {
        trackedFaceImages = new LinkedList<>();
    }

    public LinkedList<Mat> finishTracking() {
        LinkedList<Mat> matList = trackedFaceImages;
        trackedFaceImages = null;
        return matList;
    }

    private void init(int captureRate, int index) {
        long startTime = System.currentTimeMillis();
        Log.printMessage("Camera initializing...", MessageType.INIT);
        videoCapture = new VideoCapture(index, Videoio.CAP_V4L2);
        if (!videoCapture.isOpened()) {
            throw new RuntimeException("Unable to open video capture!");
        }
        faceCascade = loadClassifier("src/main/resources/haarcascades/haarcascade_frontalface_default.xml");
        smileCascade = loadClassifier("src/main/resources/haarcascades/haarcascade_smile.xml");
        captureTimer = new Timer(1000 / captureRate, this);
        captureTimer.start();
        disableTimer = new Timer(Application.getDisableDelay() * 1000, this);
        disableTimer.setRepeats(false);
        long finishTime = System.currentTimeMillis();
        Log.printMessage("Camera initialized in " + ((finishTime - startTime) / (double) 1000) + "s", MessageType.INIT);
    }

    private CascadeClassifier loadClassifier(String path) {
        CascadeClassifier cascade = new CascadeClassifier();
        cascade.load(path);
        Log.printMessage("Loaded XML classifier: " + path, MessageType.INIT);
        return cascade;
    }

    private Rect[] detectFaces(Mat image) {
        Mat grayFrame = new Mat();
        if (loadedImage != null) {
            image = loadedImage.clone();
        }
        Imgproc.cvtColor(image, grayFrame, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(grayFrame, grayFrame);
        if (this.absoluteFaceSize == 0) {
            int height = grayFrame.rows();
            if (Math.round(height * 0.2f) > 0) {
                this.absoluteFaceSize = Math.round(height * 0.2f);
            }
        }

        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
                new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        return faces.toArray();
    }

    public Person[] recognizeFaces(Mat image) {
        if (image == null) return null;
        if (image.empty()) return null;
        Rect[] faceRectangles = detectFaces(image);
        Person[] people = new Person[faceRectangles.length];

        if (recognizer == null) {
            for (int i = 0; i < people.length; i++) {
                people[i] = Recognizer.UNRECOGNIZED;
            }
            return people;
        }

        Mat grayFrame = new Mat();
        for (int i = 0; i < people.length; i++) {
            Imgproc.cvtColor(image.submat(faceRectangles[i]), grayFrame, Imgproc.COLOR_BGR2GRAY);
            Imgproc.equalizeHist(grayFrame, grayFrame);
            people[i] = recognizer.recognizeFace(grayFrame);
        }

        return people;
    }

    public int getSmileCount(Mat image) {
        int smileCount = 0;
        Rect[] faces = detectFaces(image);
        Mat grayFrame = new Mat();
        for (int i = 0; i < faces.length; i++) {
            Imgproc.cvtColor(image.submat(faces[i]), grayFrame, Imgproc.COLOR_BGR2GRAY);
            //Imgproc.equalizeHist(grayFrame, grayFrame);
            MatOfRect smiles = new MatOfRect();
            smileCascade.detectMultiScale(grayFrame, smiles, 1.4, 6);
            smileCount += smiles.toArray().length > 0 ? 1 : 0;
        }
        return smileCount;
    }

    public void loadImage(String url) {
        loadedImage = Imgcodecs.imread(url);
    }

    public void unloadImage() {
        loadedImage = null;
    }

    public void shutdown() {
        captureTimer.stop();
        videoCapture.release();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == captureTimer) {
            if (frame == null)
                frame = new Mat();
            videoCapture.read(frame);

            if (frame.empty()) return;
            if (recognizer == null) return;

            boolean authorizedPersonDetected = false;
            for (Person person : recognizeFaces(frame)) {
                if (person.isAuthorized()) {
                    authorizedPersonDetected = true;
                    if (!currentState && currentTolerance < Application.getFrameChangeCount()) {
                        currentTolerance++;
                    } else {
                        if (disableTimer.isRunning()) {
                            disableTimer.stop();
                            GPIOManager.setBlinkDevices(false);
                        }
                        GPIOManager.setState(true);
                        currentState = true;
                        currentTolerance = 0;
                    }
                    break;
                }
            }
            if (!authorizedPersonDetected) {
                if (currentState && currentTolerance < Application.getFrameChangeCount()) {
                    currentTolerance++;
                } else {
                    disableTimer.start();
                    if (currentState) GPIOManager.setBlinkDevices(true);
                    currentState = false;
                    currentTolerance = 0;
                }
            }

            if (trackedFaceImages != null) {
                Rect[] detectedFaces = detectFaces(frame);
                if (detectedFaces.length != 1)
                    return;
                Mat grayFrame = new Mat();
                Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
                Imgproc.equalizeHist(grayFrame, grayFrame);
                if (trackedFaceImages != null)
                    trackedFaceImages.add(grayFrame.submat(detectedFaces[0]));
            }
        }

        if (e.getSource() == disableTimer) {
            GPIOManager.setBlinkDevices(false);
            GPIOManager.setState(false);
        }
    }
}
