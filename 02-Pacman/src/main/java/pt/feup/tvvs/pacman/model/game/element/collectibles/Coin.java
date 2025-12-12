package pt.feup.tvvs.pacman.model.game.element.collectibles;

import pt.feup.tvvs.pacman.model.Position;

public class Coin extends Collectible {
    public Coin(Position pos) {
        super(pos, 10);
    }
}
