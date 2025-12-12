package pt.feup.tvvs.pacman.model.game;

import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.element.Wall;
import pt.feup.tvvs.pacman.model.game.element.collectibles.*;
import pt.feup.tvvs.pacman.model.game.element.ghost.*;
import pt.feup.tvvs.pacman.model.game.element.collectibles.*;
import pt.feup.tvvs.pacman.model.game.element.ghost.*;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ArenaLoader {
    private final Arena arena;
    private final int width;
    private final int height;

    public ArenaLoader(Arena arena) {
        this.arena = arena;
        this.width = arena.getWidth();
        this.height = arena.getHeight();
    }

    public void loadMap(String mapFile) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(mapFile))) {
            String line;
            int row = 0;

            while ((line = reader.readLine()) != null && row < height) {
                for (int col = 0; col < Math.min(line.length(), width); col++) {
                    char currentChar = line.charAt(col);
                    loadElementAt(col, row, currentChar);
                }
                row++;
            }
        }
    }

    private void loadElementAt(int col, int row, char element) {
        Position position = new Position(col, row);

        switch (element) {
            case 'W':
                arena.addWall(new Wall(position));
                break;
            case 'P':
                if (arena.getPacmans().size() == 2) return;
                Pacman pacman = new Pacman(position);
                pacman.setRespawnPosition(new Position(position));
                arena.addPacman(pacman);
                arena.addBlankPosition(new Position(position));
                break;
            case 'o':
                arena.addCollectible(new Coin(position));
                break;
            case 'O':
                arena.addCollectible(new Orange(position));
                break;
            case 'A':
                arena.addCollectible(new Apple(position));
                break;
            case 'C':
                arena.addCollectible(new Cherry(position));
                break;
            case 'K':
                arena.addCollectible(new Key(position));
                break;
            case 'S':
                arena.addCollectible(new Strawberry(position));
                break;
            case 'p':
                Pinky pinky = new Pinky(position);
                pinky.setRespawnPosition(new Position(position));
                arena.addGhost(pinky);
                arena.addBlankPosition(new Position(position));
                break;
            case 'i':
                Ghost inky = new Inky(position);
                inky.setRespawnPosition(new Position(position));
                arena.addGhost(inky);
                arena.addBlankPosition(new Position(position));
                break;
            case 'c':
                Clyde clyde = new Clyde(position);
                clyde.setRespawnPosition(new Position(position));
                arena.addGhost(clyde);
                arena.addBlankPosition(new Position(position));
                break;
            case 'b':
                Blinky blinky = new Blinky(position);
                blinky.setRespawnPosition(new Position(position));
                arena.addGhost(blinky);
                arena.addBlankPosition(new Position(position));
                break;
            case 'D':
                arena.setGhostGatePosition(position);
                break;
            case 'u':
                arena.addCollectible(new PowerUp(position));
                break;
            case ' ':
                arena.addBlankPosition(position);
                break;
            default:
                arena.addBlankPosition(position);
                System.out.println("Unknown element '" + element + "' at (" + position.getX() + ", " + position.getY() + ")");
                break;
        }
    }
}

/*
    Map of the keys to the elements:
    W = wall
    P = pacman
    o = coin
    A = apple
    C = cherry
    K = key
    O = orange
    S = strawberry
    p = pinky
    i = inky
    c = clyde
    b = blinky
    D = ghost door/gate
    u = power up
*/