package pt.feup.tvvs.pacman.states.menu;

import org.junit.jupiter.api.Test;
import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.controller.Controller;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.menu.PauseMenu;
import pt.feup.tvvs.pacman.viewer.Viewer;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class PauseMenuStateWhiteBoxTests {

    @Test
    public void constructor_stores_model_and_audioManager_and_uses_overrides() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        PauseMenu menu = new PauseMenu(null, GUI.SCREEN_RESOLUTION._900p, 1f);

        PauseMenuState state = new PauseMenuState(menu, audioManager) {
            @Override
            public Viewer<PauseMenu> createViewer() {
                @SuppressWarnings("unchecked")
                Viewer<PauseMenu> v = (Viewer<PauseMenu>) mock(Viewer.class);
                return v;
            }

            @Override
            public Controller<PauseMenu> createController(AudioManager audioManager) {
                @SuppressWarnings("unchecked")
                Controller<PauseMenu> c = (Controller<PauseMenu>) mock(Controller.class);
                return c;
            }
        };

        assertThat(state.getModel()).isSameAs(menu);
        assertThat(state.getAudioManager()).isSameAs(audioManager);
    }

    @Test
    public void step_delegates_to_controller_and_viewer() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        PauseMenu menu = new PauseMenu(null, GUI.SCREEN_RESOLUTION._900p, 1f);

        @SuppressWarnings("unchecked")
        Controller<PauseMenu> controller = (Controller<PauseMenu>) mock(Controller.class);
        @SuppressWarnings("unchecked")
        Viewer<PauseMenu> viewer = (Viewer<PauseMenu>) mock(Viewer.class);

        PauseMenuState state = new PauseMenuState(menu, audioManager) {
            @Override
            public Viewer<PauseMenu> createViewer() {
                return viewer;
            }

            @Override
            public Controller<PauseMenu> createController(AudioManager audioManager) {
                return controller;
            }
        };

        Game game = mock(Game.class);
        GUI gui = mock(GUI.class);
        List<GUI.ACTION> actions = List.of(GUI.ACTION.NONE);
        when(gui.getNextAction()).thenReturn(actions);

        state.step(game, gui, 99L);

        verify(controller).step(game, actions, 99L);
        verify(viewer).drawElement(gui, menu, 99L);
    }

    @Test
    public void step_propagates_checked_exceptions_from_controller() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        PauseMenu menu = new PauseMenu(null, GUI.SCREEN_RESOLUTION._900p, 1f);

        @SuppressWarnings("unchecked")
        Controller<PauseMenu> controller = (Controller<PauseMenu>) mock(Controller.class);
        doThrow(new IOException("controller fail")).when(controller).step(any(), any(), anyLong());

        @SuppressWarnings("unchecked")
        Viewer<PauseMenu> viewer = (Viewer<PauseMenu>) mock(Viewer.class);

        PauseMenuState state = new PauseMenuState(menu, audioManager) {
            @Override
            public Viewer<PauseMenu> createViewer() {
                return viewer;
            }

            @Override
            public Controller<PauseMenu> createController(AudioManager audioManager) {
                return controller;
            }
        };

        Game game = mock(Game.class);
        GUI gui = mock(GUI.class);
        when(gui.getNextAction()).thenReturn(List.of(GUI.ACTION.NONE));

        assertThatThrownBy(() -> state.step(game, gui, 2L)).isInstanceOf(IOException.class).hasMessageContaining("controller fail");
    }

    @Test
    public void step_propagates_runtime_exceptions_from_viewer() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        PauseMenu menu = new PauseMenu(null, GUI.SCREEN_RESOLUTION._900p, 1f);

        @SuppressWarnings("unchecked")
        Controller<PauseMenu> controller = (Controller<PauseMenu>) mock(Controller.class);

        @SuppressWarnings("unchecked")
        Viewer<PauseMenu> viewer = (Viewer<PauseMenu>) mock(Viewer.class);
        doThrow(new RuntimeException("viewer explode")).when(viewer).drawElement(any(), any(), anyLong());

        PauseMenuState state = new PauseMenuState(menu, audioManager) {
            @Override
            public Viewer<PauseMenu> createViewer() {
                return viewer;
            }

            @Override
            public Controller<PauseMenu> createController(AudioManager audioManager) {
                return controller;
            }
        };

        Game game = mock(Game.class);
        GUI gui = mock(GUI.class);
        when(gui.getNextAction()).thenReturn(List.of(GUI.ACTION.NONE));

        assertThatThrownBy(() -> state.step(game, gui, 3L)).isInstanceOf(RuntimeException.class).hasMessageContaining("viewer explode");
    }
}
