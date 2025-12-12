package pt.feup.tvvs.pacman.controller.game.element.behaviours;

import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.ghost.Ghost;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;

public class ClydeMovementBehaviour extends GhostMovementBehaviour {
    @Override
    protected Position getAlivePosition(Ghost ghost, Arena arena, Pacman targetPacman, boolean chaseMode) {
        if (arena.getCollectedCollectibles() < 60) return new Position(10, 11);
        if (ghost.isInsideGate()) return arena.getGhostGate().getPosition();
        if (!chaseMode) return new Position(0, arena.getHeight());

        if (ghost.getPosition().squaredDistance(targetPacman.getPosition()) >= 36) {//6 or more tiles away
            return targetPacman.getPosition();
        } else {
            return new Position(0, arena.getHeight());
        }
    }
}
