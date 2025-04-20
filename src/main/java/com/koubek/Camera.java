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
    private Mat frame;
    private LinkedList<Mat> trackedFaceImages;

    private CascadeClassifier faceCascade;
    private CascadeClassifier smileCascade;

    private Recognizer recognizer;

    private Mat loadedImage;

    private int absoluteFaceSize;

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
        // videoCapture = new VideoCapture(0);
        if (!videoCapture.isOpened()) {
            throw new RuntimeException("Unable to open video capture!");
        }
        faceCascade = loadClassifier("src/main/resources/haarcascades/haarcascade_frontalface_default.xml");
        smileCascade = loadClassifier("src/main/resources/haarcascades/haarcascade_smile.xml");
        captureTimer = new Timer(1000 / captureRate, this);
        captureTimer.start();
        long finishTime = System.currentTimeMillis();
        Log.printMessage("Camera initialized in " + ((finishTime - startTime) / (double) 1000) + "s", MessageType.INIT);
    }

    /*
     * public BufferedImage getBufferedImage(Mat frame) {
     * Mat grayFrame = new Mat();
     * if (loadedImage != null) {
     * frame = loadedImage.clone();
     * }
     * Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
     * Imgproc.equalizeHist(grayFrame, grayFrame);
     * if (this.absoluteFaceSize == 0) {
     * int height = grayFrame.rows();
     * if (Math.round(height * 0.2f) > 0) {
     * this.absoluteFaceSize = Math.round(height * 0.2f);
     * }
     * }
     * 
     * MatOfRect faces = new MatOfRect();
     * faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 |
     * Objdetect.CASCADE_SCALE_IMAGE, new Size(absoluteFaceSize, absoluteFaceSize),
     * new Size());
     * Rect[] facesArray = faces.toArray();
     * for (int i = 0; i < facesArray.length; i++) {
     * Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new
     * Scalar(0, 255, 0, 255), 3);
     * }
     * MatOfByte buffer = new MatOfByte();
     * Imgcodecs.imencode(".png", frame, buffer);
     * BufferedImage image = null;
     * try {
     * InputStream in = new ByteArrayInputStream(buffer.toArray());
     * image = ImageIO.read(in);
     * } catch (IOException e) {
     * throw new RuntimeException(e);
     * }
     * return image;
     * }
     */

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

    /*
     * private void loadClassifiers(String path) {
     * faceCascade = new CascadeClassifier();
     * File directory = new File(path);
     * if (directory.listFiles().length == 0) {
     * Log.printMessage("Invalid directory: " + path, MessageType.ERROR);
     * return;
     * }
     * for (File classifier : directory.listFiles()) {
     * if (!faceCascade.load(classifier.getPath())) {
     * Log.printMessage("Unable to load XML classifier: " + classifier.getPath(),
     * MessageType.ERROR);
     * } else {
     * Log.printMessage("Loaded XML classifier: " + classifier.getPath(),
     * MessageType.INIT);
     * }
     * }
     * }
     */

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
                    GPIOManager.setState(true);
                    authorizedPersonDetected = true;
                    break;
                }
            }
            if (!authorizedPersonDetected) {
                GPIOManager.setState(false);
            }
            //Log.printMessage("Smile count: " + getSmileCount(frame), MessageType.INIT);

            // WindowManager.getWindow().drawBufferedImage(this.getBufferedImage(frame));
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
    }
}
