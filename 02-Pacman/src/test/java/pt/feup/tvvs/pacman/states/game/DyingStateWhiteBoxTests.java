package pt.feup.tvvs.pacman.states.game;

import org.junit.jupiter.api.Test;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.audio.AudioPlayer;
import pt.feup.tvvs.pacman.controller.Controller;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.viewer.Viewer;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DyingStateWhiteBoxTests {

    // Subclass to override heavy viewer/controller creation
    private static class TestDyingState extends DyingState {
        private final Viewer<Arena> viewerOverride;
        private final Controller<Arena> controllerOverride;

        protected TestDyingState(Arena model, AudioManager audioManager, Viewer<Arena> viewerOverride, Controller<Arena> controllerOverride) throws IOException, URISyntaxException {
            super(model, audioManager);
            this.viewerOverride = viewerOverride;
            this.controllerOverride = controllerOverride;
        }

        @Override
        public Viewer<Arena> createViewer() throws IOException {
            if (viewerOverride != null) return viewerOverride;
            return super.createViewer();
        }

        @Override
        public Controller<Arena> createController(AudioManager audioManager) {
            if (controllerOverride != null) return controllerOverride;
            return super.createController(audioManager);
        }
    }

    @Test
    public void constructor_uses_overridden_create_methods_and_sets_fields() throws Exception {
        Arena arena = mock(Arena.class);
        AudioManager audio = mock(AudioManager.class);
        Viewer<Arena> viewer = mock(Viewer.class);
        Controller<Arena> controller = mock(Controller.class);

        /*
            We need to avoid the superclass calling the real createViewer/createController during construction.
            The TestDyingState above delegates to overrides only if not null, but super() is called first and will call
            our overridden methods only if they exist. To ensure safe behavior we pass nulls to the constructor
            and then set expectations by constructing a subclass that returns mocks via overriding methods.
        */

        // Instead of the previous complex approach, create an anonymous subclass that overrides both methods.
        DyingState state = new DyingState(arena, audio) {
            @Override
            public Viewer<Arena> createViewer() {
                return viewer;
            }

            @Override
            public Controller<Arena> createController(AudioManager audioManager) {
                return controller;
            }
        };

        assertThat(state.getModel()).isSameAs(arena);
        assertThat(state.getAudioManager()).isSameAs(audio);
    }

    @Test
    public void creating_state_triggers_dying_controller_audio_initialization() throws Exception {
        Arena arena = mock(Arena.class);
        AudioManager audio = mock(AudioManager.class);
        AudioPlayer player = mock(AudioPlayer.class);

        when(audio.getAudio("deathAudio")).thenReturn(player);

        Viewer<Arena> viewer = mock(Viewer.class);

        // instantiate subclass that overrides createViewer to avoid heavy Viewer creation, but uses real createController
        DyingState state = new DyingState(arena, audio) {
            @Override
            public Viewer<Arena> createViewer() {
                return viewer;
            }
        };

        // During construction, DyingStateController constructor should have been invoked and interacted with audio manager
        verify(audio).addAudio(eq("deathAudio"), anyString());
        verify(audio).getAudio("deathAudio");
        verify(player).setVolume(anyFloat());
        verify(player).playOnce();

        // Also ensure model and audio manager stored
        assertThat(state.getModel()).isSameAs(arena);
        assertThat(state.getAudioManager()).isSameAs(audio);
    }

    @Test
    public void createViewer_override_returns_mock_viewer() throws Exception {
        Arena arena = mock(Arena.class);
        AudioManager audio = mock(AudioManager.class);
        Viewer<Arena> viewer = mock(Viewer.class);

        // override createViewer to return our mock and override controller to avoid audio interactions
        DyingState state = new DyingState(arena, audio) {
            @Override
            public Viewer<Arena> createViewer() {
                return viewer;
            }

            @Override
            public Controller<Arena> createController(AudioManager audioManager) {
                return mock(Controller.class);
            }
        };

        Viewer<Arena> v = state.createViewer();
        assertThat(v).isSameAs(viewer);
    }


}
