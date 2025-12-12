package pt.feup.tvvs.pacman.audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.InputStream;

public class AudioPlayer {
    private final Clip audio;
    private float volume;

    public AudioPlayer(String audioFilepath) {
        this.audio = loadAudioFile(audioFilepath);
        this.volume = 1f;
    }

    private Clip loadAudioFile(String audioFilepath) {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(audioFilepath);
            if (inputStream == null) {
                throw new IllegalArgumentException("Audio file not found: " + audioFilepath);
            }
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
            Clip audio = AudioSystem.getClip();
            audio.open(audioInputStream);
            inputStream.close();
            audioInputStream.close();
            return audio;
        } catch (Exception e) {
            throw new RuntimeException("Could not open audio: " + audioFilepath, e);
        }
    }

    public void playOnce() {
        audio.stop();
        audio.setFramePosition(0);
        audio.start();
    }

    public void stopPlaying() {
        audio.stop();
    }

    public void playInLoop() {
        audio.stop();
        audio.setFramePosition(0);
        audio.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public boolean isPlaying() {
        return audio.isActive();
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        if (volume < 0 || volume > 1) return;
        this.volume = volume;
        FloatControl gainControl = (FloatControl) audio.getControl(FloatControl.Type.MASTER_GAIN);
        gainControl.setValue(20f * (float) Math.log10(volume));
    }
}
