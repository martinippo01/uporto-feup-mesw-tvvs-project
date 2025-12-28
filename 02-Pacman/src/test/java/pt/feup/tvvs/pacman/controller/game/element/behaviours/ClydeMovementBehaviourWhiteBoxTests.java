package pt.feup.tvvs.pacman.controller.game.element.behaviours;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.ghost.Blinky;
import pt.feup.tvvs.pacman.model.game.element.ghost.GhostState;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ClydeMovementBehaviourWhiteBoxTests {

    @Test
    public void collectedLessThan60_returnsTenEleven() {
        Arena arena = new Arena(20, 20);
        Blinky ghost = new Blinky(new Position(2, 2));
        ghost.setOutsideGate();

        ClydeMovementBehaviour behaviour = new ClydeMovementBehaviour();
        Pacman pacman = new Pacman(new Position(0, 0));

        Position target = behaviour.getTargetPosition(ghost, arena, pacman, true);

        assertThat(target).isEqualTo(new Position(10, 11));
    }

    @Test
    public void collectedAtLeast60_insideGate_returnsGatePosition() {
        Arena arena = new Arena(30, 30);
        // bump collected collectibles to 60
        for (int i = 0; i < 60; i++) arena.incrementCollectedCollectibles();

        Blinky ghost = new Blinky(new Position(5, 5));
        ghost.setInsideGate();

        ClydeMovementBehaviour behaviour = new ClydeMovementBehaviour();
        Pacman pacman = new Pacman(new Position(1, 1));

        Position target = behaviour.getTargetPosition(ghost, arena, pacman, true);

        assertThat(target).isEqualTo(arena.getGhostGate().getPosition());
    }

    @Test
    public void collectedAtLeast60_outsideGate_chaseFalse_returnsCorner() {
        Arena arena = new Arena(12, 7);
        for (int i = 0; i < 60; i++) arena.incrementCollectedCollectibles();

        Blinky ghost = new Blinky(new Position(3, 3));
        ghost.setOutsideGate();

        ClydeMovementBehaviour behaviour = new ClydeMovementBehaviour();
        Pacman pacman = new Pacman(new Position(4, 4));

        Position target = behaviour.getTargetPosition(ghost, arena, pacman, false);

        assertThat(target).isEqualTo(new Position(0, arena.getHeight()));
    }

    @Test
    public void collectedAtLeast60_chaseTrue_distanceGe36_returnsPacman() {
        Arena arena = new Arena(20, 20);
        for (int i = 0; i < 60; i++) arena.incrementCollectedCollectibles();

        Blinky ghost = new Blinky(new Position(0, 0));
        ghost.setOutsideGate();

        Pacman pacman = new Pacman(new Position(6, 0)); // squaredDistance = 36
        ClydeMovementBehaviour behaviour = new ClydeMovementBehaviour();

        Position target = behaviour.getTargetPosition(ghost, arena, pacman, true);

        assertThat(target).isEqualTo(pacman.getPosition());
    }

    @Test
    public void collectedAtLeast60_chaseTrue_distanceLt36_returnsCorner() {
        Arena arena = new Arena(20, 20);
        for (int i = 0; i < 60; i++) arena.incrementCollectedCollectibles();

        Blinky ghost = new Blinky(new Position(0, 0));
        ghost.setOutsideGate();

        Pacman pacman = new Pacman(new Position(3, 0)); // squaredDistance = 9
        ClydeMovementBehaviour behaviour = new ClydeMovementBehaviour();

        Position target = behaviour.getTargetPosition(ghost, arena, pacman, true);

        assertThat(target).isEqualTo(new Position(0, arena.getHeight()));
    }

    @Test
    public void dead_returnsGatePosition() {
        Arena arena = new Arena(10, 10);
        Blinky ghost = new Blinky(new Position(1, 1));
        ghost.setOutsideGate();
        ghost.setState(GhostState.DEAD);

        ClydeMovementBehaviour behaviour = new ClydeMovementBehaviour();
        Pacman pacman = new Pacman(new Position(2, 2));

        Position target = behaviour.getTargetPosition(ghost, arena, pacman, true);

        assertThat(target).isEqualTo(arena.getGhostGate().getPosition());
    }

    @Test
    public void scared_inside_delegatesToAlive() {
        Arena arena = new Arena(10, 10);
        Blinky ghost = new Blinky(new Position(2, 2));
        ghost.setInsideGate();
        ghost.setState(GhostState.SCARED);

        ClydeMovementBehaviour behaviour = new ClydeMovementBehaviour();
        Pacman pacman = new Pacman(new Position(0, 0));

        // collected < 60 by default so alive logic returns (10,11)
        Position target = behaviour.getTargetPosition(ghost, arena, pacman, false);

        assertThat(target).isEqualTo(new Position(10, 11));
    }

    @RepeatedTest(5)
    public void scared_outside_returnsRandomWithinBounds() {
        Arena arena = new Arena(29, 16);
        Blinky ghost = new Blinky(new Position(2, 2));
        ghost.setOutsideGate();
        ghost.setState(GhostState.SCARED);

        ClydeMovementBehaviour behaviour = new ClydeMovementBehaviour();
        Pacman pacman = new Pacman(new Position(0, 0));

        Position target = behaviour.getTargetPosition(ghost, arena, pacman, true);

        assertThat(target.getX()).isBetween(0, 28);
        assertThat(target.getY()).isBetween(0, 15);
    }

    @Test
    public void dead_doesNotInvokeGetAlivePosition_onBehaviour() {
        Arena arena = new Arena(12, 12);
        Blinky ghost = new Blinky(new Position(1, 1));
        ghost.setOutsideGate();
        ghost.setState(GhostState.DEAD);

        ClydeMovementBehaviour behaviour = spy(new ClydeMovementBehaviour());
        Pacman pacman = new Pacman(new Position(2, 2));

        Position target = behaviour.getTargetPosition(ghost, arena, pacman, true);

        assertThat(target).isEqualTo(arena.getGhostGate().getPosition());
        try {
            verify(behaviour, never()).getAlivePosition(any(), any(), any(), anyBoolean());
        } catch (Exception e) {
            throw new AssertionError("Verification failed", e);
        }
    }

    @Test
    public void scared_inside_invokesGetAlivePosition_onBehaviour() {
        Arena arena = new Arena(12, 12);
        Blinky ghost = new Blinky(new Position(1, 1));
        ghost.setInsideGate();
        ghost.setState(GhostState.SCARED);

        ClydeMovementBehaviour behaviour = spy(new ClydeMovementBehaviour());
        Pacman pacman = new Pacman(new Position(2, 2));

        Position target = behaviour.getTargetPosition(ghost, arena, pacman, true);

        // verify that getAlivePosition was invoked when scared and inside gate
        try {
            verify(behaviour, times(1)).getAlivePosition(any(), any(), any(), anyBoolean());
        } catch (Exception e) {
            throw new AssertionError("Verification failed", e);
        }

        // result when collected < 60 should be (10,11)
        assertThat(target).isEqualTo(new Position(10, 11));
    }
}

