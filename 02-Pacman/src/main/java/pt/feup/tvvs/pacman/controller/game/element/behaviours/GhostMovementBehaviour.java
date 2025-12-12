package pt.feup.tvvs.pacman.controller.game.element.behaviours;

import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.ghost.Ghost;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;


public abstract class GhostMovementBehaviour {
    public Position getTargetPosition(Ghost ghost, Arena arena, Pacman targetPacman, boolean chaseMode) {
        switch (ghost.getState()) {
            case ALIVE:
                return getAlivePosition(ghost, arena, targetPacman, chaseMode);
            case DEAD:
                return arena.getGhostGate().getPosition();
            case SCARED:
                return (ghost.isInsideGate() ? getAlivePosition(ghost, arena, targetPacman, chaseMode) : new Position((int) (Math.random() * 29), (int) (Math.random() * 16)));
        }
        return null;
    }

    protected abstract Position getAlivePosition(Ghost ghost, Arena arena, Pacman targetPacman, boolean chaseMode);
}
