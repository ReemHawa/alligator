package view;

import music.bcMusic;

import javax.swing.*;
import java.awt.*;

public class SpeakerIcon {

    // ==============================
    // Create speaker label
    // ==============================
    public static JLabel createSpeakerLabel() {

        JLabel speaker = new JLabel();
        speaker.setSize(25, 25);

        updateIcon(speaker);

        speaker.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        speaker.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                bcMusic.toggleMute();
                updateIcon(speaker);
            }
        });

        return speaker;
    }

    // ==============================
    // Update icon based on mute state
    // ==============================
    public static void updateIcon(JLabel speaker) {

        int size = 40;

        String path = bcMusic.isMuted()
                ? "/images/mute.png"
                : "/images/unmute.png";

        speaker.setIcon(loadScaledIcon(path, size));
    }

    // ==============================
    // Load & scale icon correctly
    // ==============================
    private static ImageIcon loadScaledIcon(String path, int size) {

        ImageIcon icon = new ImageIcon(
                SpeakerIcon.class.getResource(path)
        );

        Image scaled = icon.getImage()
                .getScaledInstance(size, size, Image.SCALE_SMOOTH);

        return new ImageIcon(scaled);
    }
}
