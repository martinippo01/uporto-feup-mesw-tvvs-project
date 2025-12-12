package pt.feup.tvvs.pacman.model.game.element.collectibles;

import pt.feup.tvvs.pacman.model.Element;
import pt.feup.tvvs.pacman.model.Position;

public abstract class Collectible extends Element {
    private final int value;

    public Collectible(Position pos, int value) {
        super(pos);
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
