package view;

import music.bcMusic;

public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
        	bcMusic.play("/music/Host Entrance Background Music.wav");
            new HomeScreen();
        });
    }
}