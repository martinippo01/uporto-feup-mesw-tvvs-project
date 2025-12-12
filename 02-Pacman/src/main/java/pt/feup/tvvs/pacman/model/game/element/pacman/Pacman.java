package pt.feup.tvvs.pacman.model.game.element.pacman;

import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.element.MovableElement;

public class Pacman extends MovableElement {
    private int life;
    private boolean isDying;
    private Position respawnPosition;

    public Pacman(Position pos) {
        super(pos);
        this.life = 3;
        setSpeed(4);
    }

    public void decreaseLife() {
        this.life--;
    }

    public int getLife() {
        return life;
    }

    public void setLife(int life) {
        this.life = life;
    }

    public Position getRespawnPosition() {
        return respawnPosition;
    }

    public void setRespawnPosition(Position respawnPosition) {
        this.respawnPosition = respawnPosition;
    }

    public boolean isDying() {
        return isDying;
    }

    public void setDying(boolean dying) {
        isDying = dying;
    }
}
