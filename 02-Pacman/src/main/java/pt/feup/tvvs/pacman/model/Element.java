package pt.feup.tvvs.pacman.model;

import java.util.Objects;

public abstract class Element {
    private final Position position;

    protected Element(Position pos) {
        if (pos.getY() < 0 || pos.getX() < 0) {
            throw new IllegalArgumentException("Element position cannot have negatives values: " + pos);
        }
        position = pos;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position other) {
        if (other.getY() < 0 || other.getX() < 0) {
            throw new IllegalArgumentException("Element position cannot have negatives values: " + other);
        }
        position.setPosition(other);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Element)) return false;
        Element element = (Element) obj;
        return this.position.equals(element.position) && this.getClass() == element.getClass();
    }

    @Override
    public int hashCode() {
        return Objects.hash(position.getX(), position.getX(), getClass());
    }
}
