package pt.feup.tvvs.pacman;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.ArenaLoader;
import pt.feup.tvvs.pacman.model.game.element.Wall;
import pt.feup.tvvs.pacman.model.game.element.collectibles.*;
import pt.feup.tvvs.pacman.model.game.element.ghost.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

public class PBTests {

    private static final char[] ALLOWED = new char[]{'W','P','o','O','A','C','K','S','p','i','c','b','D','u',' ','X','?'};

    @Provide
    Arbitrary<List<String>> maps() {
        // Generate between 1 and 10 rows, each row length between 0 and 10 using the allowed characters
        Arbitrary<String> rowArb = Arbitraries.strings()
                .ofMinLength(0)
                .ofMaxLength(10)
                .withChars(ALLOWED);
        return rowArb.list().ofMinSize(1).ofMaxSize(10);
    }

    @Property(tries = 200)
    void arenaLoader_parses_map(@ForAll("maps") List<String> map) throws IOException {
        final int WIDTH = 10;
        final int HEIGHT = 10;
        Arena arena = new Arena(WIDTH, HEIGHT);

        // write map to temp file
        Path tmp = Files.createTempFile("arena-map-", ".txt");
        Files.write(tmp, map, StandardCharsets.UTF_8);

        ArenaLoader loader = new ArenaLoader(arena);
        loader.loadMap(tmp.toString());

        // compute expected pacman count (loader stops after adding 2 pacmans)
        int expectedPCount = 0;
        int lastD_x = -1, lastD_y = -1;

        for (int r = 0; r < Math.min(map.size(), HEIGHT); r++) {
            String line = map.get(r);
            for (int c = 0; c < Math.min(line.length(), WIDTH); c++) {
                char ch = line.charAt(c);
                Position pos = new Position(c, r);
                switch (ch) {
                    case 'W':
                        assertThat(arena.getWalls()).contains(new Wall(pos));
                        break;
                    case 'P':
                        expectedPCount = Math.min(2, expectedPCount + 1);
                        // after load, pacman list should contain a pacman at this position (unless >2)
                        break;
                    case 'o':
                        assertThat(collectibleAt(arena.getCollectibles(), pos)).isPresent().get()
                                .isInstanceOf(Coin.class);
                        break;
                    case 'O':
                        assertThat(collectibleAt(arena.getCollectibles(), pos)).isPresent().get()
                                .isInstanceOf(Orange.class);
                        break;
                    case 'A':
                        assertThat(collectibleAt(arena.getCollectibles(), pos)).isPresent().get()
                                .isInstanceOf(Apple.class);
                        break;
                    case 'C':
                        assertThat(collectibleAt(arena.getCollectibles(), pos)).isPresent().get()
                                .isInstanceOf(Cherry.class);
                        break;
                    case 'K':
                        assertThat(collectibleAt(arena.getCollectibles(), pos)).isPresent().get()
                                .isInstanceOf(Key.class);
                        break;
                    case 'S':
                        assertThat(collectibleAt(arena.getCollectibles(), pos)).isPresent().get()
                                .isInstanceOf(Strawberry.class);
                        break;
                    case 'p':
                        assertThat(arena.getGhosts()).contains(new Pinky(pos));
                        break;
                    case 'i':
                        assertThat(arena.getGhosts()).contains(new Inky(pos));
                        break;
                    case 'c':
                        assertThat(arena.getGhosts()).contains(new Clyde(pos));
                        break;
                    case 'b':
                        assertThat(arena.getGhosts()).contains(new Blinky(pos));
                        break;
                    case 'D':
                        lastD_x = c;
                        lastD_y = r;
                        break;
                    case 'u':
                        assertThat(collectibleAt(arena.getCollectibles(), pos)).isPresent().get()
                                .isInstanceOf(PowerUp.class);
                        break;
                    case ' ': // blank
                        assertThat(arena.getBlankPositions()).contains(pos);
                        break;
                    default:
                        // unknown characters should add a blank position
                        assertThat(arena.getBlankPositions()).contains(pos);
                        break;
                }
            }
        }

        // Validate pacman count and positions: get positions of pacmans present
        int actualPacmans = arena.getPacmans().size();
        assertThat(actualPacmans).isLessThanOrEqualTo(2);
        // To compute number of P chars in processed area, count directly
        int foundP = 0;
        for (int r = 0; r < Math.min(map.size(), HEIGHT); r++) {
            String line = map.get(r);
            for (int c = 0; c < Math.min(line.length(), WIDTH); c++) {
                if (line.charAt(c) == 'P') foundP++;
            }
        }
        int expectedByMap = Math.min(2, foundP);
        assertThat(actualPacmans).isEqualTo(expectedByMap);

        // If there was any 'D' in the processed area, ghost gate should be set to last one
        if (lastD_x >= 0) {
            Position gatePos = arena.getGhostGate().getPosition();
            assertThat(gatePos.getX()).isEqualTo(lastD_x);
            assertThat(gatePos.getY()).isEqualTo(lastD_y);
        }

        // All collectibles in the arena should have positions within bounds
        for (Collectible col : arena.getCollectibles()) {
            Position p = col.getPosition();
            assertThat(p.getX()).isBetween(0, WIDTH - 1);
            assertThat(p.getY()).isBetween(0, HEIGHT - 1);
            // values are positive
            assertThat(col.getValue()).isGreaterThan(0);
        }

        // Walls should also be within bounds
        for (Wall w : arena.getWalls()) {
            Position p = w.getPosition();
            assertThat(p.getX()).isBetween(0, WIDTH - 1);
            assertThat(p.getY()).isBetween(0, HEIGHT - 1);
        }

        // Ghosts should be within bounds
        for (Ghost g : arena.getGhosts()) {
            Position p = g.getPosition();
            assertThat(p.getX()).isBetween(0, WIDTH - 1);
            assertThat(p.getY()).isBetween(0, HEIGHT - 1);
        }

        // Clean up temp file
        try {
            Files.deleteIfExists(tmp);
        } catch (IOException ignored) {
        }
    }

    private Optional<Collectible> collectibleAt(Set<Collectible> set, Position pos) {
        return set.stream().filter(c -> c.getPosition().equals(pos)).findFirst();
    }
}
