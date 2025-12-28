package pt.feup.tvvs.pacman.controller.game.element.behaviours;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.ghost.Blinky;
import pt.feup.tvvs.pacman.model.game.element.ghost.Ghost;
import pt.feup.tvvs.pacman.model.game.element.ghost.GhostState;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class GhostMovementBehaviourWhiteBoxTests {

    // Small concrete implementation to test abstract behaviour
    static class TestGhostMovementBehaviour extends GhostMovementBehaviour {
        private final Position aliveReturn;

        TestGhostMovementBehaviour(Position aliveReturn) {
            this.aliveReturn = aliveReturn;
        }

        @Override
        protected Position getAlivePosition(Ghost ghost, Arena arena, Pacman targetPacman, boolean chaseMode) {
            return aliveReturn;
        }
    }

    @Test
    public void alive_state_delegatesToGetAlivePosition() {
        Arena arena = new Arena(10, 10);
        Blinky ghost = new Blinky(new Position(3, 3));
        ghost.setOutsideGate();
        ghost.setState(GhostState.ALIVE);

        TestGhostMovementBehaviour behaviour = spy(new TestGhostMovementBehaviour(new Position(5, 6)));
        Pacman pacman = new Pacman(new Position(1, 1));

        Position target = behaviour.getTargetPosition(ghost, arena, pacman, true);

        assertThat(target).isEqualTo(new Position(5, 6));
        // verify called once with same arguments
        try {
            ArgumentCaptor<Ghost> gCap = ArgumentCaptor.forClass(Ghost.class);
            ArgumentCaptor<Arena> aCap = ArgumentCaptor.forClass(Arena.class);
            ArgumentCaptor<Pacman> pCap = ArgumentCaptor.forClass(Pacman.class);
            verify(behaviour, times(1)).getAlivePosition(gCap.capture(), aCap.capture(), pCap.capture(), eq(true));

            assertThat(gCap.getValue()).isSameAs(ghost);
            assertThat(aCap.getValue()).isSameAs(arena);
            assertThat(pCap.getValue()).isSameAs(pacman);
        } catch (Exception e) {
            throw new AssertionError("Verification failed", e);
        }
    }

    @Test
    public void dead_state_returnsGhostGatePosition() {
        Arena arena = new Arena(12, 8);
        Blinky ghost = new Blinky(new Position(2, 2));
        ghost.setOutsideGate();
        ghost.setState(GhostState.DEAD);

        TestGhostMovementBehaviour behaviour = new TestGhostMovementBehaviour(new Position(9, 9));
        Pacman pacman = new Pacman(new Position(4, 4));

        Position target = behaviour.getTargetPosition(ghost, arena, pacman, false);

        assertThat(target).isEqualTo(arena.getGhostGate().getPosition());
    }

    @Test
    public void scared_inside_delegatesToAlive() {
        Arena arena = new Arena(10, 10);
        Blinky ghost = new Blinky(new Position(2, 2));
        ghost.setInsideGate();
        ghost.setState(GhostState.SCARED);

        TestGhostMovementBehaviour behaviour = spy(new TestGhostMovementBehaviour(new Position(7, 8)));
        Pacman pacman = new Pacman(new Position(0, 0));

        Position target = behaviour.getTargetPosition(ghost, arena, pacman, false);

        assertThat(target).isEqualTo(new Position(7, 8));
        try {
            verify(behaviour, times(1)).getAlivePosition(any(), any(), any(), anyBoolean());
        } catch (Exception e) {
            throw new AssertionError("Verification failed", e);
        }
    }

    @RepeatedTest(5)
    public void scared_outside_returnsRandomWithinBounds() {
        Arena arena = new Arena(29, 16);
        Blinky ghost = new Blinky(new Position(1, 1));
        ghost.setOutsideGate();
        ghost.setState(GhostState.SCARED);

        TestGhostMovementBehaviour behaviour = new TestGhostMovementBehaviour(new Position(0, 0));
        Pacman pacman = new Pacman(new Position(0, 0));

        Position target = behaviour.getTargetPosition(ghost, arena, pacman, true);

        assertThat(target).isNotNull();
        assertThat(target.getX()).isBetween(0, 28);
        assertThat(target.getY()).isBetween(0, 15);
    }

    @Test
    public void dead_doesNot_callGetAlivePosition_whenDead() {
        Arena arena = new Arena(8, 8);
        Blinky ghost = new Blinky(new Position(0, 0));
        ghost.setOutsideGate();
        ghost.setState(GhostState.DEAD);

        TestGhostMovementBehaviour behaviour = spy(new TestGhostMovementBehaviour(new Position(2, 3)));
        Pacman pacman = new Pacman(new Position(1, 1));

        Position target = behaviour.getTargetPosition(ghost, arena, pacman, true);

        assertThat(target).isEqualTo(arena.getGhostGate().getPosition());
        try {
            verify(behaviour, never()).getAlivePosition(any(), any(), any(), anyBoolean());
        } catch (Exception e) {
            throw new AssertionError("Verification failed", e);
        }
    }
}


