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
}
