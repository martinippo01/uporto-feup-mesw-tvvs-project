package pt.feup.tvvs.pacman.controller.game.element;

import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.controller.game.GameController;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.Direction;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;

import java.util.Arrays;
import java.util.List;


public class PacmanController extends GameController {
    private final List<Direction> desiredDirections; //one for each pacman

    public PacmanController(Arena arena) {
        super(arena);
        this.desiredDirections = Arrays.asList(null, null);
    }

    private void movePacman(Pacman pacman, Direction desiredDirection) {
        if (desiredDirection != null && desiredDirection.isOpposite(pacman.getDirection())) {
            //pacman can invert direction whenever
            pacman.invertDirection();
            desiredDirection = null;
        }

        if (pacman.getCounter() > 0) {
            pacman.incrementCounter();
            return;
        }

        if (desiredDirection != null) { //try to go in the desired direction
            Position nextDesiredPosition = calculateNextPosition(pacman.getPosition(), desiredDirection);

            boolean isPositionValid = getModel().isEmpty(nextDesiredPosition) &&
                    getModel().getPacmans().stream()
                            .filter(other -> !other.isDying()) // Ignore the current Pacman
                            .noneMatch(other -> other.collidingWith(new Pacman(nextDesiredPosition)));

            if (isPositionValid &&
                    !getModel().getGhostGate().getPosition().equals(nextDesiredPosition)) {
                pacman.setDirection(desiredDirection);
                pacman.incrementCounter();
                return;
            }
        }

        //if the desired direction was invalid, try to go the in the current direction
        Position nextPosition = pacman.getNextPosition();

        // Ensure the next position is valid for movement
        if (getModel().isEmpty(nextPosition) &&
                !getModel().getGhostGate().getPosition().equals(nextPosition) &&
                getModel().getPacmans().stream()
                        .filter(other -> !other.isDying()) // Ignore the dead pacmans
                        .noneMatch(other -> other.collidingWith(new Pacman(nextPosition)))) {
            pacman.incrementCounter();
        }
    }


    private Position calculateNextPosition(Position position, Direction direction) {
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

    @Override
    @SuppressWarnings("MissingCasesInEnumSwitch")
    public void step(Game game, List<GUI.ACTION> actions, long time) {
        for (GUI.ACTION action : actions) {
            switch (action) {
                case UP:
                    desiredDirections.set(0, Direction.UP);
                    break;
                case DOWN:
                    desiredDirections.set(0, Direction.DOWN);
                    break;
                case LEFT:
                    desiredDirections.set(0, Direction.LEFT);
                    break;
                case RIGHT:
                    desiredDirections.set(0, Direction.RIGHT);
                    break;
                case W:
                    desiredDirections.set(1, Direction.UP);
                    break;
                case A:
                    desiredDirections.set(1, Direction.LEFT);
                    break;
                case S:
                    desiredDirections.set(1, Direction.DOWN);
                    break;
                case D:
                    desiredDirections.set(1, Direction.RIGHT);
                    break;

                case NONE:
                    break;
            }
        }
        for (int i = 0; i < getModel().getPacmans().size(); ++i) {
            Pacman pacman = getModel().getPacmans().get(i);
            if (time % pacman.getSpeed() != 1 && !pacman.isDying()) movePacman(pacman, desiredDirections.get(i));
        }
    }
}