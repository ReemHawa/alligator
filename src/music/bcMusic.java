package music;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class bcMusic {

    private static final Logger LOG = Logger.getLogger(bcMusic.class.getName());

    private static Clip clip;
    private static boolean isMuted = false;
    private static FloatControl volumeControl;
    private static float lastVolume = 0f; // store original volume

    public static void play(String resourcePath) {
        try {
            if (clip != null) {
                clip.stop();
                clip.close();
            }

            AudioInputStream audioStream =
                    AudioSystem.getAudioInputStream(
                            bcMusic.class.getResource(resourcePath)
                    );

            clip = AudioSystem.getClip();
            clip.open(audioStream);

            // volume control
            volumeControl = (FloatControl)
                    clip.getControl(FloatControl.Type.MASTER_GAIN);

            lastVolume = volumeControl.getValue(); // remember normal volume

            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();

        } catch (UnsupportedAudioFileException |
                 IOException |
                 LineUnavailableException e) {
            LOG.log(Level.SEVERE, "Failed to play music resource: " + resourcePath, e);
        }
    }

    //  mute 
    public static void toggleMute() {
        if (volumeControl == null) return;

        if (!isMuted) {
            lastVolume = volumeControl.getValue();
            volumeControl.setValue(volumeControl.getMinimum()); // silence
        } else {
            volumeControl.setValue(lastVolume); // restore volume
        }

        isMuted = !isMuted;
    }

    public static boolean isMuted() {
        return isMuted;
    }
}
