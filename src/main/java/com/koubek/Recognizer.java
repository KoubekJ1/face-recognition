package com.koubek;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.face.LBPHFaceRecognizer;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Recognizer instances are used to recognize faces from image input.
 * Recognizers first have to be trained, only then can they recognize faces.
 */
public class Recognizer {
    private LBPHFaceRecognizer lbph;
    private ArrayList<Person> people;
    private boolean trained;
    private boolean savingPerson = false;

    private final Object recognizerLock = new Object();

    public static final Detection UNRECOGNIZED = new Detection(new Person("Unrecognized", false), 0);

    /**
     * Constructs a new recognizer instance.
     */
    public Recognizer() {
        lbph = LBPHFaceRecognizer.create();
        people = new ArrayList<>();
        trained = false;
    }

    /**
     * Loads a recognizer from the given recognizer directory's relative path
     * @param path the given recognizer directory's relative path
     * @return The loaded recognizer
     * @throws IOException in case the recognizer could not be loaded
     * @throws ClassNotFoundException in case the class was not found
     */
    public static Recognizer loadRecognizer(String path) throws IOException, ClassNotFoundException {
        Recognizer recognizer = new Recognizer();
        recognizer.trained = true;
        recognizer.lbph = LBPHFaceRecognizer.create();
        recognizer.lbph.read(path + "/lbph.xml");
        FileInputStream fileInputStream = new FileInputStream(path + "/people.ser");
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        recognizer.people = (ArrayList<Person>) objectInputStream.readObject();

        String peopleString = "\n";
        for (int i = 0; i < recognizer.people.size(); i++) {
            peopleString += i + ": " + recognizer.people.get(i).getName();
        }
        Log.printMessage("Recognizer loaded" + peopleString, MessageType.INIT);

        return recognizer;
    }

    /**
     * Saves the recognizer to the given relative path
     * Saving a recognizer directory automatically creates any potential directories that may not already exist
     * @param path the relative path of the recognizer's directory
     * @throws IOException in case the recognizer could not be saved
     */
    public void saveRecognizer(String path) throws IOException {
        new File(path).mkdirs();
        lbph.save(path + "/lbph.xml");
        FileOutputStream fileOutputStream = new FileOutputStream(path + "/people.ser");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(people);
    }

    /**
     * Adds a new person to the recognizer who can then be recognized.
     * For optimal results, the images should be cropped to the face.
     * @param images the images of the person
     * @param person the person in the images
     */
    public void addPerson(LinkedList<Mat> images, Person person)
    {
        synchronized (recognizerLock) {
            if (images == null) return;
            if (images.size() == 0) return;
            savingPerson = true;
            int[] labels = new int[images.size()];
            Log.printMessage("Person ID: " + people.size(), MessageType.INIT);
            Arrays.fill(labels, people.size());
            if (trained) {
                lbph.update(images, new MatOfInt(labels));
            } else {
                lbph.train(images, new MatOfInt(labels));
                trained = true;
            }
            people.add(person);
            Log.printMessage("Face " + labels[0] + ": " + person.getName() + " added!", MessageType.INIT);
            savingPerson = false;
        }
    }

    /**
     * Recognizes a single face from the given image.
     * For optimal results, the image should be cropped to the face.
     * @param face
     * @return
     */
    public Detection recognizeFace(Mat face)
    {
        synchronized (recognizerLock) {
            if (savingPerson) return UNRECOGNIZED;
            if (!trained) return UNRECOGNIZED;
            int[] label = new int[1];
            double[] confidence = new double[1];
            lbph.predict(face, label, confidence);
            Log.printMessage("Face recognized as ID " + label[0] + ". Face: " + people.get(label[0]) + ". " + "Confidence: " + confidence[0] + ".", MessageType.INIT);
            return confidence[0] < Application.getMaxConfidence() ? new Detection(people.get(label[0]), confidence[0]) : UNRECOGNIZED;
            //return confidence[0] < 8000 ? people.get(label[0]) : new Person(String.valueOf(confidence[0])); // Used for testing
        }
    }

    /**
     * Returns whether the recognizer is in the process of adding a person
     * @return is saving person
     */
    public boolean isSavingPerson() {
        return savingPerson;
    }
}