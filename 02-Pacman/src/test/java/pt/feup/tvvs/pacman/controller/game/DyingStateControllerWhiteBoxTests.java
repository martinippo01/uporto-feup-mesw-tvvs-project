package pt.feup.tvvs.pacman.controller.game;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.audio.AudioPlayer;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.collectibles.Coin;
import pt.feup.tvvs.pacman.model.game.element.ghost.GhostState;
import pt.feup.tvvs.pacman.model.game.element.ghost.Ghost;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;
import pt.feup.tvvs.pacman.states.State;
import pt.feup.tvvs.pacman.states.game.GameState;
import pt.feup.tvvs.pacman.states.menu.AlertMenuState;
import pt.feup.tvvs.pacman.model.menu.AlertMenu;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DyingStateControllerWhiteBoxTests {

    @Test
    public void whenAllPacmansDead_shouldStopAudiosAndSetAlertState() throws Exception {
        // Arrange
        Arena arena = new Arena(5, 5);

        // create a pacman with no lives
        Pacman p = new Pacman(new Position(1, 1));
        p.setLife(0);
        p.setRespawnPosition(new Position(2, 2));
        arena.addPacman(p);

        // audio manager mock
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer audioPlayer = mock(AudioPlayer.class);
        doNothing().when(audioManager).addAudio(anyString(), anyString());
        when(audioManager.getAudio(anyString())).thenReturn(audioPlayer);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);

        DyingStateController controller = new DyingStateController(arena, audioManager);

        // fast-forward the stateTimeCounter to 1 then call step to decrement to 0
        // we use reflection to set the private counter to 1
        setPrivateField(controller, "stateTimeCounter", 1);

        // Mock construction of AlertMenuState so heavy initialization is avoided
        try (var mocked = mockConstruction(AlertMenuState.class)) {
            // Act
            controller.step(game, List.of(GUI.ACTION.NONE), 0L);

            // Assert
            verify(audioManager, times(1)).stopAllAudios();
            ArgumentCaptor<State> captor = ArgumentCaptor.forClass(State.class);
            verify(game, times(1)).setState(captor.capture());
            State<?> newState = captor.getValue();
            assertThat(newState).isInstanceOf(AlertMenuState.class);
        }
    }

    @Test
    public void whenThereAreAlivePacmans_shouldResetPacmansAndGhostsAndSetGameState() throws Exception {
        // Arrange
        Arena arena = new Arena(7, 7);

        // create a pacman with life > 0
        Pacman p = new Pacman(new Position(1, 1));
        p.setLife(1);
        p.setRespawnPosition(new Position(3, 3));
        p.setDying(true);
        p.setPosition(new Position(4,4));
        arena.addPacman(p);

        // create a ghost and set custom state so controller will reset it
        Ghost ghost = new Ghost(new Position(2, 2)) {
        };
        ghost.setState(GhostState.DEAD);
        ghost.setRespawnPosition(new Position(5,5));
        arena.addGhost(ghost);

        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer audioPlayer = mock(AudioPlayer.class);
        doNothing().when(audioManager).addAudio(anyString(), anyString());
        when(audioManager.getAudio(anyString())).thenReturn(audioPlayer);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);

        DyingStateController controller = new DyingStateController(arena, audioManager);
        setPrivateField(controller, "stateTimeCounter", 1);

        // Mock construction of GameState to avoid heavy init
        try (var mocked = mockConstruction(GameState.class)) {
            // Act
            controller.step(game, List.of(GUI.ACTION.NONE), 0L);

            // Assert: pacman was reset
            assertThat(p.getSpeed()).isEqualTo(Arena.PACMAN_NORMAL_SPEED);
            assertThat(p.getPosition()).isEqualTo(p.getRespawnPosition());
            assertThat(p.getCounter()).isEqualTo(0);
            assertThat(p.isDying()).isFalse();

            // Assert: ghost was reset
            assertThat(ghost.getState()).isEqualTo(GhostState.ALIVE);
            assertThat(ghost.getSpeed()).isEqualTo(Arena.GHOST_NORMAL_SPEED);
            assertThat(ghost.getPosition()).isEqualTo(ghost.getRespawnPosition());
            assertThat(ghost.getCounter()).isEqualTo(0);
            assertThat(ghost.isInsideGate()).isTrue();

            // Assert: game state set to GameState
            ArgumentCaptor<State> captor = ArgumentCaptor.forClass(State.class);
            verify(game, times(1)).setState(captor.capture());
            State<?> newState = captor.getValue();
            assertThat(newState).isInstanceOf(GameState.class);
        }
    }

    // reflection helper
    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}

