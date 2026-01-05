package pt.feup.tvvs.pacman.controller.game;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.audio.AudioPlayer;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.ghost.Ghost;
import pt.feup.tvvs.pacman.model.game.element.ghost.GhostState;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;
import pt.feup.tvvs.pacman.states.State;
import pt.feup.tvvs.pacman.states.game.GameState;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DyingStateControllerMutationTests {

    @Test
    void pacman_setters_are_called_on_step() throws Exception {
        Arena arena = new Arena(5, 5);

        // create a mock pacman to verify interactions without triggering Element.setPosition
        Pacman mockPac = mock(Pacman.class);
        when(mockPac.getLife()).thenReturn(1);
        when(mockPac.getRespawnPosition()).thenReturn(new Position(3, 3));
        // ensure setters are verifiable (no-op on mock)
        arena.addPacman(mockPac);

        // audio manager mock to satisfy constructor
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer audioPlayer = mock(AudioPlayer.class);
        doNothing().when(audioManager).addAudio(anyString(), anyString());
        when(audioManager.getAudio(anyString())).thenReturn(audioPlayer);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);

        DyingStateController controller = new DyingStateController(arena, audioManager);

        // set private counter to 1 so step() will decrement to 0
        setPrivateField(controller, "stateTimeCounter", 1);

        try (var mocked = mockConstruction(GameState.class)) {
            controller.step(game, List.of(GUI.ACTION.NONE), 0L);
        }

        // verify pacman methods were invoked to reset state
        verify(mockPac, atLeastOnce()).setSpeed(Arena.PACMAN_NORMAL_SPEED);
        verify(mockPac, atLeastOnce()).setPosition(any(Position.class));
        verify(mockPac, atLeastOnce()).setCounter(0);
        verify(mockPac, atLeastOnce()).setDying(false);
    }

    @Test
    void ghost_setters_are_called_on_step() throws Exception {
        Arena arena = new Arena(5, 5);

        // create a mock ghost to verify interactions without invoking Element.setPosition
        Ghost mockGhost = mock(Ghost.class);
        when(mockGhost.getRespawnPosition()).thenReturn(new Position(5, 5));
        when(mockGhost.getState()).thenReturn(GhostState.DEAD);
        arena.addGhost(mockGhost);

        // add a pacman with life > 0 so controller follows the "there is still at least one pacman" branch
        Pacman p = new Pacman(new Position(1,1));
        p.setLife(1);
        p.setRespawnPosition(new Position(3,3));
        arena.addPacman(p);

        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer audioPlayer = mock(AudioPlayer.class);
        doNothing().when(audioManager).addAudio(anyString(), anyString());
        when(audioManager.getAudio(anyString())).thenReturn(audioPlayer);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);

        DyingStateController controller = new DyingStateController(arena, audioManager);
        setPrivateField(controller, "stateTimeCounter", 1);

        try (var mocked = mockConstruction(GameState.class)) {
            controller.step(game, List.of(GUI.ACTION.NONE), 0L);
        }

        // verify ghost methods were invoked to reset state
        verify(mockGhost, atLeastOnce()).setState(GhostState.ALIVE);
        verify(mockGhost, atLeastOnce()).setSpeed(Arena.GHOST_NORMAL_SPEED);
        verify(mockGhost, atLeastOnce()).setPosition(any(Position.class));
        verify(mockGhost, atLeastOnce()).setCounter(0);
        verify(mockGhost, atLeastOnce()).setInsideGate();
    }

    // reflection helper
    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
