package pt.feup.tvvs.pacman;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.audio.AudioPlayer;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class BlackBoxTests {

    private AudioManager audioManager;

    @BeforeEach
    public void resetSingleton() throws Exception {
        Field instanceField = AudioManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null); // reset singleton
        audioManager = AudioManager.getInstance();
    }

    private void insertAudio(String key, AudioPlayer player) throws Exception {
        Field audiosField = AudioManager.class.getDeclaredField("audios");
        audiosField.setAccessible(true);
        Map<String, AudioPlayer> map = (Map<String, AudioPlayer>) audiosField.get(audioManager);

        map.put(key, player); // insert fake audio
    }

    private void setMainMusic(AudioPlayer mainMusic) throws Exception {
        Field mainMusicField = AudioManager.class.getDeclaredField("mainMusic");
        mainMusicField.setAccessible(true);
        mainMusicField.set(audioManager, mainMusic);
    }

    @Test
    public void testVolumeLowerInvalid_zero_doesNothing() {
        float original = audioManager.getMasterVolume();

        audioManager.setMasterVolume(0);

        assertThat(audioManager.getMasterVolume()).isEqualTo(original);
    }

    @Test
    public void testVolumeLowerInvalid_negative_doesNothing() {
        float original = audioManager.getMasterVolume();

        audioManager.setMasterVolume(-0.1f);

        assertThat(audioManager.getMasterVolume()).isEqualTo(original);
    }


    @Test
    public void testVolumeValidInterior_updatesMasterAndAudios() throws Exception {

        AudioPlayer mock1 = Mockito.mock(AudioPlayer.class);
        AudioPlayer mock2 = Mockito.mock(AudioPlayer.class);
        AudioPlayer mockMain = Mockito.mock(AudioPlayer.class);

        insertAudio("a1", mock1);
        insertAudio("a2", mock2);
        setMainMusic(mockMain);

        Mockito.when(mock1.getVolume()).thenReturn(1f);
        Mockito.when(mock2.getVolume()).thenReturn(0.5f);
        Mockito.when(mockMain.getVolume()).thenReturn(0.8f);

        float original = audioManager.getMasterVolume(); // default = 1
        float newVolume = 0.5f;
        float scale = newVolume / original;

        audioManager.setMasterVolume(newVolume);

        assertThat(audioManager.getMasterVolume()).isEqualTo(newVolume);

        Mockito.verify(mockMain).setVolume(0.8f * scale);
        Mockito.verify(mock1).setVolume(1f * scale);
        Mockito.verify(mock2).setVolume(0.5f * scale);
    }

    @Test
    public void testVolumeUpperBoundary_one_setsVolume() {
        audioManager.setMasterVolume(1f);

        assertThat(audioManager.getMasterVolume()).isEqualTo(1f);
    }

    @Test
    public void testVolumeUpperInvalid_moreThanOne_doesNothing() {
        float original = audioManager.getMasterVolume();

        audioManager.setMasterVolume(2f);

        assertThat(audioManager.getMasterVolume()).isEqualTo(original);
    }
}
