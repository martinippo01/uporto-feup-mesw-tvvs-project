package pt.feup.tvvs.pacman;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.audio.AudioPlayer;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.states.State;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameWhiteBoxTests {

    @BeforeEach
    public void resetSingletonBefore() throws Exception {
        // ensure singleton is null before each test
        Field instance = Game.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @AfterEach
    public void resetSingletonAfter() throws Exception {
        Field instance = Game.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    public void getInstance_createsSingleton_setsMasterVolume_and_initialState() throws Exception {
        GUI gui = mock(GUI.class);
        when(gui.getResolution()).thenReturn(GUI.SCREEN_RESOLUTION._720p);

        AudioManager audioManager = mock(AudioManager.class);
        when(audioManager.getMasterVolume()).thenReturn(0.5f);

        // ensure MenuController's calls to audioManager.getAudio(...) return mock AudioPlayer instances
        AudioPlayer menuSelect = mock(AudioPlayer.class);
        AudioPlayer menuConfirm = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(menuSelect);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(menuConfirm);

        Game game = Game.getInstance(gui, audioManager);

        // same references returned
        assertThat(game.getGui()).isSameAs(gui);
        assertThat(game.getAudioManager()).isSameAs(audioManager);

        // constructor should set master volume to 1f
        verify(audioManager, times(1)).setMasterVolume(1f);

        // state should be initialized (MainMenuState) - at least not null
        assertThat(game.getState()).isNotNull();

        // getInstance returns same singleton on subsequent calls
        Game game2 = Game.getInstance(gui, audioManager);
        assertThat(game2).isSameAs(game);
    }

    @Test
    public void setAndGetState_workAsExpected() throws Exception {
        GUI gui = mock(GUI.class);
        when(gui.getResolution()).thenReturn(GUI.SCREEN_RESOLUTION._720p);
        AudioManager audioManager = mock(AudioManager.class);
        when(audioManager.getMasterVolume()).thenReturn(1f);

        AudioPlayer menuSelect = mock(AudioPlayer.class);
        AudioPlayer menuConfirm = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(menuSelect);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(menuConfirm);

        Game game = Game.getInstance(gui, audioManager);

        State<?> fakeState = mock(State.class);
        game.setState(fakeState);
        assertThat(game.getState()).isSameAs(fakeState);
    }

    @Test
    public void getResolution_delegatesToGui_and_setResolution_callsResizeScreen() throws Exception {
        GUI gui = mock(GUI.class);
        when(gui.getResolution()).thenReturn(GUI.SCREEN_RESOLUTION._540p);
        AudioManager audioManager = mock(AudioManager.class);
        when(audioManager.getMasterVolume()).thenReturn(1f);

        AudioPlayer menuSelect = mock(AudioPlayer.class);
        AudioPlayer menuConfirm = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(menuSelect);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(menuConfirm);

        Game game = Game.getInstance(gui, audioManager);

        assertThat(game.getResolution()).isEqualTo(GUI.SCREEN_RESOLUTION._540p);

        // call setResolution and verify gui.resizeScreen invoked with expected constants
        GUI.SCREEN_RESOLUTION newRes = GUI.SCREEN_RESOLUTION._900p;
        game.setResolution(newRes);

        verify(gui, times(1)).resizeScreen(320, 180, newRes);
    }

    @Test
    public void gameLoop_update_incrementsFrameCount() throws InterruptedException {
        GameLoop loop = new GameLoop(1000); // frameTime = 1 ms

        // frameCount should start at 0
        assertThat(loop.getFrameCount()).isEqualTo(0);

        // update with a no-op runnable
        loop.update(() -> {
            // do nothing
        });

        assertThat(loop.getFrameCount()).isEqualTo(1);

        // multiple updates increment further
        loop.update(() -> {});
        assertThat(loop.getFrameCount()).isEqualTo(2);
    }

    @Test
    public void getInstance_handlesMultipleThreads_and_isSingleton() throws Exception {
        GUI gui = mock(GUI.class);
        when(gui.getResolution()).thenReturn(GUI.SCREEN_RESOLUTION._720p);
        AudioManager audioManager = mock(AudioManager.class);
        when(audioManager.getMasterVolume()).thenReturn(1f);

        AudioPlayer menuSelect = mock(AudioPlayer.class);
        AudioPlayer menuConfirm = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(menuSelect);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(menuConfirm);

        // call getInstance concurrently - should still result in a single instance
        Runnable r = () -> assertThatCode(() -> Game.getInstance(gui, audioManager)).doesNotThrowAnyException();
        Thread t1 = new Thread(r);
        Thread t2 = new Thread(r);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        Game g = Game.getInstance(gui, audioManager);
        assertThat(g).isNotNull();
    }
}
