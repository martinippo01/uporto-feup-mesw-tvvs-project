package pt.feup.tvvs.pacman.controller.game.element.behaviours;

import org.junit.jupiter.api.Test;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.ghost.Blinky;
import pt.feup.tvvs.pacman.model.game.element.ghost.Ghost;
import pt.feup.tvvs.pacman.model.game.element.ghost.GhostState;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class GhostMovementBehaviourMutationTests {

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
    public void scared_outside_random_notAlwaysZero_afterManySamples() {
        Arena arena = new Arena(29, 16);
        Blinky ghost = new Blinky(new Position(1, 1));
        ghost.setOutsideGate();
        ghost.setState(GhostState.SCARED);

        TestGhostMovementBehaviour behaviour = new TestGhostMovementBehaviour(new Position(0, 0));
        Pacman pacman = new Pacman(new Position(0, 0));

        final int SAMPLES = 200;
        boolean foundNonZeroX = false;
        boolean foundNonZeroY = false;
        for (int i = 0; i < SAMPLES; i++) {
            Position target = behaviour.getTargetPosition(ghost, arena, pacman, true);
            assertThat(target).isNotNull();
            assertThat(target.getX()).isBetween(0, 28);
            assertThat(target.getY()).isBetween(0, 15);
            if (target.getX() != 0) {
                foundNonZeroX = true;
            }
            if (target.getY() != 0) {
                foundNonZeroY = true;
            }
            if (foundNonZeroX && foundNonZeroY) {
                break;
            }
        }

        // Ensure both coordinates can be non-zero (mutations that replace multiplication with division
        // on one coordinate would make that coordinate always 0 and thus be detected here).
        assertThat(foundNonZeroX).isTrue();
        assertThat(foundNonZeroY).isTrue();
    }

    @Test
    public void scared_outside_coordinates_vary_acrossSamples() {
        Arena arena = new Arena(29, 16);
        Blinky ghost = new Blinky(new Position(2, 2));
        ghost.setOutsideGate();
        ghost.setState(GhostState.SCARED);

        TestGhostMovementBehaviour behaviour = new TestGhostMovementBehaviour(new Position(0, 0));
        Pacman pacman = new Pacman(new Position(0, 0));

        final int SAMPLES = 200;
        Set<Integer> xs = new HashSet<>();
        Set<Integer> ys = new HashSet<>();
        for (int i = 0; i < SAMPLES; i++) {
            Position target = behaviour.getTargetPosition(ghost, arena, pacman, false);
            assertThat(target.getX()).isBetween(0, 28);
            assertThat(target.getY()).isBetween(0, 15);
            xs.add(target.getX());
            ys.add(target.getY());
            if (xs.size() > 1 && ys.size() > 1) break;
        }

        // Expect both x and y to take more than one distinct value across many samples.
        assertThat(xs.size()).isGreaterThan(1);
        assertThat(ys.size()).isGreaterThan(1);
    }
}
