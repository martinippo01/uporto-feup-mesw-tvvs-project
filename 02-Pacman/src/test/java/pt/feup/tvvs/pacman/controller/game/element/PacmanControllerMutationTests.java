package pt.feup.tvvs.pacman.controller.game.element;

import org.junit.jupiter.api.Test;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.Direction;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PacmanControllerMutationTests {

    @Test
    void desiredMovement_blocked_when_other_collides_and_allowed_when_not() throws Exception {
        Arena arena = new Arena(10,10);
        PacmanController controller = new PacmanController(arena);

        Pacman pac = new Pacman(new Position(5,5));
        pac.setDirection(Direction.UP);
        arena.addPacman(pac);

        // create a mock other pacman at the desired target (6,5)
        Pacman other = mock(Pacman.class);
        when(other.isDying()).thenReturn(false);
        when(other.getPosition()).thenReturn(new Position(6,5));
        // scenario A: other collides => movement should be blocked
        when(other.collidingWith(any(Pacman.class))).thenReturn(true);
        arena.addPacman(other);

        Method m = PacmanController.class.getDeclaredMethod("movePacman", Pacman.class, Direction.class);
        m.setAccessible(true);

        // desired RIGHT should be blocked by other -> pac remains UP and counter unchanged
        m.invoke(controller, pac, Direction.RIGHT);
        assertThat(pac.getDirection()).isEqualTo(Direction.UP);
        assertThat(pac.getCounter()).isEqualTo(0);

        // reset pac and change other to not collide
        pac.setDirection(Direction.UP);
        pac.setCounter(0);
        // scenario B: other does NOT collide -> movement allowed
        when(other.collidingWith(any(Pacman.class))).thenReturn(false);

        m.invoke(controller, pac, Direction.RIGHT);
        assertThat(pac.getDirection()).isEqualTo(Direction.RIGHT);
        assertThat(pac.getCounter()).isEqualTo(1);
    }

    @Test
    void fallbackMovement_blocked_or_allowed_based_on_collisions_on_nextPosition() throws Exception {
        Arena arena = new Arena(10,10);
        PacmanController controller = new PacmanController(arena);

        // place pacman facing RIGHT so nextPosition is (6,5)
        Pacman pac = new Pacman(new Position(5,5));
        pac.setDirection(Direction.RIGHT);
        pac.setCounter(0);
        arena.addPacman(pac);

        Pacman other = mock(Pacman.class);
        when(other.isDying()).thenReturn(false);
        when(other.getPosition()).thenReturn(new Position(6,5));
        arena.addPacman(other);

        Method m = PacmanController.class.getDeclaredMethod("movePacman", Pacman.class, Direction.class);
        m.setAccessible(true);

        // when other.collidingWith returns true -> fallback movement should be blocked
        when(other.collidingWith(any(Pacman.class))).thenReturn(true);
        m.invoke(controller, pac, null);
        assertThat(pac.getCounter()).isEqualTo(0);

        // when other.collidingWith returns false -> fallback movement allowed
        when(other.collidingWith(any(Pacman.class))).thenReturn(false);
        m.invoke(controller, pac, null);
        assertThat(pac.getCounter()).isEqualTo(1);
    }
}
