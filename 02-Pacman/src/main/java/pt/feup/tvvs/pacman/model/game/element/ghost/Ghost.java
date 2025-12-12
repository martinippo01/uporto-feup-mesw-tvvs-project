package pt.feup.tvvs.pacman.model.game.element.ghost;

import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.element.MovableElement;


public abstract class Ghost extends MovableElement {
    private GhostState state;
    private Boolean insideGate;
    private Position respawnPosition;

    public Ghost(Position pos) {
        super(pos);
        state = GhostState.ALIVE;
        this.insideGate = true;
        setSpeed(3);
    }

    public GhostState getState() {
        return state;
    }

    public void setState(GhostState ghostState) {
        this.state = ghostState;
    }

    public boolean isDead() {
        return state == GhostState.DEAD;
    }

    public boolean isScared() {
        return state == GhostState.SCARED;
    }

    public Boolean isInsideGate() {
        return insideGate;
    }

    public void setInsideGate() {
        this.insideGate = true;
    }

    public void setOutsideGate() {
        this.insideGate = false;
    }

    public Position getRespawnPosition() {
        return respawnPosition;
    }

    public void setRespawnPosition(Position respawnPosition) {
        this.respawnPosition = respawnPosition;
    }
}
