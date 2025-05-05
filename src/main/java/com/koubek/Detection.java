package com.koubek;

public class Detection {
    private Person person;
    private double confidence;
    
    public Detection(Person person, double confidence) {
        this.person = person;
        this.confidence = confidence;
    }

    public Person getPerson() {
        return person;
    }

    public double getConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        return person + ". Confidence: " + confidence;
    }
}
