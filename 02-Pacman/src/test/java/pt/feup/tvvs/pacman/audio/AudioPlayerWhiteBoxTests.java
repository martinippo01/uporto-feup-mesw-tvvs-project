package pt.feup.tvvs.pacman.audio;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.InOrder;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AudioPlayerWhiteBoxTests {

    @Test
    public void constructor_successful_and_play_controls_behaviour() throws Exception {
        // prepare mocks
        Clip mockClip = mock(Clip.class);
        AudioInputStream mockStream = mock(AudioInputStream.class);
        // mock static AudioSystem methods
        try (MockedStatic<AudioSystem> audioSystem = Mockito.mockStatic(AudioSystem.class)) {
            audioSystem.when(() -> AudioSystem.getAudioInputStream(any(InputStream.class))).thenReturn(mockStream);
            audioSystem.when(AudioSystem::getClip).thenReturn(mockClip);

            // supply a non-null InputStream via resource path by letting the classloader return a stream
            // we create a small temp resource by using a path that exists not necessary; AudioSystem is mocked so its input is fine
            AudioPlayer player = new AudioPlayer("some-audio-path.wav");

            // default volume should be 1
            assertThat(player.getVolume()).isEqualTo(1f);

            // playOnce should stop, set frame pos to 0 and start
            player.playOnce();
            InOrder inOrder = inOrder(mockClip);
            inOrder.verify(mockClip).stop();
            inOrder.verify(mockClip).setFramePosition(0);
            inOrder.verify(mockClip).start();

            // stopPlaying
            clearInvocations(mockClip);
            player.stopPlaying();
            verify(mockClip, times(1)).stop();

            // playInLoop should stop, setFramePosition, then loop with LOOP_CONTINUOUSLY
            clearInvocations(mockClip);
            player.playInLoop();
            InOrder inOrder2 = inOrder(mockClip);
            inOrder2.verify(mockClip).stop();
            inOrder2.verify(mockClip).setFramePosition(0);
            inOrder2.verify(mockClip).loop(Clip.LOOP_CONTINUOUSLY);

            // isPlaying should return clip.isActive
            when(mockClip.isActive()).thenReturn(true);
            assertThat(player.isPlaying()).isTrue();
            when(mockClip.isActive()).thenReturn(false);
            assertThat(player.isPlaying()).isFalse();
        }
    }

    @Test
    public void constructor_when_resource_missing_throwsRuntimeException() {
        // If AudioSystem is not mocked and resource is missing, constructor should throw RuntimeException
        // Pick a path extremely unlikely to be present
        assertThatThrownBy(() -> new AudioPlayer("this-file-should-not-exist-1234567890.wav"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void setVolume_valid_callsFloatControl_and_invalid_values_ignored() throws Exception {
        Clip mockClip = mock(Clip.class);
        AudioInputStream mockStream = mock(AudioInputStream.class);
        FloatControl mockControl = mock(FloatControl.class);

        // when getControl called, return our mock control
        when(mockClip.getControl(FloatControl.Type.MASTER_GAIN)).thenReturn(mockControl);

        try (MockedStatic<AudioSystem> audioSystem = Mockito.mockStatic(AudioSystem.class)) {
            audioSystem.when(() -> AudioSystem.getAudioInputStream(any(InputStream.class))).thenReturn(mockStream);
            audioSystem.when(AudioSystem::getClip).thenReturn(mockClip);

            AudioPlayer player = new AudioPlayer("whatever.wav");

            // initial getVolume is 1
            assertThat(player.getVolume()).isEqualTo(1f);

            // setVolume valid value within [0,1]
            player.setVolume(0.5f);
            // volume field updated
            assertThat(player.getVolume()).isEqualTo(0.5f);
            // verify FloatControl.setValue called with 20*log10(0.5)
            float expected = 20f * (float) Math.log10(0.5f);
            verify(mockControl, times(1)).setValue(expected);

            // invalid values should be ignored and not call getControl
            clearInvocations(mockClip, mockControl);
            player.setVolume(-0.1f);
            player.setVolume(1.1f);
            // no interactions on clip or control
            verifyNoInteractions(mockClip, mockControl);
        }
    }
}
