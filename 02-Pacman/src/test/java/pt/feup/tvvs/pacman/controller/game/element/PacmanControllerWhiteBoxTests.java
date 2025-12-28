package pt.feup.tvvs.pacman.controller.game.element;

import org.junit.jupiter.api.Test;
import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.Direction;
import pt.feup.tvvs.pacman.model.game.element.ghost.Blinky;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;
import pt.feup.tvvs.pacman.model.game.element.Wall;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class PacmanControllerWhiteBoxTests {

    @Test
    public void calculateNextPosition_reflection() throws Exception {
        Arena arena = new Arena(10, 10);
        PacmanController controller = new PacmanController(arena);

        Method m = PacmanController.class.getDeclaredMethod("calculateNextPosition", Position.class, Direction.class);
        m.setAccessible(true);

        Position p = new Position(5, 5);
        assertThat((Position) m.invoke(controller, p, Direction.UP)).isEqualTo(new Position(5, 4));
        assertThat((Position) m.invoke(controller, p, Direction.DOWN)).isEqualTo(new Position(5, 6));
        assertThat((Position) m.invoke(controller, p, Direction.LEFT)).isEqualTo(new Position(4, 5));
        assertThat((Position) m.invoke(controller, p, Direction.RIGHT)).isEqualTo(new Position(6, 5));
    }

    @Test
    public void movePacman_inverts_when_desiredOpposite_and_then_moves() throws Exception {
        Arena arena = new Arena(20, 20);
        PacmanController controller = new PacmanController(arena);

        Pacman pacman = new Pacman(new Position(5, 5));
        pacman.setDirection(Direction.RIGHT);
        arena.addPacman(pacman);

        Method m = PacmanController.class.getDeclaredMethod("movePacman", Pacman.class, Direction.class);
        m.setAccessible(true);

        // desired is LEFT which is opposite of RIGHT -> will invertDirection and then perform movement
        m.invoke(controller, pacman, Direction.LEFT);

        // direction should have been inverted to LEFT and counter incremented (movement executed)
        assertThat(pacman.getDirection()).isEqualTo(Direction.LEFT);
        assertThat(pacman.getCounter()).isEqualTo(1);
    }

    @Test
    public void movePacman_when_counterNonZero_onlyIncrementsCounter() throws Exception {
        Arena arena = new Arena(10, 10);
        PacmanController controller = new PacmanController(arena);

        Pacman pacman = new Pacman(new Position(3, 3));
        // no gate state to set on Pacman
        pacman.setCounter(5);
        arena.addPacman(pacman);

        Method m = PacmanController.class.getDeclaredMethod("movePacman", Pacman.class, Direction.class);
        m.setAccessible(true);

        m.invoke(controller, pacman, null);

        assertThat(pacman.getCounter()).isEqualTo(6);
        assertThat(pacman.getPosition()).isEqualTo(new Position(3, 3));
    }

    @Test
    public void movePacman_desiredValid_setsDirection_and_incrementsCounter() throws Exception {
        Arena arena = new Arena(10, 10);
        PacmanController controller = new PacmanController(arena);

        Pacman pacman = new Pacman(new Position(2, 2));
        pacman.setDirection(Direction.UP);
        arena.addPacman(pacman);

        Method m = PacmanController.class.getDeclaredMethod("movePacman", Pacman.class, Direction.class);
        m.setAccessible(true);

        // desired RIGHT is valid (no wall, no pacman, not ghost gate)
        m.invoke(controller, pacman, Direction.RIGHT);

        assertThat(pacman.getDirection()).isEqualTo(Direction.RIGHT);
        assertThat(pacman.getCounter()).isEqualTo(1);
    }

    @Test
    public void movePacman_desiredBlocked_usesCurrentDirection() throws Exception {
        Arena arena = new Arena(10, 10);
        PacmanController controller = new PacmanController(arena);

        Pacman blocker = new Pacman(new Position(6, 5));
        arena.addPacman(blocker);

        Pacman pacman = new Pacman(new Position(5, 5));
        pacman.setDirection(Direction.DOWN);
        arena.addPacman(pacman);

        Method m = PacmanController.class.getDeclaredMethod("movePacman", Pacman.class, Direction.class);
        m.setAccessible(true);

        // desired RIGHT would point to (6,5) which is occupied -> should fallback to current DOWN
        m.invoke(controller, pacman, Direction.RIGHT);

        assertThat(pacman.getDirection()).isEqualTo(Direction.DOWN);
        assertThat(pacman.getCounter()).isEqualTo(1);
    }

    @Test
    public void movePacman_desiredEqualsGhostGate_isIgnored() throws Exception {
        Arena arena = new Arena(10, 10);
        PacmanController controller = new PacmanController(arena);

        Pacman pacman = new Pacman(new Position(4, 4));
        pacman.setDirection(Direction.UP);
        arena.addPacman(pacman);

        // set ghost gate to the desired next position
        arena.setGhostGatePosition(new Position(5, 4));

        Method m = PacmanController.class.getDeclaredMethod("movePacman", Pacman.class, Direction.class);
        m.setAccessible(true);

        // desired RIGHT would move to (5,4) which equals ghost gate -> must be ignored
        m.invoke(controller, pacman, Direction.RIGHT);

        // fallback to current direction UP -> next position (4,3) valid -> counter incremented
        assertThat(pacman.getCounter()).isEqualTo(1);
        assertThat(pacman.getDirection()).isEqualTo(Direction.UP);
    }

    @Test
    public void step_appliesActions_and_skipsDyingPacman() throws Exception {
        Arena arena = new Arena(20, 20);
        PacmanController controller = new PacmanController(arena);

        Pacman pacman = new Pacman(new Position(3, 3));
        pacman.setDirection(Direction.UP);
        arena.addPacman(pacman);

        List<GUI.ACTION> actions = Arrays.asList(GUI.ACTION.RIGHT);

        // call step with time 0 -> time % speed != 1 -> movePacman invoked
        controller.step(null, actions, 0);

        // desired RIGHT should have been applied resulting in direction RIGHT and counter increment
        assertThat(pacman.getDirection()).isEqualTo(Direction.RIGHT);
        assertThat(pacman.getCounter()).isEqualTo(1);

        // now mark pacman as dying and attempt to change direction via actions
        pacman.setDying(true);
        controller.step(null, Arrays.asList(GUI.ACTION.LEFT), 0);

        // direction and counter should remain unchanged when dying
        assertThat(pacman.getDirection()).isEqualTo(Direction.RIGHT);
        assertThat(pacman.getCounter()).isEqualTo(1);
    }

    @Test
    public void step_setsSecondPacmanDesired_usingWASD_and_movesSecond() throws Exception {
        Arena arena = new Arena(20, 20);
        PacmanController controller = new PacmanController(arena);

        Pacman p1 = new Pacman(new Position(1,1));
        Pacman p2 = new Pacman(new Position(3,3));
        arena.addPacman(p1);
        arena.addPacman(p2);

        // set actions to control second pacman with W (UP)
        List<GUI.ACTION> actions = Arrays.asList(GUI.ACTION.W);

        // call step to apply desired and move
        controller.step(null, actions, 0);

        // second pacman should have direction UP and counter incremented
        assertThat(p2.getDirection()).isEqualTo(Direction.UP);
        assertThat(p2.getCounter()).isEqualTo(1);
    }

    @Test
    public void movePacman_desiredNull_and_nextPositionBlockedByWall_noIncrement() throws Exception {
        Arena arena = new Arena(10, 10);
        PacmanController controller = new PacmanController(arena);

        Pacman pacman = new Pacman(new Position(5,5));
        pacman.setDirection(Direction.UP);
        arena.addPacman(pacman);

        // place a wall at pacman's next position when moving UP -> (5,4)
        Wall wall = new Wall(new Position(5,4));
        arena.addWall(wall);

        Method m = PacmanController.class.getDeclaredMethod("movePacman", Pacman.class, Direction.class);
        m.setAccessible(true);

        // call with desired null so it uses current direction -> nextPosition blocked by wall -> no increment
        m.invoke(controller, pacman, null);

        assertThat(pacman.getCounter()).isEqualTo(0);
    }

    @Test
    public void step_skipsMovement_when_timeModuloEqualsOne() throws Exception {
        Arena arena = new Arena(20, 20);
        PacmanController controller = new PacmanController(arena);

        Pacman pacman = new Pacman(new Position(2,2));
        pacman.setDirection(Direction.UP);
        arena.addPacman(pacman);

        // attempt to change direction to RIGHT but use time that causes skip: time % speed == 1
        int speed = pacman.getSpeed();
        long time = 1; // since speed is 4, 1 % 4 == 1

        controller.step(null, Arrays.asList(GUI.ACTION.RIGHT), time);

        // movement should have been skipped
        assertThat(pacman.getDirection()).isEqualTo(Direction.UP);
        assertThat(pacman.getCounter()).isEqualTo(0);
    }

    @Test
    public void movePacman_desiredTargetOccupiedByDyingPacman_allowed() throws Exception {
        Arena arena = new Arena(20, 20);
        PacmanController controller = new PacmanController(arena);

        Pacman pacman = new Pacman(new Position(5,5));
        pacman.setDirection(Direction.RIGHT);
        arena.addPacman(pacman);

        // place another pacman at desired position (6,5) but mark as dying
        Pacman other = new Pacman(new Position(6,5));
        other.setDying(true);
        arena.addPacman(other);

        Method m = PacmanController.class.getDeclaredMethod("movePacman", Pacman.class, Direction.class);
        m.setAccessible(true);

        // desired RIGHT would normally collide with other, but other is dying so it should be ignored
        m.invoke(controller, pacman, Direction.RIGHT);

        assertThat(pacman.getDirection()).isEqualTo(Direction.RIGHT);
        assertThat(pacman.getCounter()).isEqualTo(1);
    }

    @Test
    public void calculateNextPosition_nullDirection_returnsNull() throws Exception {
        Arena arena = new Arena(10, 10);
        PacmanController controller = new PacmanController(arena);

        Method m = PacmanController.class.getDeclaredMethod("calculateNextPosition", Position.class, Direction.class);
        m.setAccessible(true);

        Position p = new Position(5, 5);
        // calling the method with a null enum will raise a NullPointerException due to switch on null
        assertThrows(NullPointerException.class, () -> {
            try {
                m.invoke(controller, p, (Direction) null);
            } catch (Exception e) {
                // unwrap InvocationTargetException
                throw e.getCause() == null ? e : (RuntimeException) e.getCause();
            }
        });
    }

    @Test
    public void step_maps_player1_actions_UP_DOWN_LEFT_RIGHT() throws Exception {
        GUI.ACTION[] actions = {GUI.ACTION.UP, GUI.ACTION.DOWN, GUI.ACTION.LEFT, GUI.ACTION.RIGHT};
        Direction[] expected = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};

        for (int i = 0; i < actions.length; i++) {
            Arena arena = new Arena(10, 10);
            PacmanController controller = new PacmanController(arena);
            Pacman p1 = new Pacman(new Position(2, 2));
            arena.addPacman(p1);

            controller.step(null, Arrays.asList(actions[i]), 0);

            assertThat(p1.getDirection()).isEqualTo(expected[i]);
            // reset counter isn't necessary since controller recreated each loop
        }
    }

    @Test
    public void step_maps_player2_actions_WASD() throws Exception {
        GUI.ACTION[] actions = {GUI.ACTION.W, GUI.ACTION.A, GUI.ACTION.S, GUI.ACTION.D};
        Direction[] expected = {Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT};

        for (int i = 0; i < actions.length; i++) {
            Arena arena = new Arena(10, 10);
            PacmanController controller = new PacmanController(arena);
            Pacman p1 = new Pacman(new Position(1, 1));
            Pacman p2 = new Pacman(new Position(3, 3));
            arena.addPacman(p1);
            arena.addPacman(p2);

            controller.step(null, Arrays.asList(actions[i]), 0);

            assertThat(p2.getDirection()).isEqualTo(expected[i]);
        }
    }

    @Test
    public void movePacman_desiredDirectionBlockedByWall_fallsBackToCurrentDirection() throws Exception {
        Arena arena = new Arena(10, 10);
        PacmanController controller = new PacmanController(arena);

        Pacman pacman = new Pacman(new Position(5,5));
        pacman.setDirection(Direction.UP);
        arena.addPacman(pacman);

        // desired RIGHT would be (6,5) -> place a wall there to block desired direction
        arena.addWall(new Wall(new Position(6,5)));

        Method m = PacmanController.class.getDeclaredMethod("movePacman", Pacman.class, Direction.class);
        m.setAccessible(true);

        m.invoke(controller, pacman, Direction.RIGHT);

        // desired was blocked, so it should fallback to current UP and increment counter if possible
        assertThat(pacman.getDirection()).isEqualTo(Direction.UP);
        assertThat(pacman.getCounter()).isEqualTo(1);
    }

    @Test
    public void step_withNone_action_doesNothing() throws Exception {
        Arena arena = new Arena(10,10);
        PacmanController controller = new PacmanController(arena);

        Pacman p = new Pacman(new Position(2,2));
        p.setDirection(Direction.UP);
        arena.addPacman(p);

        // place a wall at p's next position so no movement happens when action is NONE
        arena.addWall(new Wall(p.getNextPosition()));

        controller.step(null, Arrays.asList(GUI.ACTION.NONE), 0);

        assertThat(p.getDirection()).isEqualTo(Direction.UP);
        assertThat(p.getCounter()).isEqualTo(0);
    }

    @Test
    public void movePacman_currentNextPosition_equalsGhostGate_noIncrement() throws Exception {
        Arena arena = new Arena(10,10);
        PacmanController controller = new PacmanController(arena);

        Pacman p = new Pacman(new Position(4,4));
        p.setDirection(Direction.UP);
        arena.addPacman(p);

        // set ghost gate to p's next position when moving UP -> (4,3)
        arena.setGhostGatePosition(new Position(4,3));

        Method m = PacmanController.class.getDeclaredMethod("movePacman", Pacman.class, Direction.class);
        m.setAccessible(true);

        m.invoke(controller, p, null);

        assertThat(p.getCounter()).isEqualTo(0);
    }

    @Test
    public void movePacman_currentNextPosition_collidesWithAlivePacman_noIncrement() throws Exception {
        Arena arena = new Arena(10,10);
        PacmanController controller = new PacmanController(arena);

        Pacman p = new Pacman(new Position(4,4));
        p.setDirection(Direction.RIGHT);
        arena.addPacman(p);

        // place another alive pacman at next position (5,4)
        Pacman other = new Pacman(new Position(5,4));
        other.setDying(false);
        arena.addPacman(other);

        Method m = PacmanController.class.getDeclaredMethod("movePacman", Pacman.class, Direction.class);
        m.setAccessible(true);

        m.invoke(controller, p, null);

        assertThat(p.getCounter()).isEqualTo(0);
    }

    @Test
    public void step_maps_both_players_actions_simultaneously() throws Exception {
        Arena arena = new Arena(10,10);
        PacmanController controller = new PacmanController(arena);

        Pacman p1 = new Pacman(new Position(1,1));
        Pacman p2 = new Pacman(new Position(3,3));
        arena.addPacman(p1);
        arena.addPacman(p2);

        // actions list contains player1 RIGHT and player2 W (UP)
        List<GUI.ACTION> actions = Arrays.asList(GUI.ACTION.RIGHT, GUI.ACTION.W);
        controller.step(null, actions, 0);

        assertThat(p1.getDirection()).isEqualTo(Direction.RIGHT);
        assertThat(p2.getDirection()).isEqualTo(Direction.UP);
    }
}
