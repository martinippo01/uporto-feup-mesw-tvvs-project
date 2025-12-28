package pt.feup.tvvs.pacman.controller.game.element;

import org.junit.jupiter.api.Test;
import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.controller.game.element.GhostController;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.Direction;
import pt.feup.tvvs.pacman.model.game.element.ghost.Blinky;
import pt.feup.tvvs.pacman.model.game.element.ghost.Ghost;
import pt.feup.tvvs.pacman.model.game.element.ghost.GhostState;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class GhostControllerWhiteBoxTests {

    @Test
    public void testGetNextPosition_reflection() throws Exception {
        Arena arena = new Arena(10,10);
        GhostController controller = new GhostController(arena);

        Method m = GhostController.class.getDeclaredMethod("getNextPosition", Position.class, Direction.class);
        m.setAccessible(true);

        Position p = new Position(5,5);
        Position up = (Position) m.invoke(controller, p, Direction.UP);
        Position down = (Position) m.invoke(controller, p, Direction.DOWN);
        Position left = (Position) m.invoke(controller, p, Direction.LEFT);
        Position right = (Position) m.invoke(controller, p, Direction.RIGHT);

        assertThat(up).isEqualTo(new Position(5,4));
        assertThat(down).isEqualTo(new Position(5,6));
        assertThat(left).isEqualTo(new Position(4,5));
        assertThat(right).isEqualTo(new Position(6,5));
    }

    @Test
    public void testIsChaseMode_boundaries() throws Exception {
        Arena arena = new Arena(5,5);
        GhostController controller = new GhostController(arena);

        Field frameCount = GhostController.class.getDeclaredField("frameCount");
        frameCount.setAccessible(true);

        Method isChase = GhostController.class.getDeclaredMethod("isChaseMode");
        isChase.setAccessible(true);

        frameCount.setInt(controller, 0);
        assertThat((Boolean) isChase.invoke(controller)).isFalse();

        frameCount.setInt(controller, 450);
        assertThat((Boolean) isChase.invoke(controller)).isTrue();

        frameCount.setInt(controller, 2699);
        assertThat((Boolean) isChase.invoke(controller)).isTrue();

        frameCount.setInt(controller, 2700);
        assertThat((Boolean) isChase.invoke(controller)).isFalse();

        frameCount.setInt(controller, 3199);
        assertThat((Boolean) isChase.invoke(controller)).isFalse();

        frameCount.setInt(controller, 3200);
        assertThat((Boolean) isChase.invoke(controller)).isTrue();
    }

    @Test
    public void testGetDirectionTowards_excludesOpposite_and_selectsNearest() throws Exception {
        Arena arena = new Arena(20,20);
        GhostController controller = new GhostController(arena);

        // create ghost at (2,2) with current direction LEFT (so RIGHT is opposite)
        Blinky ghost = new Blinky(new Position(2,2));
        ghost.setOutsideGate();
        ghost.setDirection(Direction.LEFT);

        // target is to the right at (5,2) - RIGHT would be closest but is opposite and should be excluded
        Position target = new Position(5,2);

        Method m = GhostController.class.getDeclaredMethod("getDirectionTowards", Ghost.class, Position.class);
        m.setAccessible(true);

        Direction chosen = (Direction) m.invoke(controller, ghost, target);

        // expected to pick UP (distance 9) or DOWN (distance 9) but UP is iterated first in Direction.values()
        assertThat(chosen).isEqualTo(Direction.UP);
    }

    @Test
    public void testGetDirectionTowards_avoidsGhostGate_unlessInside() throws Exception {
        Arena arena = new Arena(10,10);
        // set ghost gate to known position via arena.setGhostGatePosition
        arena.setGhostGatePosition(new Position(3,2));

        GhostController controller = new GhostController(arena);

        Blinky ghost = new Blinky(new Position(2,2));
        ghost.setOutsideGate();
        ghost.setDirection(Direction.UP);

        // target is to the right -> RIGHT would move onto ghost gate (3,2)
        Position target = new Position(5,2);

        Method m = GhostController.class.getDeclaredMethod("getDirectionTowards", Ghost.class, Position.class);
        m.setAccessible(true);

        Direction chosen = (Direction) m.invoke(controller, ghost, target);

        // RIGHT should be avoided because it equals ghost gate and ghost is outside -> choose another direction
        assertThat(chosen).isNotEqualTo(Direction.RIGHT);
    }

    @Test
    public void testStep_moveGhost_counterNonZero_incrementsCounter_noPositionChange() throws Exception {
        Arena arena = new Arena(10,10);
        GhostController controller = new GhostController(arena);

        Blinky ghost = new Blinky(new Position(4,4));
        ghost.setOutsideGate();
        ghost.setCounter(5);

        arena.addGhost(ghost);
        arena.addPacman(new Pacman(new Position(0,0)));

        // call step with time that causes moveGhost to be invoked (time % speed != 1). speed default 3
        controller.step(null, new ArrayList<>(), 0);

        // counter should have incremented by 1
        assertThat(ghost.getCounter()).isEqualTo(6);
        // position should remain unchanged
        assertThat(ghost.getPosition()).isEqualTo(new Position(4,4));
    }

    @Test
    public void testMoveGhost_deadAtGate_becomesAlive_and_insideGate_and_speedSet() throws Exception {
        Arena arena = new Arena(10,10);
        GhostController controller = new GhostController(arena);

        Blinky ghost = new Blinky(arena.getGhostGate().getPosition());
        ghost.setOutsideGate();
        ghost.setState(GhostState.DEAD);

        arena.addGhost(ghost);
        arena.addPacman(new Pacman(new Position(1,1)));

        // call step to invoke moveGhost
        controller.step(null, new ArrayList<>(), 0);

        assertThat(ghost.getState()).isEqualTo(GhostState.ALIVE);
        assertThat(ghost.isInsideGate()).isTrue();
        assertThat(ghost.getSpeed()).isEqualTo(Arena.GHOST_NORMAL_SPEED);
    }

    @Test
    public void testStep_frameCount_toggle_invertsDirection_forGhostsOutsideGate() throws Exception {
        Arena arena = new Arena(10,10);
        GhostController controller = new GhostController(arena);

        Blinky ghost = new Blinky(new Position(4,4));
        ghost.setOutsideGate();
        ghost.setDirection(Direction.LEFT);

        arena.addGhost(ghost);
        arena.addPacman(new Pacman(new Position(0,0)));

        // set frameCount to toggle value 450
        Field frameCount = GhostController.class.getDeclaredField("frameCount");
        frameCount.setAccessible(true);
        frameCount.setInt(controller, 450);

        // call step; since frameCount == 450 it will invertDirection on ghosts not at gate
        // use time=1 so moveGhost won't be invoked (time % ghost.getSpeed() == 1) and the inverted direction remains
        controller.step(null, new ArrayList<>(), 1);

        // direction should be inverted to RIGHT
        assertThat(ghost.getDirection()).isEqualTo(Direction.RIGHT);
    }
}
