package com.koubek;

/**
 * Detection instances serve as objects returned by a recognizer on a per-person basis that state who the person is and how confident the recognizer is with its statement
 * The lower the confidence value, the more confident the recognizer is
 */
public class Detection {
    private Person person;
    private double confidence;
    
    /**
     * Constructs a new detection instance
     * @param person the identified person
     * @param confidence the confidence value
     */
    public Detection(Person person, double confidence) {
        this.person = person;
        this.confidence = confidence;
    }

    /**
     * Returns the identified person
     * @return the identified person
     */
    public Person getPerson() {
        return person;
    }

    /**
     * Returns the confidence value
     * @return the confidence value
     */
    public double getConfidence() {
        return confidence;
    }

    /**
     * Returns the detection information as a string.
     */
    @Override
    public String toString() {
        return person + ". Confidence: " + confidence;
    }
}
