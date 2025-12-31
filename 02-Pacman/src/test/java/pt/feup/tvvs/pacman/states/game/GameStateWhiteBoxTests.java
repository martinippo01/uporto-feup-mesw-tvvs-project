package pt.feup.tvvs.pacman.states.game;

import org.junit.jupiter.api.Test;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.audio.AudioPlayer;
import pt.feup.tvvs.pacman.controller.Controller;
import pt.feup.tvvs.pacman.controller.game.ArenaController;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.viewer.Viewer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class GameStateWhiteBoxTests {

    @Test
    public void constructor_uses_overridden_create_methods_and_sets_fields() throws Exception {
        Arena arena = mock(Arena.class);
        AudioManager audio = mock(AudioManager.class);
        Viewer<Arena> viewer = mock(Viewer.class);
        Controller<Arena> controller = mock(Controller.class);

        GameState state = new GameState(arena, audio) {
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
    public void createViewer_override_returns_mock_viewer() throws Exception {
        Arena arena = mock(Arena.class);
        AudioManager audio = mock(AudioManager.class);
        Viewer<Arena> viewer = mock(Viewer.class);

        GameState state = new GameState(arena, audio) {
            @Override
            public Viewer<Arena> createViewer() {
                return viewer;
            }

            @Override
            public Controller<Arena> createController(AudioManager audioManager) {
                return mock(Controller.class);
            }
        };

        assertThat(state.createViewer()).isSameAs(viewer);
    }

    @Test
    public void createController_override_returns_mock_controller() throws Exception {
        Arena arena = mock(Arena.class);
        AudioManager audio = mock(AudioManager.class);
        Controller<Arena> controller = mock(Controller.class);

        GameState state = new GameState(arena, audio) {
            @Override
            public Viewer<Arena> createViewer() {
                return mock(Viewer.class);
            }

            @Override
            public Controller<Arena> createController(AudioManager audioManager) {
                return controller;
            }
        };

        assertThat(state.createController(audio)).isSameAs(controller);
    }

    @Test
    public void step_delegates_to_controller_and_viewer() throws Exception {
        Arena arena = mock(Arena.class);
        AudioManager audio = mock(AudioManager.class);
        Viewer<Arena> viewer = mock(Viewer.class);
        Controller<Arena> controller = mock(Controller.class);

        GameState state = new GameState(arena, audio) {
            @Override
            public Viewer<Arena> createViewer() {
                return viewer;
            }

            @Override
            public Controller<Arena> createController(AudioManager audioManager) {
                return controller;
            }
        };

        pt.feup.tvvs.pacman.Game game = mock(pt.feup.tvvs.pacman.Game.class);
        pt.feup.tvvs.pacman.gui.GUI gui = mock(pt.feup.tvvs.pacman.gui.GUI.class);
        when(gui.getNextAction()).thenReturn(java.util.Collections.emptyList());

        state.step(game, gui, 555L);

        verify(controller).step(eq(game), anyList(), eq(555L));
        verify(viewer).drawElement(eq(gui), eq(arena), eq(555L));
    }

    @Test
    public void createController_triggers_collision_controller_audio_initialization() throws Exception {
        Arena arena = mock(Arena.class);
        AudioManager audio = mock(AudioManager.class);
        AudioPlayer p1 = mock(AudioPlayer.class);
        AudioPlayer p2 = mock(AudioPlayer.class);
        AudioPlayer p3 = mock(AudioPlayer.class);
        AudioPlayer p4 = mock(AudioPlayer.class);

        when(audio.getAudio("ghostEaten")).thenReturn(p1);
        when(audio.getAudio("collectibleEaten")).thenReturn(p2);
        when(audio.getAudio("ghostsAliveSiren")).thenReturn(p3);
        when(audio.getAudio("ghostsScaredSiren")).thenReturn(p4);

        // construct state but override viewer to avoid heavy initialization
        GameState state = new GameState(arena, audio) {
            @Override
            public Viewer<Arena> createViewer() {
                return mock(Viewer.class);
            }
        };

        // The constructor should have created an ArenaController which created a CollisionController and invoked audio methods
        verify(audio).addAudio(eq("ghostEaten"), anyString());
        verify(audio).addAudio(eq("collectibleEaten"), anyString());
        verify(audio).addAudio(eq("ghostsAliveSiren"), anyString());
        verify(audio).addAudio(eq("ghostsScaredSiren"), anyString());

        verify(audio).getAudio("ghostEaten");
        verify(p1).setVolume(anyFloat());
        verify(audio).getAudio("collectibleEaten");
        verify(p2).setVolume(anyFloat());
        verify(audio).getAudio("ghostsAliveSiren");
        verify(p3).setVolume(anyFloat());
        verify(audio).getAudio("ghostsScaredSiren");
        verify(p4).setVolume(anyFloat());
    }

    @Test
    public void createController_returns_arenaController_instance() throws Exception {
        Arena arena = mock(Arena.class);
        AudioManager audio = mock(AudioManager.class);
        // provide AudioPlayer stubs so CollisionController constructor doesn't NPE
        AudioPlayer p1 = mock(AudioPlayer.class);
        AudioPlayer p2 = mock(AudioPlayer.class);
        AudioPlayer p3 = mock(AudioPlayer.class);
        AudioPlayer p4 = mock(AudioPlayer.class);
        when(audio.getAudio("ghostEaten")).thenReturn(p1);
        when(audio.getAudio("collectibleEaten")).thenReturn(p2);
        when(audio.getAudio("ghostsAliveSiren")).thenReturn(p3);
        when(audio.getAudio("ghostsScaredSiren")).thenReturn(p4);

        GameState state = new GameState(arena, audio) {
            @Override
            public Viewer<Arena> createViewer() {
                return mock(Viewer.class);
            }
        };

        Controller<Arena> controller = state.createController(audio);
        assertThat(controller).isInstanceOf(ArenaController.class);
    }

}
