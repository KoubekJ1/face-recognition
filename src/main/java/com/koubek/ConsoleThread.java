package com.koubek;

import org.opencv.core.Mat;

import java.util.LinkedList;
import java.util.Scanner;

public class ConsoleThread extends Thread {
    Scanner scanner;

    public ConsoleThread() {
        scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        boolean running = true;
        printToConsole("Face recognition project\nUse \"help\" to see all available commands");
        while (running) {
            System.out.print("command: ");
            String input = scanner.next();
            try {
                switch (input.toLowerCase()) {
                    case "exit":
                        running = false;
                        Application.shutdown();
                        break;
                    case "help":
                        help();
                        break;
                    case "addgpio":
                    case "ag":
                        addGpioDeviceProcedure();
                        break;
                    case "faces":
                    case "f":
                        Person[] people = Application.getCamera().recognizeFaces(Application.getCamera().getCurrentFrame());
                        if (people == null) {
                            printToConsole("Unable to detect faces");
                            break;
                        }
                        for (Person person : people) {
                            printToConsole(person.toString());
                        }
                        break;
                    case "initcamera":
                    case "ic":
                        printToConsole("Enter camera index: ");
                        Application.initCamera(scanner.nextInt());
                        break;
                    case "createrecognizer":
                    case "cr":
                        Application.createRecognizer();
                        break;
                    case "loadrecognizer":
                    case "lr":
                        printToConsole("Enter recognizer directory path: ");
                        Application.loadRecognizer(scanner.next());
                        break;
                    case "saverecognizer":
                    case "sr":
                        printToConsole("Enter recognizer directory path: ");
                        Application.saveRecognizer(scanner.next());
                        break;
                    case "trackface":
                    case "tf":
                        Application.getCamera().trackFace();
                        printToConsole("Started tracking face");
                        break;
                    case "saveface":
                    case "sf":
                        printToConsole("Enter person name: ");
                        String personName = scanner.next();
                        printToConsole("Is " + personName + " authorized? (y/n)");
                        boolean isAuthorized = scanner.next().equalsIgnoreCase("y") ? true : false;
                        Person person = new Person(personName, isAuthorized);
                        LinkedList<Mat> faceImages = Application.getCamera().finishTracking();
                        Application.getCamera().getRecognizer().addPerson(faceImages, person);
                        printToConsole("Face saved to recognizer");
                        break;
                    case "smile":
                    case "s":
                        printToConsole("Smile count: " + Application.getCamera().getSmileCount(Application.getCamera().getCurrentFrame()));
                        break;
                    default:
                        printToConsole("Unrecognized command: \"" + input + "\". Try using \"help\".");
                        break;
                }
            } catch (Exception e) {
                Log.printMessage(e.getMessage(), MessageType.ERROR);
                e.printStackTrace();
            }
        }
    }

    private void help() {
        printToConsole("Available commands:" +
                "\nhelp" +
                "\naddgpio | ag" +
                "\ninitcamera | ic" +
                "\ncreaterecognizer | cr" +
                "\nloadrecognizer | lr" +
                "\nsaverecognizer | sr" +
                "\ntrackface | tf" +
                "\nsaveface | sf" +
                "\nfaces | f" +
                "\nsmile | s" +
                "\nexit");
    }

    private void addGpioDeviceProcedure() {
        printToConsole("Choose GPIO device type:\n1) Digital output (LED)\n2) Pulse Width Modulation (Servo motor)");
        int answer = Integer.parseInt(scanner.nextLine());
        switch (answer) {
            case 1:
                addDODeviceProcedure();
                break;
        
            default:
                printToConsole("Invalid");
                break;
        }
    }

    private void addDODeviceProcedure() {

    }

    private void printToConsole(String message) {
        System.out.println(message);
    }
}
