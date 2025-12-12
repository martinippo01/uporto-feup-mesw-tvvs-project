package pt.feup.tvvs.pacman.model.game.element;

public enum Direction {
    UP,
    LEFT,
    DOWN,
    RIGHT;

    public boolean isOpposite(Direction other) {
        return (this == UP && other == DOWN) || (this == DOWN && other == UP) || (this == LEFT && other == RIGHT) || (this == RIGHT && other == LEFT);
    }

    public Direction getOpposite() {
        switch (this) {
            case UP:
                return DOWN;
            case DOWN:
                return UP;
            case LEFT:
                return RIGHT;
            case RIGHT:
                return LEFT;
        }
        return null;
    }
}
