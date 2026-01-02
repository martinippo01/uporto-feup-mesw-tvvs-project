package pt.feup.tvvs.pacman.states.menu;

import org.junit.jupiter.api.Test;
import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.controller.Controller;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.menu.MainMenu;
import pt.feup.tvvs.pacman.viewer.Viewer;

import java.awt.FontFormatException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class MainMenuStateWhiteBoxTests {

    @Test
    public void constructor_stores_model_and_audioManager_and_uses_overrides() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        MainMenu menu = new MainMenu(GUI.SCREEN_RESOLUTION._900p, 1f);

        // override createViewer and createController to avoid resource loading
        MainMenuState state = new MainMenuState(menu, audioManager) {
            @Override
            public Viewer<MainMenu> createViewer() {
                @SuppressWarnings("unchecked")
                Viewer<MainMenu> v = (Viewer<MainMenu>) mock(Viewer.class);
                return v;
            }

            @Override
            public Controller<MainMenu> createController(AudioManager audioManager) {
                @SuppressWarnings("unchecked")
                Controller<MainMenu> c = (Controller<MainMenu>) mock(Controller.class);
                return c;
            }
        };

        assertThat(state.getModel()).isSameAs(menu);
        assertThat(state.getAudioManager()).isSameAs(audioManager);
    }

    @Test
    public void step_delegates_to_controller_and_viewer() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        MainMenu menu = new MainMenu(GUI.SCREEN_RESOLUTION._900p, 1f);

        // mocked controller and viewer
        @SuppressWarnings("unchecked")
        Controller<MainMenu> controller = (Controller<MainMenu>) mock(Controller.class);
        @SuppressWarnings("unchecked")
        Viewer<MainMenu> viewer = (Viewer<MainMenu>) mock(Viewer.class);

        MainMenuState state = new MainMenuState(menu, audioManager) {
            @Override
            public Viewer<MainMenu> createViewer() {
                return viewer;
            }

            @Override
            public Controller<MainMenu> createController(AudioManager audioManager) {
                return controller;
            }
        };

        Game game = mock(Game.class);
        GUI gui = mock(GUI.class);
        List<GUI.ACTION> actions = List.of(GUI.ACTION.NONE);
        when(gui.getNextAction()).thenReturn(actions);

        // call step
        state.step(game, gui, 123L);

        // controller.step should be invoked, viewer.drawElement should be invoked
        verify(controller).step(game, actions, 123L);
        verify(viewer).drawElement(gui, menu, 123L);
    }

    @Test
    public void step_propagates_checked_exceptions_from_controller() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        MainMenu menu = new MainMenu(GUI.SCREEN_RESOLUTION._900p, 1f);

        @SuppressWarnings("unchecked")
        Controller<MainMenu> controller = (Controller<MainMenu>) mock(Controller.class);
        doThrow(new IOException("controller io")).when(controller).step(any(), any(), anyLong());

        @SuppressWarnings("unchecked")
        Viewer<MainMenu> viewer = (Viewer<MainMenu>) mock(Viewer.class);

        MainMenuState state = new MainMenuState(menu, audioManager) {
            @Override
            public Viewer<MainMenu> createViewer() {
                return viewer;
            }

            @Override
            public Controller<MainMenu> createController(AudioManager audioManager) {
                return controller;
            }
        };

        Game game = mock(Game.class);
        GUI gui = mock(GUI.class);
        when(gui.getNextAction()).thenReturn(List.of(GUI.ACTION.NONE));

        assertThatThrownBy(() -> state.step(game, gui, 1L)).isInstanceOf(IOException.class).hasMessageContaining("controller io");
    }

    @Test
    public void step_propagates_runtime_exceptions_from_viewer() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        MainMenu menu = new MainMenu(GUI.SCREEN_RESOLUTION._900p, 1f);

        @SuppressWarnings("unchecked")
        Controller<MainMenu> controller = (Controller<MainMenu>) mock(Controller.class);

        @SuppressWarnings("unchecked")
        Viewer<MainMenu> viewer = (Viewer<MainMenu>) mock(Viewer.class);
        doThrow(new RuntimeException("viewer boom")).when(viewer).drawElement(any(), any(), anyLong());

        MainMenuState state = new MainMenuState(menu, audioManager) {
            @Override
            public Viewer<MainMenu> createViewer() {
                return viewer;
            }

            @Override
            public Controller<MainMenu> createController(AudioManager audioManager) {
                return controller;
            }
        };

        Game game = mock(Game.class);
        GUI gui = mock(GUI.class);
        when(gui.getNextAction()).thenReturn(List.of(GUI.ACTION.NONE));

        assertThatThrownBy(() -> state.step(game, gui, 5L)).isInstanceOf(RuntimeException.class).hasMessageContaining("viewer boom");
    }
}
