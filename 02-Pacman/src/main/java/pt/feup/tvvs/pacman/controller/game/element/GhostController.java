package pt.feup.tvvs.pacman.controller.game.element;

import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.controller.game.GameController;
import pt.feup.tvvs.pacman.controller.game.element.behaviours.*;
import pt.feup.tvvs.pacman.model.game.element.ghost.*;
import pt.feup.tvvs.pacman.controller.game.element.behaviours.*;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.Direction;
import pt.feup.tvvs.pacman.model.game.element.ghost.*;

import java.util.List;
import java.util.Map;

public class GhostController extends GameController {
    private final Map<Class<?>, GhostMovementBehaviour> movementBehaviours;
    private int frameCount; //useful for alternating between chase and scatter states when ghosts are alive
    private int targetPacman;

    public GhostController(Arena arena) {
        super(arena);
        this.movementBehaviours = Map.of(
                Blinky.class, new BlinkyMovementBehaviour(),
                Pinky.class, new PinkyMovementBehaviour(),
                Inky.class, new InkyMovementBehaviour(),
                Clyde.class, new ClydeMovementBehaviour()
        );
        this.frameCount = 0;
        this.targetPacman = 0;
    }


    private Position getNextPosition(Position position, Direction direction) {
        switch (direction) {
            case UP:
                return position.getUp();
            case DOWN:
                return position.getDown();
            case LEFT:
                return position.getLeft();
            case RIGHT:
                return position.getRight();
        }
        return null;
    }

    private void moveGhost(Ghost ghost) {//checks if the ghost can move in newDirection and moves it if it can
        if (ghost.getCounter() > 0) {
            ghost.incrementCounter();
            return;
        }
        Position targetPosition = movementBehaviours.get(ghost.getClass()).getTargetPosition(ghost, getModel(), getModel().getPacmans().get(targetPacman), isChaseMode());
        Direction nextDirection = getDirectionTowards(ghost, targetPosition);
        ghost.setDirection(nextDirection);
        if (ghost.getPosition().equals(getModel().getGhostGate().getPosition())) {
            if (ghost.isDead()) { //dead ghost arrives at the ghost gate
                ghost.setState(GhostState.ALIVE);
                ghost.setInsideGate();
                ghost.setSpeed(Arena.GHOST_NORMAL_SPEED);
            } else ghost.setOutsideGate();
        }
        ghost.incrementCounter();
    }

    private boolean isChaseMode() {
        return ((frameCount >= 450 && frameCount < 2700) || frameCount >= 3200);
    }

    private Direction getDirectionTowards(Ghost ghost, Position targetPosition) {//choose new direction to follow (the one with the minimum linear distance from target)
        Direction currentDirection = ghost.getDirection();
        Direction nextDirection = Direction.UP;
        double minimumDistance = Double.MAX_VALUE;
        double tempDistance;
        for (Direction direction : Direction.values()) {
            Position testPosition = getNextPosition(ghost.getPosition(), direction);
            if (!direction.isOpposite(currentDirection) && //can't move in opposite direction
                    (tempDistance = testPosition.squaredDistance(targetPosition)) < minimumDistance && //can't move in a direction that is farther away from target
                    getModel().isEmpty(testPosition) && //can't move in a direction where there is a wall
                    (!testPosition.equals(getModel().getGhostGate().getPosition()) || ghost.isInsideGate() || ghost.isDead())) //can't move to the ghost gate, unless the ghost is inside
            {
                minimumDistance = tempDistance;
                nextDirection = direction;
            }
        }
        return nextDirection;
    }

    @Override
    public void step(Game game, List<GUI.ACTION> actions, long time) {
        if (frameCount == 450 || frameCount == 2700 || frameCount == 3200)
            //toggle between chase and scatter modes
            getModel().getGhosts().forEach(ghost -> {
                if (!ghost.getPosition().equals(getModel().getGhostGate().getPosition())) ghost.invertDirection();
            });

        //change the target pacman from time to time on multiplayer
        if (frameCount % 2000 == 0 && getModel().getPacmans().size() > 1 && !getModel().getPacmans().get((targetPacman == 0 ? 1 : 0)).isDying())
            targetPacman = (targetPacman == 0 ? 1 : 0);


        for (Ghost ghost : getModel().getGhosts()) {//move all ghosts
            if (time % ghost.getSpeed() != 1) moveGhost(ghost);
        }

        ++frameCount;
    }
}
