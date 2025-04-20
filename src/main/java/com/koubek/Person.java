package com.koubek;

import java.io.Serializable;

public class Person implements Serializable {
    private String name;
    private boolean authorized;

    public Person(String name, boolean authorized) {
        this.name = name;
        this.authorized = authorized;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    @Override
    public String toString() {
        return name + ", Authorized: " + authorized;
    }
}
