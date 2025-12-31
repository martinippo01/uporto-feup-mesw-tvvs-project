package pt.feup.tvvs.pacman.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class ElementWhiteBoxTests {

    // Minimal concrete subclass for testing
    private static class TestElement extends Element {
        protected TestElement(Position pos) {
            super(pos);
        }
    }

    private static class OtherElement extends Element {
        protected OtherElement(Position pos) {
            super(pos);
        }
    }

    @Test
    public void constructor_rejects_negative_x() {
        Position p = new Position(-1, 0);
        assertThatThrownBy(() -> new TestElement(p))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot have negatives");
    }

    @Test
    public void constructor_rejects_negative_y() {
        Position p = new Position(0, -5);
        assertThatThrownBy(() -> new TestElement(p))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot have negatives");
    }

    @Test
    public void getPosition_returns_same_reference_passed_to_constructor() {
        Position p = new Position(2, 3);
        TestElement e = new TestElement(p);
        assertThat(e.getPosition()).isSameAs(p);
    }

    @Test
    public void setPosition_delegates_to_internal_position_setPosition() {
        // use a mock for the internal position so we can verify delegation
        Position internal = mock(Position.class);
        when(internal.getX()).thenReturn(1);
        when(internal.getY()).thenReturn(1);

        // construct element using a spy subclass that allows passing the mock
        TestElement e = new TestElement(internal);

        Position other = new Position(7, 8);
        e.setPosition(other);

        // verify the internal position's setPosition was called with the provided other
        verify(internal).setPosition(other);
    }

    @Test
    public void setPosition_rejects_negative_coordinates() {
        TestElement e = new TestElement(new Position(4, 4));
        Position bad = new Position(-2, 5);
        assertThatThrownBy(() -> e.setPosition(bad))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot have negatives");
    }

    @Test
    public void equals_and_hashCode_behaviour_same_class_same_position() {
        TestElement a = new TestElement(new Position(5, 6));
        TestElement b = new TestElement(new Position(5, 6));

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isEqualTo(a); // reflexive
    }

    @Test
    public void equals_different_class_same_position_is_false_and_hashcode_differs() {
        Position p = new Position(9, 9);
        TestElement a = new TestElement(p);
        OtherElement o = new OtherElement(new Position(9, 9));

        assertThat(a).isNotEqualTo(o);
        // hashCode includes class, so it's very likely different; assertNotEquals to be strict
        assertThat(a.hashCode()).isNotEqualTo(o.hashCode());
    }

    @Test
    public void equals_returns_false_for_null_or_other_types() {
        TestElement a = new TestElement(new Position(1, 1));
        assertThat(a.equals(null)).isFalse();
        assertThat(a.equals("not an element")).isFalse();
    }
}
