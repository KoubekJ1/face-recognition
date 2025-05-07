package com.koubek.window;

import javax.swing.JFrame;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * FaceRecognitionWindow serves as the window used for displaying the altered camera output.
 */
public class FaceRecognitionWindow extends JFrame {

    /**
     * Constructs a new face recognition window
     */
    public FaceRecognitionWindow() {
        this.setTitle("Face recognition");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setLayout(null);
    }

    /**
     * Sets the image displayed by the window.
     * @param image the image
     */
    public void setImage(BufferedImage image) {
        Graphics2D g2D = (Graphics2D) this.getGraphics();
        g2D.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
    }
}
