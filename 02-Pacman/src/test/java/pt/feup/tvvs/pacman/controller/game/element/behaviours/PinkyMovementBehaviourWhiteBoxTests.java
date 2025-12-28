package pt.feup.tvvs.pacman.controller.game.element.behaviours;

import org.junit.jupiter.api.Test;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.Direction;
import pt.feup.tvvs.pacman.model.game.element.ghost.Blinky;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PinkyMovementBehaviourWhiteBoxTests {

    @Test
    public void insideGate_returnsGhostGatePosition() {
        Arena arena = new Arena(20, 20);
        PinkyMovementBehaviour behaviour = new PinkyMovementBehaviour();

        Blinky ghost = new Blinky(new Position(4,4));
        // by default ghost is inside gate

        Pacman pacman = new Pacman(new Position(1,1));

        Position target = behaviour.getTargetPosition(ghost, arena, pacman, true);

        assertThat(target).isEqualTo(arena.getGhostGate().getPosition());
    }

    @Test
    public void notChase_outsideGate_returnsZeroZero() {
        Arena arena = new Arena(10, 10);
        PinkyMovementBehaviour behaviour = new PinkyMovementBehaviour();

        Blinky ghost = new Blinky(new Position(2,2));
        ghost.setOutsideGate();

        Pacman pacman = new Pacman(new Position(3,3));
        pacman.setDirection(Direction.UP);

        Position target = behaviour.getTargetPosition(ghost, arena, pacman, false);

        assertThat(target).isEqualTo(new Position(0,0));
    }

    @Test
    public void chase_up_movesThreeUp_and_clampsAtZero() {
        Arena arena = new Arena(15, 15);
        PinkyMovementBehaviour behaviour = new PinkyMovementBehaviour();

        Blinky ghost = new Blinky(new Position(5,5));
        ghost.setOutsideGate();

        // use Mockito to mock Pacman for this test
        Pacman pacman = mock(Pacman.class);
        when(pacman.getPosition()).thenReturn(new Position(2,1));
        when(pacman.getDirection()).thenReturn(Direction.UP);

        Position target = behaviour.getTargetPosition(ghost, arena, pacman, true);

        // newY = max(0, 1 - 3) = 0
        assertThat(target).isEqualTo(new Position(2, 0));
    }

    @Test
    public void chase_down_movesThreeDown_and_clampsToHeight() {
        Arena arena = new Arena(10, 6); // height = 6
        PinkyMovementBehaviour behaviour = new PinkyMovementBehaviour();

        Blinky ghost = new Blinky(new Position(1,1));
        ghost.setOutsideGate();

        Pacman pacman = new Pacman(new Position(4,5));
        pacman.setDirection(Direction.DOWN);

        Position target = behaviour.getTargetPosition(ghost, arena, pacman, true);

        // newY = min(arena.getHeight(), 5 + 3) = min(6,8) = 6
        assertThat(target).isEqualTo(new Position(4, 6));
    }

    @Test
    public void chase_right_movesThreeRight_and_clampsToWidth() {
        Arena arena = new Arena(7, 8); // width = 7
        PinkyMovementBehaviour behaviour = new PinkyMovementBehaviour();

        Blinky ghost = new Blinky(new Position(0,0));
        ghost.setOutsideGate();

        Pacman pacman = new Pacman(new Position(6,2));
        pacman.setDirection(Direction.RIGHT);

        Position target = behaviour.getTargetPosition(ghost, arena, pacman, true);

        // newX = min(arena.getWidth(), 6 + 3) = min(7,9) = 7
        assertThat(target).isEqualTo(new Position(7, 2));
    }

    @Test
    public void chase_left_movesThreeLeft_and_clampsAtZero() {
        Arena arena = new Arena(20, 20);
        PinkyMovementBehaviour behaviour = new PinkyMovementBehaviour();

        Blinky ghost = new Blinky(new Position(3,3));
        ghost.setOutsideGate();

        Pacman pacman = new Pacman(new Position(1,7));
        pacman.setDirection(Direction.LEFT);

        Position target = behaviour.getTargetPosition(ghost, arena, pacman, true);

        // newX = max(0, 1 - 3) = 0
        assertThat(target).isEqualTo(new Position(0, 7));
    }

}
