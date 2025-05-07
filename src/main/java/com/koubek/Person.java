package com.koubek;

import java.io.Serializable;

/**
 * Person instances serve as representations of real people (or rather their faces)
 * A person may be set as authorized to trigger GPIO output upon recognition
 */
public class Person implements Serializable {
    private String name;
    private boolean authorized;

    /**
     * Constructs a new person with the given parameters
     * @param name person name
     * @param authorized whether the person is authorized
     */
    public Person(String name, boolean authorized) {
        this.name = name;
        this.authorized = authorized;
    }

    /**
     * Returns the person's name
     * @return the person's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the person's name
     * @param name the person's name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns whether the person is authorized
     * @return whether the person is authorized
     */
    public boolean isAuthorized() {
        return authorized;
    }

    /**
     * Sets whether the person is authorized
     * @param authorized whether the person is authorized
     */
    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    /**
     * Returns the person information as a string
     */
    @Override
    public String toString() {
        return name + ", Authorized: " + authorized;
    }
}
