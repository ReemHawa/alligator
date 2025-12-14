package music;

import javax.sound.sampled.*;
import java.io.IOException;

public class bcMusic {

    private static Clip clip;
    private static boolean isMuted = false;

    // play music from classpath
    public static void play(String resourcePath) {
        if (isMuted) return;

        try {
            if (clip != null && clip.isRunning()) {
                clip.stop();
            }

            AudioInputStream audioStream =
                    AudioSystem.getAudioInputStream(
                            bcMusic.class.getResource(resourcePath)
                    );

            clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // stop music
    public static void stop() {
        if (clip != null) {
            clip.stop();
        }
    }

    // mute toggle
    public static void toggleMute() {
        isMuted = !isMuted;

        if (isMuted) {
            stop();
        } else {
            // when unmuting: restart music
            play("/music/Host Entrance Background Music.wav");
        }
    }

    public static boolean isMuted() {
        return isMuted;
    }
}
