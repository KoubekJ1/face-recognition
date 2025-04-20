package com.koubek;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class FaceRecognitionWindow extends JFrame implements KeyListener {

    private Camera camera;

    public FaceRecognitionWindow() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1280, 800);
        this.setResizable(true);
        this.addKeyListener(this);
        this.setTitle("Face Recognition");

        this.camera = new Camera(-1);
    }

    public void drawBufferedImage(BufferedImage image) {
        Graphics2D g2D = (Graphics2D) this.getGraphics();
        g2D.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
    }

    public void display() {
        this.setVisible(true);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_I) {
            JFileChooser chooser = new JFileChooser("resources/images");
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "jpg", "jpeg", "jpe", "bmp", "dib", "jp2", "png", "webp", "avif", "pbm", "pgm", "ppm", "pxm", "pnm", "pfm", "sr", "ras", "tiff", "tif", "exr", "hdr", "pic");
            chooser.setFileFilter(filter);
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                try {
                    camera.loadImage(chooser.getSelectedFile().getPath());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Unable to open file: " + chooser.getSelectedFile().getPath(), "Error", JOptionPane.ERROR);
                }
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            camera.unloadImage();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
