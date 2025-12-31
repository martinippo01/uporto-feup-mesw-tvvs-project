package pt.feup.tvvs.pacman.states;

import org.junit.jupiter.api.Test;
import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.controller.Controller;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.viewer.Viewer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.awt.FontFormatException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class StateWhiteBoxTests {

    @Test
    public void constructor_initializes_model_and_audio_and_uses_overrides() throws Exception {
        String model = "myModel";
        AudioManager audio = mock(AudioManager.class);
        Viewer<String> viewer = mock(Viewer.class);
        Controller<String> controller = mock(Controller.class);

        State<String> state = new State<String>(model, audio) {
            @Override
            public Viewer<String> createViewer() throws IOException {
                return viewer;
            }

            @Override
            public Controller<String> createController(AudioManager audioManager) {
                return controller;
            }
        };

        assertThat(state.getModel()).isSameAs(model);
        assertThat(state.getAudioManager()).isSameAs(audio);

        // calling step should delegate to provided controller and viewer
        GUI gui = mock(GUI.class);
        when(gui.getNextAction()).thenReturn(Collections.emptyList());
        Game game = mock(Game.class);

        state.step(game, gui, 42L);

        verify(controller).step(eq(game), anyList(), eq(42L));
        verify(viewer).drawElement(eq(gui), eq(model), eq(42L));
    }

    @Test
    public void step_propagates_controller_exceptions() throws Exception {
        String model = "m";
        AudioManager audio = mock(AudioManager.class);
        Controller<String> controller = mock(Controller.class);
        Viewer<String> viewer = mock(Viewer.class);

        doThrow(new IOException("boom")).when(controller).step(any(), anyList(), anyLong());

        State<String> state = new State<String>(model, audio) {
            @Override
            public Viewer<String> createViewer() throws IOException {
                return viewer;
            }

            @Override
            public Controller<String> createController(AudioManager audioManager) {
                return controller;
            }
        };

        GUI gui = mock(GUI.class);
        when(gui.getNextAction()).thenReturn(Collections.emptyList());
        Game game = mock(Game.class);

        assertThatThrownBy(() -> state.step(game, gui, 7L)).isInstanceOf(IOException.class).hasMessageContaining("boom");
    }

    @Test
    public void step_propagates_uri_and_font_exceptions_from_viewer_draw() throws Exception {
        String model = "m";
        AudioManager audio = mock(AudioManager.class);
        Controller<String> controller = mock(Controller.class);
        Viewer<String> viewer = mock(Viewer.class);

        // make viewer.drawElement throw URISyntaxException wrapped in a RuntimeException (method doesn't declare checked exceptions)
        doThrow(new RuntimeException(new URISyntaxException("/x","err"))).when(viewer).drawElement(any(), any(), anyLong());

        State<String> state = new State<String>(model, audio) {
            @Override
            public Viewer<String> createViewer() throws IOException {
                return viewer;
            }

            @Override
            public Controller<String> createController(AudioManager audioManager) {
                return controller;
            }
        };

        GUI gui = mock(GUI.class);
        when(gui.getNextAction()).thenReturn(Collections.emptyList());
        Game game = mock(Game.class);

        assertThatThrownBy(() -> state.step(game, gui, 8L)).hasRootCauseInstanceOf(URISyntaxException.class);
    }

    @Test
    public void step_propagates_font_format_exception_wrapped() throws Exception {
        String model = "m";
        AudioManager audio = mock(AudioManager.class);
        Controller<String> controller = mock(Controller.class);
        Viewer<String> viewer = mock(Viewer.class);

        // make viewer.drawElement throw FontFormatException wrapped in RuntimeException
        doThrow(new RuntimeException(new FontFormatException("bad font"))).when(viewer).drawElement(any(), any(), anyLong());

        State<String> state = new State<String>(model, audio) {
            @Override
            public Viewer<String> createViewer() throws IOException {
                return viewer;
            }

            @Override
            public Controller<String> createController(AudioManager audioManager) {
                return controller;
            }
        };

        GUI gui = mock(GUI.class);
        when(gui.getNextAction()).thenReturn(Collections.emptyList());
        Game game = mock(Game.class);

        assertThatThrownBy(() -> state.step(game, gui, 9L)).hasRootCauseInstanceOf(FontFormatException.class);
    }
}
