package com.koubek;

import java.util.Scanner;

/**
 * ScannerInput is used for receiving input from the console that simplifies getting input matching certain parameters
 */
public abstract class ScannerInput {

    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Receives an integer from the user.
     * The user must enter an integer to proceed.
     * @return
     */
    public static int GetInt() {
        while (true) {
            try {
                return scanner.nextInt();
            } catch (Exception e) {}
        }
    }

    /**
     * Receives an integer from the user that is within the given bounds
     * @param min the minimum number
     * @param max the maximum number
     * @return the integer
     */
    public static int GetInt(int min, int max) {
        int returnVal;

        do {
            returnVal = GetInt();
        } while (returnVal < min || returnVal > max);

        return returnVal;
    }

    /**
     * Receives a double from the user.
     * The user must enter a double or an integer to proceed.
     * @return
     */
    public static double GetDouble() {
        while (true) {
            try {
                return scanner.nextDouble();
            } catch (Exception e) {}
        }
    }

    /**
     * Receives a double from the user that is within the given bounds
     * @param min the minimum number
     * @param max the maximum number
     * @return the double
     */
    public static double GetDouble(double min, double max) {
        double returnVal;

        do {
            returnVal = GetDouble();
        } while (returnVal < min || returnVal > max);

        return returnVal;
    }
}
