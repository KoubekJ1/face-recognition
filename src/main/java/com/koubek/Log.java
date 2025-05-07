package com.koubek;

import java.io.PrintStream;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The log class contains a method used for abstracting log output for means of alteration.
 * Depending on the terminal emulator's capabilities, the log output may also have a special color based on the message type.
 */
public class Log {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";

    private static boolean printLog = true;
    private static boolean printInitializationDialog = false;

    /**
     * Prints the message to the console along with the type and date.
     * All message types have a different output color.
     * INIT type messages are only displayed when the program is in debug mode.
     * @param message Message to be displayed
     * @param type type of the message
     */
    public static void printMessage(String message, MessageType type) {
        printInitializationDialog = Application.isDebug();
        if (!printLog) return;
        if (type == MessageType.INIT && !printInitializationDialog) return;
        String colorCode;
        colorCode = switch (type) {
            case INIT -> ANSI_RESET;
            case INFO -> ANSI_CYAN;
            case WARNING -> ANSI_YELLOW;
            case ERROR -> ANSI_RED;
            case FATAL -> ANSI_PURPLE;
            default -> ANSI_RESET;
        };
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        System.out.println("[" + formatter.format(new Date()) + "] " + type + ": " + message);
    }
}

