package pt.feup.tvvs.pacman.controller.game.element.behaviours;

import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.ghost.Blinky;
import pt.feup.tvvs.pacman.model.game.element.ghost.Ghost;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;

public class InkyMovementBehaviour extends GhostMovementBehaviour {
    @Override
    protected Position getAlivePosition(Ghost ghost, Arena arena, Pacman targetPacman, boolean chaseMode) {
        if (arena.getCollectedCollectibles() < 25) return new Position(8, 11);
        if (ghost.isInsideGate()) return arena.getGhostGate().getPosition();
        if (!chaseMode) return new Position(arena.getWidth(), arena.getHeight());

        Ghost blinky = null;
        for (Ghost g : arena.getGhosts())
            if (g.getClass() == Blinky.class) {
                blinky = g;
                break;
            }
        assert blinky != null;
        Position pacmanNextPos = targetPacman.getNextPosition();

        int newX = 2 * pacmanNextPos.getX() - blinky.getPosition().getX();
        int newY = 2 * pacmanNextPos.getY() - blinky.getPosition().getY();

        //bound the new coords
        if (newX < 0) newX = 0;
        if (newX > arena.getWidth()) newX = arena.getWidth();
        if (newY < 0) newY = 0;
        if (newY > arena.getHeight()) newY = arena.getHeight();

        return new Position(newX, newY);
    }

}
