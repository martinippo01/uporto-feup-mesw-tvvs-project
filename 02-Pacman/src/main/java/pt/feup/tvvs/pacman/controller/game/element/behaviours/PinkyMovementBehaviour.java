package pt.feup.tvvs.pacman.controller.game.element.behaviours;

import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.ghost.Ghost;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;

public class PinkyMovementBehaviour extends GhostMovementBehaviour {
    @Override
    protected Position getAlivePosition(Ghost ghost, Arena arena, Pacman targetPacman, boolean chaseMode) {
        if (ghost.isInsideGate()) return arena.getGhostGate().getPosition();
        if (!chaseMode) return new Position(0, 0);

        int newX = targetPacman.getPosition().getX(), newY = targetPacman.getPosition().getY();
        switch (targetPacman.getDirection()) {
            case UP:
                newY = Math.max(0, newY - 3); break;
            case DOWN:
                newY = Math.min(arena.getHeight(), newY + 3); break;
            case RIGHT:
                newX = Math.min(arena.getWidth(), newX + 3); break;
            case LEFT:
                newX = Math.max(0, newX - 3); break;
        }
        return new Position(newX, newY);
    }
}
