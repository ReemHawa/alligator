package view;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundManager {

    public static void play(String resourcePath) {
        try {
            URL url = SoundManager.class.getResource(resourcePath);
            System.out.println("WIN URL = " + url); 
            if (url == null) {
                System.err.println("Sound not found: " + resourcePath);
                return;
            }

            try (AudioInputStream ais = AudioSystem.getAudioInputStream(url)) {
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                clip.start();
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static void win()  { play("/sounds/win.wav"); }
    public static void lose() { play("/sounds/lose.wav"); }
    

}
