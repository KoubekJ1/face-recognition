package com.koubek;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import com.koubek.gpio.GPIOManager;
import com.koubek.window.WindowManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

/**
 * Camera objects serve as software representations of hardware cameras, which have assigned recognizer objects.
 */
public class Camera implements ActionListener {
    private VideoCapture videoCapture;
    private Timer captureTimer;
    private Timer disableTimer;
    private Mat frame;
    private Rect[] faces, smiles;
    private LinkedList<Mat> trackedFaceImages;
    private Detection[] recognizedFaces;

    private CascadeClassifier faceCascade;
    private CascadeClassifier smileCascade;

    private Recognizer recognizer;

    private Mat loadedImage;

    private int absoluteFaceSize;

    private boolean currentState = false;
    private int currentTolerance = 0;
    private float currentDisableDelay = 0;

    private final Object timerLock = new Object();

    /**
     * Creates a new camera with the camera index 0
     */
    public Camera() {
        init(5, 0);
    }

    /**
     * Creates a new camera with the given camera index
     * @param index camera index
     */
    public Camera(int index) {
        init(5, index);
    }

    /**
     * Creates a new recognizer object to be used by the camera
     */
    public void createRecognizer() {
        recognizer = new Recognizer();
    }

    /**
     * Loads a recognizer object from the given relative path
     * @param path the path of the recognizer's directory
     * @throws IOException in case the recognizer could not be loaded
     * @throws ClassNotFoundException in case the class could not be found
     */
    public void loadRecognizer(String path) throws IOException, ClassNotFoundException {
        recognizer = Recognizer.loadRecognizer(path);
    }

    /**
     * Returns the recognizer used by the camera
     * @return recognizer
     */
    public Recognizer getRecognizer() {
        return recognizer;
    }

    /**
     * Returns the camera's current unaltered frame
     * @return current frame
     */
    public Mat getCurrentFrame() {
        return frame;
    }

    /**
     * Enables saving images of a person to then be able to recognize
     */
    public void trackFace() {
        trackedFaceImages = new LinkedList<>();
    }

    /**
     * Returns the saved images of the tracked person and deletes them
     * @return the saved images
     */
    public LinkedList<Mat> finishTracking() {
        LinkedList<Mat> matList = trackedFaceImages;
        trackedFaceImages = null;
        return matList;
    }

    /**
     * Initializes the camera with the given capture rate and index
     * @param captureRate camera capture rate
     * @param index camera index
     */
    private void init(int captureRate, int index) {
        long startTime = System.currentTimeMillis();
        Log.printMessage("Camera initializing...", MessageType.INIT);
        videoCapture = new VideoCapture(index, Videoio.CAP_V4L2);
        if (!videoCapture.isOpened()) {
            throw new RuntimeException("Unable to open video capture!");
        }
        videoCapture.set(3, 1920);
        videoCapture.set(4, 1080);
        faceCascade = loadClassifier("opencv/haarcascades/haarcascade_frontalface_default.xml");
        smileCascade = loadClassifier("opencv/haarcascades/haarcascade_smile.xml");
        captureTimer = new Timer(1000 / captureRate, this);
        captureTimer.start();
        disableTimer = new Timer(Application.getDisableDelay() * 1000, this);
        disableTimer.setRepeats(false);
        long finishTime = System.currentTimeMillis();
        Log.printMessage("Camera initialized in " + ((finishTime - startTime) / (double) 1000) + "s", MessageType.INIT);
    }

    /**
     * Loads and returns a cascade classifier from an XML file in the given relative path
     * @param path relative path to XML file
     * @return cascade classifier
     */
    private CascadeClassifier loadClassifier(String path) {
        CascadeClassifier cascade = new CascadeClassifier();
        cascade.load(path);
        Log.printMessage("Loaded XML classifier: " + path, MessageType.INIT);
        return cascade;
    }

    /**
     * Detects and returns parts of the given image containing faces
     * @param image image to be analysed
     * @return rectangles with faces
     */
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

    /**
     * Recognizes the faces in the image using the camera's recognizer
     * @param image image to be analyzed
     * @return detection objects for each indiviual detected face
     */
    public Detection[] recognizeFaces(Mat image) {
        if (image == null) return null;
        if (image.empty()) return null;
        Detection[] people = new Detection[faces.length];

        if (recognizer == null) {
            for (int i = 0; i < people.length; i++) {
                people[i] = Recognizer.UNRECOGNIZED;
            }
            return people;
        }

        Mat grayFrame = new Mat();
        for (int i = 0; i < people.length; i++) {
            Imgproc.cvtColor(image.submat(faces[i]), grayFrame, Imgproc.COLOR_BGR2GRAY);
            // ! Případně odkomentovat!!!
            //Imgproc.equalizeHist(grayFrame, grayFrame);
            people[i] = recognizer.recognizeFace(grayFrame);
        }

        return people;
    }

