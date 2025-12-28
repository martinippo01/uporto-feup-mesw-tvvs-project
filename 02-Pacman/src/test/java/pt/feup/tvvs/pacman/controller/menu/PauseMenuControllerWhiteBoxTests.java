package pt.feup.tvvs.pacman.controller.menu;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.audio.AudioPlayer;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.menu.PauseMenu;
import pt.feup.tvvs.pacman.states.State;
import pt.feup.tvvs.pacman.states.menu.MainMenuState;

import java.io.IOException;
import java.net.URISyntaxException;
import java.awt.FontFormatException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class PauseMenuControllerWhiteBoxTests {

    @Test
    public void select_resume_returns_to_paused_state_and_plays_confirm() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer select = mock(AudioPlayer.class);
        AudioPlayer confirm = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(select);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(confirm);

        State<?> pausedState = mock(State.class);
        PauseMenu menu = new PauseMenu(pausedState, GUI.SCREEN_RESOLUTION._900p, 0.5f);
        menu.setSelectedOption(0); // Resume

        PauseMenuController controller = new PauseMenuController(menu, audioManager);

        Game game = mock(Game.class);

        controller.step(game, List.of(GUI.ACTION.SELECT), 0);

        verify(confirm, times(1)).playOnce();
        verify(game).setState(pausedState);
    }

    @Test
    public void select_exit_sets_main_menu_state() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer select = mock(AudioPlayer.class);
        AudioPlayer confirm = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(select);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(confirm);
        when(audioManager.getMasterVolume()).thenReturn(0.7f);

        State<?> pausedState = mock(State.class);
        PauseMenu menu = new PauseMenu(pausedState, GUI.SCREEN_RESOLUTION._360p, 0.7f);
        menu.setSelectedOption(3); // Exit

        PauseMenuController controller = new PauseMenuController(menu, audioManager);

        Game game = mock(Game.class);
        when(game.getResolution()).thenReturn(GUI.SCREEN_RESOLUTION._360p);
        when(game.getAudioManager()).thenReturn(audioManager);

        try (MockedConstruction<MainMenuState> mocked = mockConstruction(MainMenuState.class)) {
            controller.step(game, List.of(GUI.ACTION.SELECT), 0);

            ArgumentCaptor<pt.feup.tvvs.pacman.states.State> captor = ArgumentCaptor.forClass(pt.feup.tvvs.pacman.states.State.class);
            verify(game).setState(captor.capture());
            assertThat(captor.getValue()).isInstanceOf(MainMenuState.class);
            assertThat(mocked.constructed()).hasSize(1);
        }
    }

    @Test
    public void select_resolution_changes_resolution_and_updates_model() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer select = mock(AudioPlayer.class);
        AudioPlayer confirm = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(select);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(confirm);

        State<?> pausedState = mock(State.class);
        PauseMenu menu = new PauseMenu(pausedState, GUI.SCREEN_RESOLUTION._360p, 0.5f);
        menu.setSelectedOption(1); // Resolution

        PauseMenuController controller = new PauseMenuController(menu, audioManager);

        Game game = mock(Game.class);
        when(game.getResolution()).thenReturn(GUI.SCREEN_RESOLUTION._360p);

        controller.step(game, List.of(GUI.ACTION.SELECT), 0);

        verify(game).setResolution(GUI.SCREEN_RESOLUTION._540p);
        assertThat(menu.getOptions().get(1).getText()).contains("540p");
        verify(confirm, times(1)).playOnce();
    }

    @Test
    public void select_masterVolume_changes_volume_and_updates_model() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer select = mock(AudioPlayer.class);
        AudioPlayer confirm = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(select);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(confirm);
        when(audioManager.getMasterVolume()).thenReturn(0.5f);

        State<?> pausedState = mock(State.class);
        PauseMenu menu = new PauseMenu(pausedState, GUI.SCREEN_RESOLUTION._900p, 0.5f);
        menu.setSelectedOption(2); // Master Volume

        PauseMenuController controller = new PauseMenuController(menu, audioManager);

        Game game = mock(Game.class);
        GUI gui = mock(GUI.class);
        when(game.getGui()).thenReturn(gui);
        when(game.getAudioManager()).thenReturn(audioManager);

        controller.step(game, List.of(GUI.ACTION.SELECT), 0);

        verify(audioManager).setMasterVolume(0.6f);
        assertThat(menu.getOptions().get(2).getText()).contains("Master Volume");
        verify(gui).clear();
        verify(confirm, times(1)).playOnce();
    }

    @Test
    public void non_select_actions_delegate_to_parent_and_play_select() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer select = mock(AudioPlayer.class);
        AudioPlayer confirm = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(select);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(confirm);

        State<?> pausedState = mock(State.class);
        PauseMenu menu = new PauseMenu(pausedState, GUI.SCREEN_RESOLUTION._900p, 0.5f);

        PauseMenuController controller = new PauseMenuController(menu, audioManager);

        Game game = mock(Game.class);

        controller.step(game, List.of(GUI.ACTION.UP), 0);
        verify(select, times(1)).playOnce();

        controller.step(game, List.of(GUI.ACTION.DOWN), 0);
        verify(select, times(2)).playOnce();
    }

    @Test
    public void select_when_map_related_step_propagates_exceptions() throws Exception {
        // ensure that if incrementResolution or setResolution throws, exception propagates
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer select = mock(AudioPlayer.class);
        AudioPlayer confirm = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(select);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(confirm);

        State<?> pausedState = mock(State.class);
        PauseMenu menu = new PauseMenu(pausedState, GUI.SCREEN_RESOLUTION._900p, 0.5f);
        menu.setSelectedOption(1); // Resolution

        PauseMenuController controller = new PauseMenuController(menu, audioManager) {
            @Override
            public void step(Game game, List<GUI.ACTION> actions, long time) throws IOException, URISyntaxException, FontFormatException {
                // simulate a runtime exception during resolution change
                throw new IOException("boom");
            }
        };

        Game game = mock(Game.class);

        assertThrows(IOException.class, () -> controller.step(game, List.of(GUI.ACTION.SELECT), 0));
    }
}
