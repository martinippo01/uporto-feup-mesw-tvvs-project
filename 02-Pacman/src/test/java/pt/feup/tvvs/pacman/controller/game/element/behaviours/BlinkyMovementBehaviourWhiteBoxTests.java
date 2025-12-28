package pt.feup.tvvs.pacman.controller.game.element.behaviours;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.ghost.Blinky;
import pt.feup.tvvs.pacman.model.game.element.ghost.Ghost;
import pt.feup.tvvs.pacman.model.game.element.ghost.GhostState;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class BlinkyMovementBehaviourWhiteBoxTests {

    @Test
    public void alive_insideGate_returnsGatePosition() {
        Arena arena = new Arena(20, 20);
        Blinky blinky = new Blinky(new Position(5, 5));
        // by default ghosts are inside the gate
        blinky.setInsideGate();

        BlinkyMovementBehaviour behaviour = new BlinkyMovementBehaviour();
        Pacman pacman = new Pacman(new Position(1, 1));

        Position target = behaviour.getTargetPosition(blinky, arena, pacman, true);

        assertThat(target).isEqualTo(arena.getGhostGate().getPosition());
    }

    @Test
    public void alive_outsideGate_chaseFalse_returnsRightTopEdge() {
        int width = 33;
        Arena arena = new Arena(width, 10);
        Blinky blinky = new Blinky(new Position(2, 2));
        blinky.setOutsideGate();

        BlinkyMovementBehaviour behaviour = new BlinkyMovementBehaviour();
        Pacman pacman = new Pacman(new Position(3, 3));

        Position target = behaviour.getTargetPosition(blinky, arena, pacman, false);

        assertThat(target).isEqualTo(new Position(arena.getWidth(), 0));
    }

    @Test
    public void alive_outsideGate_chaseTrue_returnsPacmanPosition() {
        Arena arena = new Arena(20, 20);
        Blinky blinky = new Blinky(new Position(4, 4));
        blinky.setOutsideGate();

        BlinkyMovementBehaviour behaviour = new BlinkyMovementBehaviour();
        Pacman pacman = new Pacman(new Position(7, 8));

        Position target = behaviour.getTargetPosition(blinky, arena, pacman, true);

        assertThat(target).isEqualTo(pacman.getPosition());
    }

    @Test
    public void dead_returnsGatePosition() {
        Arena arena = new Arena(15, 15);
        Blinky blinky = new Blinky(new Position(0, 0));
        blinky.setOutsideGate();
        blinky.setState(GhostState.DEAD);

        BlinkyMovementBehaviour behaviour = new BlinkyMovementBehaviour();
        Pacman pacman = new Pacman(new Position(2, 2));

        Position target = behaviour.getTargetPosition(blinky, arena, pacman, true);

        assertThat(target).isEqualTo(arena.getGhostGate().getPosition());
    }

    @Test
    public void scared_insideGate_delegatesToAlive() {
        Arena arena = new Arena(10, 10);
        Blinky blinky = new Blinky(new Position(1, 1));
        blinky.setInsideGate();
        blinky.setState(GhostState.SCARED);

        BlinkyMovementBehaviour behaviour = new BlinkyMovementBehaviour();
        Pacman pacman = new Pacman(new Position(9, 9));

        Position target = behaviour.getTargetPosition(blinky, arena, pacman, false);

        // when inside gate, getAlivePosition returns gate position
        assertThat(target).isEqualTo(arena.getGhostGate().getPosition());
    }

    @RepeatedTest(5)
    public void scared_outsideGate_returnsRandomPositionWithinBounds() {
        Arena arena = new Arena(29, 16); // width and height used as random bounds in behaviour
        Blinky blinky = new Blinky(new Position(1, 1));
        blinky.setOutsideGate();
        blinky.setState(GhostState.SCARED);

        BlinkyMovementBehaviour behaviour = new BlinkyMovementBehaviour();
        Pacman pacman = new Pacman(new Position(0, 0));

        Position target = behaviour.getTargetPosition(blinky, arena, pacman, true);

        // random x is generated as (int)(Math.random() * 29) so valid x range is [0,28]
        assertThat(target.getX()).isBetween(0, 28);
        // random y is generated as (int)(Math.random() * 16) so valid y range is [0,15]
        assertThat(target.getY()).isBetween(0, 15);
    }

    @Test
    public void dead_doesNotInvokeGetAlivePosition_onBehaviour() {
        Arena arena = new Arena(12, 12);
        Blinky blinky = new Blinky(new Position(1, 1));
        blinky.setOutsideGate();
        blinky.setState(GhostState.DEAD);

        BlinkyMovementBehaviour behaviour = spy(new BlinkyMovementBehaviour());
        Pacman pacman = new Pacman(new Position(2, 2));

        Position target = behaviour.getTargetPosition(blinky, arena, pacman, true);

        assertThat(target).isEqualTo(arena.getGhostGate().getPosition());
        // verify that the protected getAlivePosition method was never called when ghost is DEAD
        try {
            verify(behaviour, never()).getAlivePosition(any(Ghost.class), any(Arena.class), any(Pacman.class), anyBoolean());
        } catch (Exception e) {
            // in case of reflection/proxy visibility issues, fail the test explicitly
            throw new AssertionError("Verification of getAlivePosition invocation failed", e);
        }
    }
}
