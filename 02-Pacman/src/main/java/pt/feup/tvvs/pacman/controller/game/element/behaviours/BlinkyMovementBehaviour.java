package pt.feup.tvvs.pacman.controller.game.element.behaviours;

import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.ghost.Ghost;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;

public class BlinkyMovementBehaviour extends GhostMovementBehaviour {
    @Override
    protected Position getAlivePosition(Ghost ghost, Arena arena, Pacman targetPacman, boolean chaseMode) {
        if (ghost.isInsideGate()) return arena.getGhostGate().getPosition();
        if (!chaseMode) return new Position(arena.getWidth(), 0);
        return targetPacman.getPosition();
    }
}
