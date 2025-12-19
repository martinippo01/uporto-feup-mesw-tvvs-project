package pt.feup.tvvs.pacman.controller.game.element;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.audio.AudioPlayer;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.ghost.Blinky;
import pt.feup.tvvs.pacman.model.game.element.ghost.Ghost;
import pt.feup.tvvs.pacman.model.game.element.ghost.GhostState;
import pt.feup.tvvs.pacman.model.game.element.collectibles.PowerUp;
import pt.feup.tvvs.pacman.model.game.element.collectibles.Collectible;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;
import pt.feup.tvvs.pacman.states.game.DyingState;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class CollisionControllerWhiteBoxTests {

    private AudioManager audioManager;
    private AudioPlayer ghostEatenAudio;
    private AudioPlayer collectibleEatenAudio;
    private AudioPlayer ghostsAliveSiren;
    private AudioPlayer ghostsScaredSiren;
    private Arena arena;

    @BeforeEach
    public void setup() {
        audioManager = mock(AudioManager.class);
        ghostEatenAudio = mock(AudioPlayer.class);
        collectibleEatenAudio = mock(AudioPlayer.class);
        ghostsAliveSiren = mock(AudioPlayer.class);
        ghostsScaredSiren = mock(AudioPlayer.class);

        when(audioManager.getAudio("ghostEaten")).thenReturn(ghostEatenAudio);
        when(audioManager.getAudio("collectibleEaten")).thenReturn(collectibleEatenAudio);
        when(audioManager.getAudio("ghostsAliveSiren")).thenReturn(ghostsAliveSiren);
        when(audioManager.getAudio("ghostsScaredSiren")).thenReturn(ghostsScaredSiren);

        // make isPlaying default to false
        when(ghostsAliveSiren.isPlaying()).thenReturn(false);
        when(ghostsScaredSiren.isPlaying()).thenReturn(false);

        arena = new Arena(20, 20);
    }

    // Helper to set private fields via reflection
    private void setPrivateField(Object target, String name, Object value) throws Exception {
        Field f = CollisionController.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private Object getPrivateField(Object target, String name) throws Exception {
        Field f = CollisionController.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }

    @Test
    public void testPacmanGhostCollision_setsDyingAnd_multiplayerCounter() throws Exception {
        Pacman pacman = new Pacman(new Position(1, 1));
        Pacman other = new Pacman(new Position(5, 5));
        arena.addPacman(pacman);
        arena.addPacman(other);

        Ghost blinky = new Blinky(new Position(1, 1));
        Set<Ghost> ghosts = new HashSet<>();
        ghosts.add(blinky);
        arena.setGhosts(ghosts);

        CollisionController controller = new CollisionController(arena, audioManager);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);

        controller.step(game, new ArrayList<>(), 0);

        // pacman should be marked dying and life decreased
        assertThat(pacman.isDying()).isTrue();
        assertThat(pacman.getLife()).isEqualTo(2);

        // since there is another alive pacman, no DyingState should be created; instead deadPacmanTimeCounter must be set
        Object counter = getPrivateField(controller, "deadPacmanTimeCounter");
        assertThat(counter).isEqualTo(110);

        // verify game.setState was not invoked with DyingState
        verify(game, never()).setState(any(DyingState.class));
    }

    @Test
    public void testPacmanGhostCollision_multiplayer_revivesAfterCounterExpires() throws Exception {
        Pacman p1 = new Pacman(new Position(2, 2));
        Pacman p2 = new Pacman(new Position(5, 5));
        p1.setRespawnPosition(new Position(0, 0));
        p1.setLife(2);
        p2.setRespawnPosition(new Position(10, 10));

        arena.addPacman(p1);
        arena.addPacman(p2);

        Ghost blinky = new Blinky(new Position(2, 2));
        Set<Ghost> ghosts = new HashSet<>();
        ghosts.add(blinky);
        arena.setGhosts(ghosts);

        CollisionController controller = new CollisionController(arena, audioManager);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);

        // first step: collision happens and deadPacmanTimeCounter should be set (multiplayer branch)
        controller.step(game, new ArrayList<>(), 0);

        assertThat(p1.isDying()).isTrue();
        // now set the counter to 1 via reflection so next step revives
        setPrivateField(controller, "deadPacmanTimeCounter", 1);

        // make p1 still have life >0 and be dying
        p1.setLife(1);
        p1.setDying(true);

        controller.step(game, new ArrayList<>(), 0);

        // p1 should be revived
        assertThat(p1.isDying()).isFalse();
        assertThat(p1.getPosition()).isEqualTo(p1.getRespawnPosition());
        assertThat(p1.getCounter()).isEqualTo(0);
    }

    @Test
    public void testGhostScaredCollision_eatenIncreasesScoreAndSetsDead() throws Exception {
        Pacman pacman = new Pacman(new Position(3, 3));
        arena.addPacman(pacman);

        Blinky blinky = new Blinky(new Position(3, 3));
        Set<Ghost> ghosts = new HashSet<>();
        ghosts.add(blinky);
        arena.setGhosts(ghosts);

        CollisionController controller = new CollisionController(arena, audioManager);

        // put ghost in scared state
        blinky.setState(GhostState.SCARED);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);

        long before = arena.getScore();
        controller.step(game, new ArrayList<>(), 0);

        // ghost should be dead and score increased by 200 (first ghost eaten)
        assertThat(blinky.isDead()).isTrue();
        assertThat(arena.getScore()).isEqualTo(before + 200);
        verify(ghostEatenAudio).playOnce();
    }

    @Test
    public void testCollectiblePowerUpCollision_triggersScaredAndScoreAndBlankPosition() throws Exception {
        Pacman pacman = new Pacman(new Position(4, 4));
        Pacman other = new Pacman(new Position(6, 6));
        arena.addPacman(pacman);
        arena.addPacman(other);

        // collectible at pacman's position
        PowerUp powerUp = new PowerUp(new Position(4, 4));
        Set<Collectible> collectibles = new HashSet<>();
        collectibles.add(powerUp);
        arena.setCollectibles(collectibles);

        // a ghost that is outside gate so invertDirection will be attempted
        Blinky blinky = new Blinky(new Position(1, 1));
        blinky.setOutsideGate();
        Set<Ghost> ghosts = new HashSet<>();
        ghosts.add(blinky);
        arena.setGhosts(ghosts);

        CollisionController controller = new CollisionController(arena, audioManager);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);

        long beforeScore = arena.getScore();
        int beforeCollected = arena.getCollectedCollectibles();

        controller.step(game, new ArrayList<>(), 0);

        // collectible removed and blank position added
        assertThat(arena.getBlankPositions()).contains(new Position(4, 4));
        assertThat(arena.getCollectedCollectibles()).isEqualTo(beforeCollected + 1);
        assertThat(arena.getScore()).isEqualTo(beforeScore + powerUp.getValue());

        // ghosts should be scared and pacmans boosted
        assertThat(blinky.isScared()).isTrue();
        for (Pacman p : arena.getPacmans()) {
            assertThat(p.getSpeed()).isEqualTo(Arena.PACMAN_BOOSTED_SPEED);
        }

        verify(collectibleEatenAudio).playOnce();
        verify(ghostsAliveSiren).stopPlaying();
        verify(ghostsScaredSiren).playInLoop();
    }

    @Test
    public void testStep_audioLoopSelection_whenNoSirenPlaying_playsAliveSiren() throws Exception {
        // Ensure audios report not playing
        when(ghostsAliveSiren.isPlaying()).thenReturn(false);
        when(ghostsScaredSiren.isPlaying()).thenReturn(false);

        // add a dummy ghost so ghostsEaten != getModel().getGhosts().size() (avoid second playInLoop invocation)
        Blinky blinky = new Blinky(new Position(0, 0));
        Set<Ghost> ghosts = new HashSet<>();
        ghosts.add(blinky);
        arena.setGhosts(ghosts);

        CollisionController controller = new CollisionController(arena, audioManager);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);

        // make sure scaredTimeLeft is zero
        setPrivateField(controller, "scaredTimeLeft", 0);

        controller.step(game, new ArrayList<>(), 0);

        verify(ghostsAliveSiren).playInLoop();
    }

    @Test
    public void testScaredTimeExpires_resetsGhostsAndStopsScaredSiren() throws Exception {
        Pacman pacman = new Pacman(new Position(7, 7));
        arena.addPacman(pacman);

        Blinky blinky = new Blinky(new Position(1, 1));
        Set<Ghost> ghosts = new HashSet<>();
        ghosts.add(blinky);
        arena.setGhosts(ghosts);

        CollisionController controller = new CollisionController(arena, audioManager);

        // set a scared ghost and force scaredTimeLeft to 1 so it will decrement to 0
        blinky.setState(GhostState.SCARED);
        setPrivateField(controller, "scaredTimeLeft", 1);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);

        controller.step(game, new ArrayList<>(), 0);

        // ghost should return to ALIVE and normal speed
        assertThat(blinky.getState()).isEqualTo(GhostState.ALIVE);
        assertThat(blinky.getSpeed()).isEqualTo(Arena.GHOST_NORMAL_SPEED);

        verify(ghostsScaredSiren).stopPlaying();
        verify(ghostsAliveSiren).playInLoop();
    }
}