    /**
     * Detects and returns parts of the given image containing smiles
     * @param image image to be analyzed
     * @return smiles
     */
    public Rect[] detectSmiles(Mat image) {
        LinkedList<Rect> rectangles = new LinkedList<>();
        if (faces == null) faces = detectFaces(image);
        Mat grayFrame = new Mat();
        for (int i = 0; i < faces.length; i++) {
            Imgproc.cvtColor(image.submat(faces[i]), grayFrame, Imgproc.COLOR_BGR2GRAY);
            //Imgproc.equalizeHist(grayFrame, grayFrame);
            MatOfRect smiles = new MatOfRect();
            smileCascade.detectMultiScale(grayFrame, smiles, 1.4, 6);
            //smileCascade.detectMultiScale(grayFrame, smiles);
            for (Rect rect : smiles.toArray()) {
                rect.x += faces[i].x;
                rect.y += faces[i].y;
                rectangles.add(rect);
            }
        }
        Rect[] rectArray = new Rect[rectangles.size()];
        return rectangles.toArray(rectArray);
    }

    /**
     * Returns how many smiles are present in the given image
     * @param image image to be analyzed
     * @return smile count
     */
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

    /**
     * Returns a buffered image object containing the camera's current frame with highlighted faces with names and confidence values, as well as highlighted smiles
     * @return buffered image of the camera's altered current frame
     */
    public BufferedImage getBufferedImage() {
        if (faces == null) faces = detectFaces(frame);
        if (smiles == null) smiles = detectSmiles(frame);
        if (recognizedFaces == null) recognizedFaces = recognizeFaces(frame);
        //Mat newFrame = new Mat();
        //Imgproc.cvtColor(frame, newFrame, Imgproc.COLOR_BGR2GRAY);
        Mat newFrame = frame.clone();
        for (Rect rect : faces) {
            Imgproc.rectangle(newFrame, rect, new Scalar(0, 255, 0));
        }

        Detection[] peopleOnScreen = recognizedFaces;
        for (int i = 0; i < faces.length; i++) {
            Imgproc.putText(newFrame, peopleOnScreen[i].getPerson().getName(), new Point(faces[i].x, faces[i].y + faces[i].height), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 255, 255), 4);
            Imgproc.putText(newFrame, String.valueOf((int) peopleOnScreen[i].getConfidence()), new Point(faces[i].x, faces[i].y + faces[i].height + 50), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 255, 255), 4);
        }

        for (Rect rect : smiles) {
            Imgproc.rectangle(newFrame, rect, new Scalar(255, 0, 0));
        }
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", newFrame, buffer);
        BufferedImage image = null;
        try {
            InputStream in = new ByteArrayInputStream(buffer.toArray());
            image = ImageIO.read(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return image;   
    }

    /**
     * Loads the given image to the camera
     * @param url path to the image
     */
    public void loadImage(String url) {
        loadedImage = Imgcodecs.imread(url);
    }

    /**
     * Unloads the previously loaded image
     */
    public void unloadImage() {
        loadedImage = null;
    }

    /**
     * Shuts the camera down, stopping the frame capture and face recognition cycle
     */
    public void shutdown() {
        captureTimer.stop();
        videoCapture.release();
    }

    /**
     * Triggered by the camera capture timer. This method serves as the camera frame capture and face recognition cycle
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        synchronized (timerLock) {
            if (e.getSource() == captureTimer) {
                if (frame == null)
                    frame = new Mat();
                videoCapture.read(frame);
                faces = null;
                smiles = null;
                recognizedFaces = null;

                if (frame.empty()) return;
                if (recognizer == null) return;

                faces = detectFaces(frame);
                smiles = detectSmiles(frame);
                recognizedFaces = recognizeFaces(frame);
                
                if (WindowManager.isWindowVisible()) {
                    WindowManager.getWindow().setImage(getBufferedImage());
                }

                boolean authorizedPersonDetected = false;
                for (Detection person : recognizedFaces) {
                    if (person.getPerson().isAuthorized()) {
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
                    //Rect[] detectedFaces = detectFaces(frame);
                    Rect[] detectedFaces = faces;
                    if (detectedFaces.length != 1)
                        return;
                    Mat grayFrame = new Mat();
                    Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
                    //Imgproc.equalizeHist(grayFrame, grayFrame);
                    if (trackedFaceImages != null)
                        trackedFaceImages.add(grayFrame.submat(detectedFaces[0]));
                }
            }
        }

        if (e.getSource() == disableTimer) {
            GPIOManager.setBlinkDevices(false);
            GPIOManager.setState(false);
        }
    }
}
