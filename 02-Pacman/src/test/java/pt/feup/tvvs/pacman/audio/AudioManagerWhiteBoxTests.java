package pt.feup.tvvs.pacman.audio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class AudioManagerWhiteBoxTests {

    @BeforeEach
    void resetSingleton() throws Exception {
        Field instanceField = AudioManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    private AudioManager freshManager() {
        return AudioManager.getInstance();
    }

    private void putMockIntoAudios(AudioManager manager, String key, AudioPlayer mock) throws Exception {
        Field audiosField = AudioManager.class.getDeclaredField("audios");
        audiosField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, AudioPlayer> audios = (Map<String, AudioPlayer>) audiosField.get(manager);
        audios.put(key, mock);
    }

    @Test
    void singleton_getInstance_returnsSameInstance() {
        AudioManager m1 = freshManager();
        AudioManager m2 = freshManager();
        assertThat(m1).isSameAs(m2);
    }

    @Test
    void addAudio_whenKeyAlreadyExists_doesNotReplaceExisting() throws Exception {
        AudioManager manager = freshManager();
        AudioPlayer existing = mock(AudioPlayer.class);
        putMockIntoAudios(manager, "music", existing);

        // calling addAudio with an existing key should not create a new AudioPlayer or alter the map
        manager.addAudio("music", "somefile.wav");

        assertThat(manager.getAudio("music")).isSameAs(existing);
        // no interactions should have occurred on the mock because addAudio returns early
        verifyNoInteractions(existing);
    }

    @Test
    void addAudio_whenFileMissing_throwsRuntimeException() {
        AudioManager manager = freshManager();
        // This will attempt to construct a real AudioPlayer which should fail for a non-existent resource
        assertThatThrownBy(() -> manager.addAudio("missing", "nonexistent-audio-file-hopefully-not-present.wav"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void addAudio_successful_constructsAudioPlayerAndSetsVolume() {
        AudioManager manager = freshManager();

        try (MockedConstruction<AudioPlayer> mocked = mockConstruction(AudioPlayer.class, (mock, ctx) -> {
            when(mock.getVolume()).thenReturn(0.9f);
        })) {
            // should not throw because AudioPlayer constructor is mocked
            manager.addAudio("new-audio", "somepath.wav");

            // ensure a constructed mock exists
            assertThat(mocked.constructed()).hasSize(1);
            AudioPlayer constructed = mocked.constructed().get(0);

            // verify setVolume was called with currentVolume * masterVolume (masterVolume defaults to 1)
            verify(constructed, times(1)).setVolume(0.9f);

            // manager.getAudio should return the constructed mock
            assertThat(manager.getAudio("new-audio")).isSameAs(constructed);
        }
    }

    @Test
    void stopAllAudios_invokesStopPlayingOnAll() throws Exception {
        AudioManager manager = freshManager();
        AudioPlayer a1 = mock(AudioPlayer.class);
        AudioPlayer a2 = mock(AudioPlayer.class);
        putMockIntoAudios(manager, "a1", a1);
        putMockIntoAudios(manager, "a2", a2);

        manager.stopAllAudios();

        verify(a1, times(1)).stopPlaying();
        verify(a2, times(1)).stopPlaying();
    }

    @Test
    void setMainMusic_setsVolumeAndStoresMainMusic() {
        AudioManager manager = freshManager();
        AudioPlayer main = mock(AudioPlayer.class);
        when(main.getVolume()).thenReturn(0.75f);

        manager.setMainMusic(main);

        // setMainMusic should set the main's volume = currentVolume * masterVolume (masterVolume defaults to 1)
        verify(main, times(1)).setVolume(0.75f);
        // getAudio for an unrelated key should be null
        assertThat(manager.getAudio("no-such-audio")).isNull();
        // master volume should remain at default
        assertThat(manager.getMasterVolume()).isEqualTo(1f);
    }

    @Test
    void setMasterVolume_changesVolumesCorrectly_and_invalidValuesAreIgnored() throws Exception {
        AudioManager manager = freshManager();

        AudioPlayer main = mock(AudioPlayer.class);
        when(main.getVolume()).thenReturn(0.8f);

        AudioPlayer other = mock(AudioPlayer.class);
        when(other.getVolume()).thenReturn(0.6f);

        // inject both
        putMockIntoAudios(manager, "other", other);
        manager.setMainMusic(main);

        // clear previous interactions so we can assert exactly what happens next
        clearInvocations(main, other);

        // set to 0.5 should multiply volumes by 0.5 / masterVolume (masterVolume initially 1)
        manager.setMasterVolume(0.5f);

        // capture the calls
        ArgumentCaptor<Float> mainCaptor = ArgumentCaptor.forClass(Float.class);
        ArgumentCaptor<Float> otherCaptor = ArgumentCaptor.forClass(Float.class);

        verify(main, times(1)).setVolume(mainCaptor.capture());
        verify(other, times(1)).setVolume(otherCaptor.capture());

        // expected values: main: 0.8 * 0.5 = 0.4; other: 0.6 * 0.5 = 0.3
        assertThat(mainCaptor.getValue()).isCloseTo(0.4f, within(1e-6f));
        assertThat(otherCaptor.getValue()).isCloseTo(0.3f, within(1e-6f));

        // Now reset recorded interactions and call invalid values; they should not trigger any setVolume calls
        clearInvocations(main, other);
        manager.setMasterVolume(0f);
        manager.setMasterVolume(1.1f);

        // no interactions should have occurred during invalid calls
        verifyNoInteractions(main, other);
    }

    // helper to avoid repeating float comparison import
    private org.assertj.core.data.Offset<Float> within(float delta) {
        return org.assertj.core.data.Offset.offset(delta);
    }
}
