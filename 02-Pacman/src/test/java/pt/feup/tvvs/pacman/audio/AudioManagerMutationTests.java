package pt.feup.tvvs.pacman.audio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AudioManagerMutationTests {

    @BeforeEach
    void resetSingleton() throws Exception {
        Field instanceField = AudioManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    private AudioManager freshManager() {
        return AudioManager.getInstance();
    }

    // helper to set the private masterVolume field directly (so we can test code paths where masterVolume != 1)
    private void setMasterVolumeField(AudioManager manager, float value) throws Exception {
        Field masterField = AudioManager.class.getDeclaredField("masterVolume");
        masterField.setAccessible(true);
        masterField.setFloat(manager, value);
    }

    // helper to put a mock AudioPlayer into the manager's private audios map
    @SuppressWarnings("unchecked")
    private void putMockIntoAudios(AudioManager manager, String key, AudioPlayer mock) throws Exception {
        Field audiosField = AudioManager.class.getDeclaredField("audios");
        audiosField.setAccessible(true);
        Map<String, AudioPlayer> audios = (Map<String, AudioPlayer>) audiosField.get(manager);
        audios.put(key, mock);
    }

    private org.assertj.core.data.Offset<Float> within(float delta) {
        return org.assertj.core.data.Offset.offset(delta);
    }

    @Test
    public void addAudio_respects_nonOne_masterVolume() throws Exception {
        AudioManager manager = freshManager();
        // set masterVolume to a value other than 1 to differentiate multiplication vs division
        setMasterVolumeField(manager, 0.5f);

        try (MockedConstruction<AudioPlayer> mocked = mockConstruction(AudioPlayer.class, (mock, ctx) -> {
            when(mock.getVolume()).thenReturn(0.8f);
        })) {
            manager.addAudio("mutant-test", "somepath.wav");

            // ensure a constructed mock exists
            assertThat(mocked.constructed()).hasSize(1);
            AudioPlayer constructed = mocked.constructed().get(0);

            // expected: setVolume should be called with 0.8 * masterVolume (0.5) = 0.4
            verify(constructed, times(1)).setVolume(0.4f);
        }
    }

    @Test
    public void setMainMusic_respects_nonOne_masterVolume() throws Exception {
        AudioManager manager = freshManager();
        setMasterVolumeField(manager, 0.25f);

        AudioPlayer main = mock(AudioPlayer.class);
        when(main.getVolume()).thenReturn(0.6f);

        manager.setMainMusic(main);

        // expected: setVolume called with 0.6 * 0.25 = 0.15
        verify(main, times(1)).setVolume(0.15f);
    }

    @Test
    public void setMasterVolume_uses_division_by_previous_masterVolume_and_allows_one_as_valid_boundary() throws Exception {
        AudioManager manager = freshManager();

        // create mocks and insert them without using manager setters (to avoid extra volume adjustments)
        AudioPlayer main = mock(AudioPlayer.class);
        when(main.getVolume()).thenReturn(0.8f);

        AudioPlayer other = mock(AudioPlayer.class);
        when(other.getVolume()).thenReturn(0.6f);

        // set a previous masterVolume different from 1
        setMasterVolumeField(manager, 0.25f);

        // inject mainMusic directly and put other into audios map
        Field mainField = AudioManager.class.getDeclaredField("mainMusic");
        mainField.setAccessible(true);
        mainField.set(manager, main);

        putMockIntoAudios(manager, "other", other);

        // clear any invocations (we didn't call setters, but keep consistent pattern)
        clearInvocations(main, other);

        // now call setMasterVolume to change to 0.5 and expect division by previous master (0.25)
        manager.setMasterVolume(0.5f);

        // expected: main new volume = 0.8 * 0.5 / 0.25 = 1.6
        //           other new volume = 0.6 * 0.5 / 0.25 = 1.2
        ArgumentCaptor<Float> mainCaptor = ArgumentCaptor.forClass(Float.class);
        ArgumentCaptor<Float> otherCaptor = ArgumentCaptor.forClass(Float.class);
        verify(main, times(1)).setVolume(mainCaptor.capture());
        verify(other, times(1)).setVolume(otherCaptor.capture());
        assertThat(mainCaptor.getValue()).isCloseTo(1.6f, within(1e-6f));
        assertThat(otherCaptor.getValue()).isCloseTo(1.2f, within(1e-6f));

        clearInvocations(main, other);

        // additionally verify that setting masterVolume to 1 is considered valid and applies the ratio
        // set previous master to 0.5 and then set to 1.0 -> multiplier = 1 / 0.5 = 2
        setMasterVolumeField(manager, 0.5f);

        manager.setMasterVolume(1.0f);

        ArgumentCaptor<Float> mainCaptor2 = ArgumentCaptor.forClass(Float.class);
        ArgumentCaptor<Float> otherCaptor2 = ArgumentCaptor.forClass(Float.class);
        verify(main, times(1)).setVolume(mainCaptor2.capture());
        verify(other, times(1)).setVolume(otherCaptor2.capture());
        assertThat(mainCaptor2.getValue()).isCloseTo(0.8f * 1.0f / 0.5f, within(1e-6f));
        assertThat(otherCaptor2.getValue()).isCloseTo(0.6f * 1.0f / 0.5f, within(1e-6f));
    }
}
