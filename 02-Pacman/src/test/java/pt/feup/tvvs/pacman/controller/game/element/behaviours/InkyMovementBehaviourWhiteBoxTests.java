package pt.feup.tvvs.pacman.controller.game.element.behaviours;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.ghost.Blinky;
import pt.feup.tvvs.pacman.model.game.element.ghost.Ghost;
import pt.feup.tvvs.pacman.model.game.element.ghost.GhostState;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class InkyMovementBehaviourWhiteBoxTests {

    @Test
    public void collectedLessThan25_returnsEightEleven() {
        Arena arena = new Arena(20, 20);
        InkyMovementBehaviour behaviour = new InkyMovementBehaviour();

        Blinky ghost = new Blinky(new Position(2, 2));
        // default collected is 0
        Position target = behaviour.getTargetPosition(ghost, arena, new Pacman(new Position(0,0)), true);

        assertThat(target).isEqualTo(new Position(8, 11));
    }

    @Test
    public void collectedAtLeast25_and_insideGate_returnsGhostGatePosition() {
        Arena arena = new Arena(30, 30);
        // increment to reach 25
        for (int i = 0; i < 25; i++) arena.incrementCollectedCollectibles();

        InkyMovementBehaviour behaviour = new InkyMovementBehaviour();
        Blinky ghost = new Blinky(new Position(5,5));
        ghost.setInsideGate();

        Position target = behaviour.getTargetPosition(ghost, arena, new Pacman(new Position(0,0)), true);

        assertThat(target).isEqualTo(arena.getGhostGate().getPosition());
    }

    @Test
    public void collectedAtLeast25_and_notChase_returnsArenaCorner() {
        Arena arena = new Arena(12, 7);
        for (int i = 0; i < 25; i++) arena.incrementCollectedCollectibles();

        InkyMovementBehaviour behaviour = new InkyMovementBehaviour();
        Blinky ghost = new Blinky(new Position(3,3));
        ghost.setOutsideGate();

        Position target = behaviour.getTargetPosition(ghost, arena, new Pacman(new Position(0,0)), false);

        assertThat(target).isEqualTo(new Position(arena.getWidth(), arena.getHeight()));
    }

    @Test
    public void chaseMode_computesTarget_usingBlinkyAndPacmanNext_withinBounds() {
        Arena arena = new Arena(20, 20);
        for (int i = 0; i < 25; i++) arena.incrementCollectedCollectibles();

        InkyMovementBehaviour behaviour = new InkyMovementBehaviour();

        // add a Blinky at (2,2) to arena so the algorithm can find it
        Blinky blinky = new Blinky(new Position(2,2));
        blinky.setOutsideGate();
        arena.addGhost(blinky);

        // ghost parameter (inky) is irrelevant for computation other than gate/collected checks
        Blinky ghost = new Blinky(new Position(3,3));
        ghost.setOutsideGate();

        // mock pacman to return a chosen next position (5,4)
        Pacman pacman = mock(Pacman.class);
        when(pacman.getNextPosition()).thenReturn(new Position(5,4));

        Position target = behaviour.getTargetPosition(ghost, arena, pacman, true);

        // expected newX = 2*5 - 2 = 8, newY = 2*4 - 2 = 6
        assertThat(target).isEqualTo(new Position(8,6));
    }

    @Test
    public void chaseMode_computesTarget_and_clampsToZeroWhenNegative() {
        Arena arena = new Arena(10, 10);
        for (int i = 0; i < 25; i++) arena.incrementCollectedCollectibles();

        InkyMovementBehaviour behaviour = new InkyMovementBehaviour();

        // Blinky at (5,5)
        Blinky blinky = new Blinky(new Position(5,5));
        blinky.setOutsideGate();
        arena.addGhost(blinky);

        Blinky ghost = new Blinky(new Position(1,1));
        ghost.setOutsideGate();

        Pacman pacman = mock(Pacman.class);
        // pacman next is (0,0) -> newX = 2*0 - 5 = -5 -> clamp to 0
        when(pacman.getNextPosition()).thenReturn(new Position(0,0));

        Position target = behaviour.getTargetPosition(ghost, arena, pacman, true);

        assertThat(target).isEqualTo(new Position(0,0));
    }

    @Test
    public void chaseMode_computesTarget_and_clampsToArenaMaxWhenTooLarge() {
        Arena arena = new Arena(7, 6);
        for (int i = 0; i < 25; i++) arena.incrementCollectedCollectibles();

        InkyMovementBehaviour behaviour = new InkyMovementBehaviour();

        // Blinky near origin
        Blinky blinky = new Blinky(new Position(0,0));
        blinky.setOutsideGate();
        arena.addGhost(blinky);

        Blinky ghost = new Blinky(new Position(1,1));
        ghost.setOutsideGate();

        Pacman pacman = mock(Pacman.class);
        // pacman next pos (10,10) -> newX = 20 - 0 = 20 -> clamp to arena.getWidth() which is 7
        when(pacman.getNextPosition()).thenReturn(new Position(10,10));

        Position target = behaviour.getTargetPosition(ghost, arena, pacman, true);

        assertThat(target).isEqualTo(new Position(arena.getWidth(), arena.getHeight()));
    }

}
