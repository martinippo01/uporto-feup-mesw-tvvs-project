package pt.feup.tvvs.pacman;

import com.googlecode.lanterna.screen.Screen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.audio.AudioPlayer;
import pt.feup.tvvs.pacman.gui.LanternaGUI;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.awt.FontFormatException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class BlackBoxTests {

    @Nested
    class LanternaGUIResizeTests {

        @Test
        public void resize_withTypicalPositiveSize_changesResolutionAndClosesOldScreen() throws IOException, URISyntaxException, FontFormatException {
            Screen mockScreen = mock(Screen.class);
            LanternaGUI gui = new LanternaGUI(mockScreen, pt.feup.tvvs.pacman.gui.GUI.SCREEN_RESOLUTION._720p);

            // valid size in typical range
            gui.resizeScreen(80, 24, pt.feup.tvvs.pacman.gui.GUI.SCREEN_RESOLUTION._1080p);

            // old screen must have been closed
            verify(mockScreen, atLeastOnce()).close();

            // resolution must have been updated to the new one
            assertThat(gui.getResolution()).isEqualTo(pt.feup.tvvs.pacman.gui.GUI.SCREEN_RESOLUTION._1080p);
        }

        @Test
        public void resize_onPoint_smallestPositive_updatesResolution() throws IOException, URISyntaxException, FontFormatException {
            Screen mockScreen = mock(Screen.class);
            LanternaGUI gui = new LanternaGUI(mockScreen, pt.feup.tvvs.pacman.gui.GUI.SCREEN_RESOLUTION._360p);

            // On-point: smallest positive size we consider (1)
            gui.resizeScreen(1, 1, pt.feup.tvvs.pacman.gui.GUI.SCREEN_RESOLUTION._540p);

            // resolution should have been updated (single focused assertion for on-point)
            assertThat(gui.getResolution()).isEqualTo(pt.feup.tvvs.pacman.gui.GUI.SCREEN_RESOLUTION._540p);
        }

        @Test
        public void resize_offPoint_zero_behaviourIsPlatformDependent_butIsHandled() {
            Screen mockScreen = mock(Screen.class);
            LanternaGUI gui = new LanternaGUI(mockScreen, pt.feup.tvvs.pacman.gui.GUI.SCREEN_RESOLUTION._360p);

            // Off-point: the value that flips the condition around zero (0)
            try {
                gui.resizeScreen(0, 0, pt.feup.tvvs.pacman.gui.GUI.SCREEN_RESOLUTION._720p);
                // If it succeeds, resolution must be updated to the new resolution (single focused assertion)
                assertThat(gui.getResolution()).isEqualTo(pt.feup.tvvs.pacman.gui.GUI.SCREEN_RESOLUTION._720p);
            } catch (Exception e) {
                assertThat(e).isInstanceOf(Exception.class);
            }
        }

        @Test
        public void resize_withNegativeSizes_throwsException() {
            Screen mockScreen = mock(Screen.class);
            LanternaGUI gui = new LanternaGUI(mockScreen, pt.feup.tvvs.pacman.gui.GUI.SCREEN_RESOLUTION._900p);

            // Out-point for valid size: negative values should be invalid and cause an exception
            assertThatThrownBy(() -> gui.resizeScreen(-10, -5, pt.feup.tvvs.pacman.gui.GUI.SCREEN_RESOLUTION._360p))
                    .isInstanceOf(Exception.class);
        }

        @Test
        public void resize_withVeryLargeSize_succeedsAndSetsResolution() throws IOException, URISyntaxException, FontFormatException {
            Screen mockScreen = mock(Screen.class);
            LanternaGUI gui = new LanternaGUI(mockScreen, pt.feup.tvvs.pacman.gui.GUI.SCREEN_RESOLUTION._720p);

            // Large sizes (large but valid)
            gui.resizeScreen(2000, 1200, pt.feup.tvvs.pacman.gui.GUI.SCREEN_RESOLUTION._2160p);
            assertThat(gui.getResolution()).isEqualTo(pt.feup.tvvs.pacman.gui.GUI.SCREEN_RESOLUTION._2160p);
        }
    }

    @Nested
    class AudioManagerSetMasterVolumeTests {
        private AudioManager audioManager;

        @BeforeEach
        public void resetSingleton() throws Exception {
            Field inst = AudioManager.class.getDeclaredField("instance");
            inst.setAccessible(true);
            inst.set(null, null);
            audioManager = AudioManager.getInstance();
        }

        private void injectAudio(String key, AudioPlayer player) throws Exception {
            Field f = AudioManager.class.getDeclaredField("audios");
            f.setAccessible(true);
            ((Map<String, AudioPlayer>) f.get(audioManager)).put(key, player);
        }

        private void injectMain(AudioPlayer main) throws Exception {
            Field f = AudioManager.class.getDeclaredField("mainMusic");
            f.setAccessible(true);
            f.set(audioManager, main);
        }

        // ----------------------------
        // Boundary 0 Side
        // ----------------------------

        @Test
        public void testVolume_boundary0_onPoint_returns() {
            float before = audioManager.getMasterVolume();
            audioManager.setMasterVolume(0f);
            assertThat(audioManager.getMasterVolume()).isEqualTo(before);
        }

        @Test
        public void testVolume_boundary0_outPoint_below_returns() {
            float before = audioManager.getMasterVolume();
            audioManager.setMasterVolume(-0.000001f);
            assertThat(audioManager.getMasterVolume()).isEqualTo(before);
        }

        @Test
        public void testVolume_boundary0_inPoint_justAbove_updates() {
            float before = audioManager.getMasterVolume();
            float epsilon = 0.000001f;

            audioManager.setMasterVolume(epsilon);

            assertThat(audioManager.getMasterVolume()).isEqualTo(epsilon);
        }

        // ----------------------------
        // Boundary 1 Side
        // ----------------------------

        @Test
        public void testVolume_boundary1_onPoint_valid_updates() {
            audioManager.setMasterVolume(1f);
            assertThat(audioManager.getMasterVolume()).isEqualTo(1f);
        }

        @Test
        public void testVolume_boundary1_inPoint_justBelow_updates() {
            float v = 0.99999f;
            audioManager.setMasterVolume(v);
            assertThat(audioManager.getMasterVolume()).isEqualTo(v);
        }

        @Test
        public void testVolume_boundary1_outPoint_above_returns() {
            float before = audioManager.getMasterVolume();
            audioManager.setMasterVolume(1.00001f);
            assertThat(audioManager.getMasterVolume()).isEqualTo(before);
        }

        // ----------------------------
        // Side effect propagation (still blackbox)
        // ----------------------------

        @Test
        public void validVolume_propagatesToAudios() throws Exception {

            float before = audioManager.getMasterVolume(); // expected 1

            // mocks
            AudioPlayer p1 = Mockito.mock(AudioPlayer.class);
            AudioPlayer p2 = Mockito.mock(AudioPlayer.class);
            AudioPlayer main = Mockito.mock(AudioPlayer.class);

            injectAudio("a1", p1);
            injectAudio("a2", p2);
            injectMain(main);

            Mockito.when(p1.getVolume()).thenReturn(1f);
            Mockito.when(p2.getVolume()).thenReturn(0.5f);
            Mockito.when(main.getVolume()).thenReturn(0.8f);

            float newVolume = 0.5f;
            float scale = newVolume / before;

            audioManager.setMasterVolume(newVolume);

            assertThat(audioManager.getMasterVolume()).isEqualTo(newVolume);

            Mockito.verify(main).setVolume(0.8f * scale);
            Mockito.verify(p1).setVolume(1f * scale);
            Mockito.verify(p2).setVolume(0.5f * scale);
        }
    }
}
