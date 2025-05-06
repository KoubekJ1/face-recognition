package com.koubek;

import org.opencv.core.Mat;

import com.koubek.gpio.DigitalOutputDevice;
import com.koubek.gpio.GPIOManager;
import com.koubek.gpio.PWMDevice;
import com.koubek.window.WindowManager;

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
        printToConsole("Face recognition project. Jan Koubek 2025\nUse \"help\" to see all available commands");
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
                    case "configuration":
                    case "conf":
                        changeSettingsProcedure();
                        break;
                    case "addgpio":
                    case "ag":
                        addGpioDeviceProcedure();
                        break;
                    case "openwindow":
                    case "ow":
                        WindowManager.getWindow().setVisible(true);
                        break;
                    case "faces":
                    case "f":
                        Detection[] people = Application.getCamera().recognizeFaces(Application.getCamera().getCurrentFrame());
                        if (people == null) {
                            printToConsole("Unable to detect faces");
                            break;
                        }
                        for (Detection person : people) {
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
                        LinkedList<Mat> faceImages = Application.getCamera().finishTracking();
                        printToConsole("Enter person name: ");
                        String personName = scanner.next();
                        printToConsole("Is " + personName + " authorized? (y/n)");
                        boolean isAuthorized = scanner.next().equalsIgnoreCase("y") ? true : false;
                        Person person = new Person(personName, isAuthorized);
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
                "\nconfiguration | conf" +
                "\naddgpio | ag" +
                "\nopenwindow | ow" +
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
        printToConsole("If you are using the provided servo motor barrier, please set all configuration values the same as default");
        printToConsole("Choose GPIO device type:\n0) Cancel\n1) Digital output (LED light)\n2) Pulse Width Modulation (Servo motor)");
        int answer = ScannerInput.GetInt(0, 2);
        printToConsole("Is this device used for blinking when the state disable timer is active? (y/n)");
        boolean blinkingDevice = scanner.next().equalsIgnoreCase("y");
        switch (answer) {
            case 0:
                return;
            case 1:
                printToConsole("Enter pin BCM number (please refer to the pin numbering diagram)");
                int address = ScannerInput.GetInt();
                
                GPIOManager.addDevice(new DigitalOutputDevice(address, blinkingDevice));
                break;
            case 2:
                printToConsole("Enter PWM channel number (0 - BCM pin 12, 18; 1 - BCM pin 13, 19)");
                int channel = ScannerInput.GetInt(0, 1);
                printToConsole("Enter pulse frequency (Default: 50)");
                int frequency = ScannerInput.GetInt();
                printToConsole("Enter device duty cycle for enabled state (Percentual amount of time the signal will be on. Default: 8.2)");
                double dutyCycleOn = ScannerInput.GetDouble();
                printToConsole("Enter device duty cycle for disabled state (Default: 13.2)");
                double dutyCycleOff = ScannerInput.GetDouble();
                GPIOManager.addDevice(new PWMDevice(channel, frequency, dutyCycleOn, dutyCycleOff, blinkingDevice));
                break;
        
            default:
                printToConsole("Invalid");
                break;
        }
    }

    private void changeSettingsProcedure() {
        printToConsole("Which setting do you wish to change?");
        printToConsole("1) Cancel\n2) Verification frame count\n3) Output disable delay\n4) Max confidence value");
        switch (ScannerInput.GetInt(1, 4)) {
            case 1:
                break;
            case 2:
                printToConsole("Enter verification frame count: ");
                Application.setFrameChangeCount(ScannerInput.GetInt());
                break;
            case 3:
                printToConsole("Enter output disable delay (in seconds): ");
                Application.setDisableDelay(ScannerInput.GetInt());
                break;
            case 4:
                printToConsole("Enter max confidence value: ");
                Application.setMaxConfidence(ScannerInput.GetInt());
                break;
            default:
                break;
        }
    }

    private void printToConsole(String message) {
        System.out.println(message);
    }
}
