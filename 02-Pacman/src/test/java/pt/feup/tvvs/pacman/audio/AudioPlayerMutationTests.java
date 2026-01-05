package pt.feup.tvvs.pacman.audio;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AudioPlayerMutationTests {

    @Test
    void constructor_opens_clip_and_closes_streams() throws Exception {
        // prepare mocks for AudioSystem
        Clip mockClip = mock(Clip.class);
        AudioInputStream mockAudioStream = mock(AudioInputStream.class);

        // create a spy input stream whose close() we can detect
        AtomicBoolean inputClosed = new AtomicBoolean(false);
        InputStream spyInput = new ByteArrayInputStream(new byte[]{1, 2, 3}) {
            @Override
            public void close() throws IOException {
                inputClosed.set(true);
                super.close();
            }
        };

        // load AudioPlayer class bytes so we can define it in a custom ClassLoader
        String classResource = "pt/feup/tvvs/pacman/audio/AudioPlayer.class";
        InputStream classBytesIn = getClass().getClassLoader().getResourceAsStream(classResource);
        assertThat(classBytesIn).isNotNull();
        byte[] classBytes = classBytesIn.readAllBytes();
        classBytesIn.close();

        // custom ClassLoader that will return our spyInput for the requested audio resource
        ClassLoader customLoader = new ClassLoader(getClass().getClassLoader()) {
            @Override
            public synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                // child-first for our target class
                if ("pt.feup.tvvs.pacman.audio.AudioPlayer".equals(name)) {
                    Class<?> loaded = findLoadedClass(name);
                    if (loaded == null) {
                        byte[] bytes = classBytes;
                        Class<?> cls = defineClass(name, bytes, 0, bytes.length);
                        if (resolve) resolveClass(cls);
                        return cls;
                    }
                    return loaded;
                }
                return super.loadClass(name, resolve);
            }

            @Override
            public InputStream getResourceAsStream(String name) {
                if ("mutant-test-resource.wav".equals(name)) {
                    return spyInput;
                }
                return super.getResourceAsStream(name);
            }
        };

        try (MockedStatic<AudioSystem> audioSystem = Mockito.mockStatic(AudioSystem.class)) {
            audioSystem.when(() -> AudioSystem.getAudioInputStream(any(InputStream.class))).thenReturn(mockAudioStream);
            audioSystem.when(AudioSystem::getClip).thenReturn(mockClip);

            // load the class via our custom loader and instantiate
            Class<?> customAudioPlayerClass = Class.forName("pt.feup.tvvs.pacman.audio.AudioPlayer", true, customLoader);
            Constructor<?> ctor = customAudioPlayerClass.getConstructor(String.class);
            Object player = ctor.newInstance("mutant-test-resource.wav");

            // verify that clip.open was called with the AudioInputStream
            verify(mockClip, times(1)).open(mockAudioStream);
            // verify that audio input stream was closed
            verify(mockAudioStream, times(1)).close();
            // verify that the original InputStream returned by our custom loader was closed
            assertThat(inputClosed.get()).isTrue();
        }
    }

    @Test
    void setVolume_accepts_zero_and_one_and_calls_floatcontrol() throws Exception {
        Clip mockClip = mock(Clip.class);
        AudioInputStream mockStream = mock(AudioInputStream.class);
        FloatControl mockControl = mock(FloatControl.class);

        when(mockClip.getControl(FloatControl.Type.MASTER_GAIN)).thenReturn(mockControl);

        try (MockedStatic<AudioSystem> audioSystem = Mockito.mockStatic(AudioSystem.class)) {
            audioSystem.when(() -> AudioSystem.getAudioInputStream(any(InputStream.class))).thenReturn(mockStream);
            audioSystem.when(AudioSystem::getClip).thenReturn(mockClip);

            AudioPlayer player = new AudioPlayer("some-audio-path.wav");

            // set to 0 should be allowed (boundary) and update volume
            player.setVolume(0f);
            assertThat(player.getVolume()).isEqualTo(0f);
            ArgumentCaptor<Float> captor = ArgumentCaptor.forClass(Float.class);
            verify(mockControl, times(1)).setValue(captor.capture());
            Float val0 = captor.getValue();
            assertThat(val0).satisfies(v -> assertThat(Float.isInfinite(v) && v < 0).isTrue());

            clearInvocations(mockControl);

            // set to 1 should be allowed (boundary) and update volume to 1
            player.setVolume(1f);
            assertThat(player.getVolume()).isEqualTo(1f);
            ArgumentCaptor<Float> captor2 = ArgumentCaptor.forClass(Float.class);
            verify(mockControl, times(1)).setValue(captor2.capture());
            assertThat(captor2.getValue()).isCloseTo(0f, org.assertj.core.data.Offset.offset(1e-6f));
        }
    }
}
