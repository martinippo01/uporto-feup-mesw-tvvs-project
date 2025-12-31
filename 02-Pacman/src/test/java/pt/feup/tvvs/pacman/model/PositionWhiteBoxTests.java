package pt.feup.tvvs.pacman.model;

import com.googlecode.lanterna.TerminalPosition;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class PositionWhiteBoxTests {

    @Test
    public void constructor_and_getters_setters_work() {
        Position p = new Position(3, 4);
        assertThat(p.getX()).isEqualTo(3);
        assertThat(p.getY()).isEqualTo(4);

        p.setX(7);
        p.setY(8);
        assertThat(p.getX()).isEqualTo(7);
        assertThat(p.getY()).isEqualTo(8);
    }

    @Test
    public void copy_constructor_creates_independent_copy() {
        Position original = new Position(5, 6);
        Position copy = new Position(original);
        assertThat(copy).isEqualTo(original);
        // change original
        original.setX(20);
        original.setY(21);
        // copy should not change
        assertThat(copy.getX()).isEqualTo(5);
        assertThat(copy.getY()).isEqualTo(6);
    }

    @Test
    public void directional_methods_return_offsets() {
        Position p = new Position(10, 10);
        assertThat(p.getLeft()).isEqualTo(new Position(9, 10));
        assertThat(p.getRight()).isEqualTo(new Position(11, 10));
        assertThat(p.getUp()).isEqualTo(new Position(10, 9));
        assertThat(p.getDown()).isEqualTo(new Position(10, 11));

        // ensure they return new instances (not same reference)
        assertThat(p.getLeft()).isNotSameAs(p);
    }

    @Test
    public void setPosition_copies_values() {
        Position a = new Position(1, 2);
        Position b = new Position(9, 8);
        a.setPosition(b);
        assertThat(a).isEqualTo(b);
    }

    @Test
    public void squaredDistance_calculation_is_correct() {
        Position a = new Position(0, 0);
        Position b = new Position(3, 4);
        double d = a.squaredDistance(b);
        // 3^2 + 4^2 = 9 + 16 = 25
        assertThat(d).isCloseTo(25.0, within(1e-9));

        // same position -> 0
        assertThat(a.squaredDistance(a)).isCloseTo(0.0, within(1e-9));
    }

    @Test
    public void toTerminalPosition_has_same_coordinates() {
        Position p = new Position(2, 5);
        TerminalPosition tp = p.toTerminalPosition();
        // TerminalPosition provides getColumn/getRow
        assertThat(tp.getColumn()).isEqualTo(2);
        assertThat(tp.getRow()).isEqualTo(5);
    }

    @Test
    public void equals_and_hashcode_and_toString() {
        Position a = new Position(4, 7);
        Position b = new Position(4, 7);
        Position c = new Position(4, 8);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("some string");

        assertThat(a.toString()).isEqualTo("(4, 7)");
    }

    @Test
    public void equals_uses_getters_on_argument() {
        Position real = new Position(1, 1);
        Position spyPos = spy(new Position(1, 1));

        // call equals on real with spy as argument; equals implementation calls getX/getY on argument
        boolean eq = real.equals(spyPos);
        assertThat(eq).isTrue();

        // verify that equals invoked getters on the spy
        verify(spyPos).getX();
        verify(spyPos).getY();
    }
}
