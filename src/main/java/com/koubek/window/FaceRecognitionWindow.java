package com.koubek.window;

import javax.swing.JFrame;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class FaceRecognitionWindow extends JFrame {
    public FaceRecognitionWindow() {
        this.setTitle("Face recognition");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setLayout(null);
    }

    public void setImage(BufferedImage image) {
        Graphics2D g2D = (Graphics2D) this.getGraphics();
        g2D.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
    }
}
