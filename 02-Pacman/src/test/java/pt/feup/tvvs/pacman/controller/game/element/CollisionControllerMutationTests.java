package pt.feup.tvvs.pacman.controller.game.element;

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
import pt.feup.tvvs.pacman.model.game.element.collectibles.PowerUp;
import pt.feup.tvvs.pacman.model.game.element.collectibles.Collectible;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class CollisionControllerMutationTests {

    // helper to set private fields on CollisionController
    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = CollisionController.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    public void alive_collision_singlePlayer_invokes_pacman_setters_and_triggers_dying_state() throws Exception {
        Arena arena = new Arena(10, 10);

        Pacman mockPac = mock(Pacman.class);
        AtomicBoolean dying = new AtomicBoolean(false);
        when(mockPac.isDying()).thenAnswer(inv -> dying.get());
        doAnswer(inv -> { dying.set((Boolean) inv.getArgument(0)); return null; }).when(mockPac).setDying(anyBoolean());
        when(mockPac.getRespawnPosition()).thenReturn(new Position(0,0));
        arena.addPacman(mockPac);

        Ghost mockGhost = mock(Ghost.class);
        when(mockGhost.collidingWith(mockPac)).thenReturn(true);
        when(mockGhost.getState()).thenReturn(GhostState.ALIVE);
        Set<Ghost> ghosts = new HashSet<>(); ghosts.add(mockGhost); arena.setGhosts(ghosts);

        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer ghostEaten = mock(AudioPlayer.class);
        AudioPlayer collectible = mock(AudioPlayer.class);
        AudioPlayer aliveSiren = mock(AudioPlayer.class);
        AudioPlayer scaredSiren = mock(AudioPlayer.class);
        when(audioManager.getAudio(anyString())).thenReturn(ghostEaten, collectible, aliveSiren, scaredSiren);
        doNothing().when(audioManager).addAudio(anyString(), anyString());

        CollisionController controller = new CollisionController(arena, audioManager);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);

        controller.step(game, List.of(GUI.ACTION.NONE), 0L);

        // verify pacman was affected
        verify(mockPac, atLeastOnce()).decreaseLife();
        verify(mockPac, atLeastOnce()).setDying(true);
        verify(mockPac, atLeastOnce()).setSpeed(Arena.PACMAN_NORMAL_SPEED);

        // since single pacman, game.setState should have been called (transition to DyingState)
        verify(game, times(1)).setState(any());
        verify(game.getAudioManager(), times(1)).stopAllAudios();
    }

    @Test
    public void scared_collision_invokes_ghost_setters_and_increments_score() throws Exception {
        Arena arena = new Arena(10,10);

        Pacman pac = new Pacman(new Position(1,1)); arena.addPacman(pac);

        Ghost mockGhost = mock(Ghost.class);
        when(mockGhost.collidingWith(any(Pacman.class))).thenReturn(true);
        when(mockGhost.getState()).thenReturn(GhostState.SCARED);
        when(mockGhost.isDead()).thenReturn(false);
        Set<Ghost> ghosts = new HashSet<>(); ghosts.add(mockGhost); arena.setGhosts(ghosts);

        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer ghostEaten = mock(AudioPlayer.class);
        AudioPlayer collectible = mock(AudioPlayer.class);
        AudioPlayer aliveSiren = mock(AudioPlayer.class);
        AudioPlayer scaredSiren = mock(AudioPlayer.class);
        when(audioManager.getAudio(anyString())).thenReturn(ghostEaten, collectible, aliveSiren, scaredSiren);
        doNothing().when(audioManager).addAudio(anyString(), anyString());

        CollisionController controller = new CollisionController(arena, audioManager);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);

        long before = arena.getScore();
        controller.step(game, List.of(GUI.ACTION.NONE), 0L);

        verify(ghostEaten).playOnce();
        verify(mockGhost, atLeastOnce()).setState(GhostState.DEAD);
        verify(mockGhost, atLeastOnce()).setSpeed(Arena.GHOST_DEAD_SPEED);
        assertThat(arena.getScore()).isEqualTo(before + 200);
    }

    @Test
    public void powerup_collision_removes_collectible_and_sets_ghosts_scared_and_inverts_direction() throws Exception {
        Arena arena = new Arena(10,10);

        Pacman pac = new Pacman(new Position(4,4)); arena.addPacman(pac);

        PowerUp powerUp = new PowerUp(new Position(4,4));
        Set<Collectible> collectibles = new HashSet<>(); collectibles.add(powerUp); arena.setCollectibles(collectibles);

        Ghost mockGhost = mock(Ghost.class);
        when(mockGhost.isDead()).thenReturn(false);
        when(mockGhost.getPosition()).thenReturn(new Position(0,0));
        // ghostGate default at (10,10) so positions will not be equal -> invertDirection expected
        when(mockGhost.getState()).thenReturn(GhostState.ALIVE);
        Set<Ghost> ghosts = new HashSet<>(); ghosts.add(mockGhost); arena.setGhosts(ghosts);

        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer ghostEaten = mock(AudioPlayer.class);
        AudioPlayer collectible = mock(AudioPlayer.class);
        AudioPlayer aliveSiren = mock(AudioPlayer.class);
        AudioPlayer scaredSiren = mock(AudioPlayer.class);
        when(audioManager.getAudio(anyString())).thenReturn(ghostEaten, collectible, aliveSiren, scaredSiren);
        doNothing().when(audioManager).addAudio(anyString(), anyString());

        CollisionController controller = new CollisionController(arena, audioManager);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);

        controller.step(game, List.of(GUI.ACTION.NONE), 0L);

        // collectible should be removed
        assertThat(arena.getCollectibles()).doesNotContain(powerUp);
        assertThat(arena.getBlankPositions()).contains(new Position(4,4));
        assertThat(arena.getCollectedCollectibles()).isGreaterThan(0);

        // verify ghosts were set to scared and speed adjusted and invertDirection attempted
        verify(mockGhost, atLeastOnce()).setState(GhostState.SCARED);
        verify(mockGhost, atLeastOnce()).setSpeed(Arena.GHOST_SCARED_SPEED);
        verify(mockGhost, atLeastOnce()).invertDirection();
    }

    @Test
    public void scared_time_expires_resets_ghosts_and_pacman_speed() throws Exception {
        Arena arena = new Arena(10,10);

        Pacman mockPac = mock(Pacman.class);
        when(mockPac.isDying()).thenReturn(false);
        arena.addPacman(mockPac);

        Ghost mockGhost = mock(Ghost.class);
        when(mockGhost.isScared()).thenReturn(true);
        Set<Ghost> ghosts = new HashSet<>(); ghosts.add(mockGhost); arena.setGhosts(ghosts);

        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer ghostEaten = mock(AudioPlayer.class);
        AudioPlayer collectible = mock(AudioPlayer.class);
        AudioPlayer aliveSiren = mock(AudioPlayer.class);
        AudioPlayer scaredSiren = mock(AudioPlayer.class);
        when(audioManager.getAudio(anyString())).thenReturn(ghostEaten, collectible, aliveSiren, scaredSiren);
        doNothing().when(audioManager).addAudio(anyString(), anyString());

        CollisionController controller = new CollisionController(arena, audioManager);
        // set scaredTimeLeft to 1 so it decrements to 0 and triggers the reset branch
        setPrivateField(controller, "scaredTimeLeft", 1);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);

        controller.step(game, List.of(GUI.ACTION.NONE), 0L);

        verify(mockGhost, atLeastOnce()).setState(GhostState.ALIVE);
        verify(mockGhost, atLeastOnce()).setSpeed(Arena.GHOST_NORMAL_SPEED);
        verify(mockPac, atLeastOnce()).setSpeed(Arena.PACMAN_NORMAL_SPEED);
        verify(scaredSiren, atLeastOnce()).stopPlaying();
        verify(aliveSiren, atLeastOnce()).playInLoop();
    }
}

