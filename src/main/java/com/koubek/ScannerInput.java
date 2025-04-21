package com.koubek;

import java.util.Scanner;

public abstract class ScannerInput {

    private static final Scanner scanner = new Scanner(System.in);

    public static int GetInt() {
        while (true) {
            try {
                return scanner.nextInt();
            } catch (Exception e) {}
        }
    }

    public static int GetInt(int min, int max) {
        int returnVal;

        do {
            returnVal = GetInt();
        } while (returnVal < min || returnVal > max);

        return returnVal;
    }

    public static double GetDouble() {
        while (true) {
            try {
                return scanner.nextDouble();
            } catch (Exception e) {}
        }
    }

    public static double GetDouble(double min, double max) {
        double returnVal;

        do {
            returnVal = GetDouble();
        } while (returnVal < min || returnVal > max);

        return returnVal;
    }
}
