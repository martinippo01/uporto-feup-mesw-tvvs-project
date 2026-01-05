package pt.feup.tvvs.pacman.viewer.menu;

import com.googlecode.lanterna.TextColor;
import org.junit.jupiter.api.Test;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.Wall;
import pt.feup.tvvs.pacman.model.game.element.collectibles.Coin;
import pt.feup.tvvs.pacman.model.game.element.ghost.Blinky;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;
import pt.feup.tvvs.pacman.model.menu.AlertMenu;

import java.awt.image.BufferedImage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class AlertMenuViewerMutationTests {

    @Test
    public void drawElements_draws_score_and_lives_and_options_singlePacman() throws Exception {
        GUI gui = mock(GUI.class);
        doNothing().when(gui).refresh();
        doNothing().when(gui).erase(any(Position.class));
        doNothing().when(gui).drawCharacter(any(Position.class), any(BufferedImage.class), any(TextColor.class));
        doNothing().when(gui).drawImage(any(Position.class), any(BufferedImage.class), anyInt(), anyInt());

        Arena arena = new Arena(20, 15);
        arena.addBlankPosition(new Position(2, 3));
        Wall wall = new Wall(new Position(1,1));
        arena.addWall(wall);
        Coin coin = new Coin(new Position(4,4));
        arena.addCollectible(coin);
        Blinky blinky = new Blinky(new Position(5,5));
        arena.addGhost(blinky);
        Pacman p = new Pacman(new Position(6,6));
        p.setLife(3);
        arena.addPacman(p);
        arena.setScore(77L);

        AlertMenu menu = new AlertMenu(arena, "PNGs/gameover.png");

        AlertMenuViewer viewer = new AlertMenuViewer(menu.getAlertFilePath());

        // call drawElement which internally draws TextBoxes (Score, Lives, Options)
        viewer.drawElement(gui, menu, 0L);

        // Score TextBox is created at Position(11,0) -> first drawCharacter call for that box uses x==11
        verify(gui, atLeastOnce()).drawCharacter(argThat(pos -> pos.getX() == 11 && pos.getY() == 0), any(BufferedImage.class), any(TextColor.class));

        // For single pacman, Lives textbox is at Position(274,0)
        verify(gui, atLeastOnce()).drawCharacter(argThat(pos -> pos.getX() == 274 && pos.getY() == 0), any(BufferedImage.class), any(TextColor.class));

        // Options are drawn via drawCharacter as well; verify at least one drawCharacter happened (whitebox already covers content elsewhere)
        verify(gui, atLeastOnce()).drawCharacter(any(Position.class), any(BufferedImage.class), any(TextColor.class));

        // Alert image should be drawn via drawImage
        verify(gui, atLeastOnce()).drawImage(any(Position.class), any(BufferedImage.class), anyInt(), anyInt());
    }

    @Test
    public void drawElements_draws_two_lives_textboxes_for_twoPacmans() throws Exception {
        GUI gui = mock(GUI.class);
        doNothing().when(gui).refresh();
        doNothing().when(gui).drawCharacter(any(Position.class), any(BufferedImage.class), any(TextColor.class));
        doNothing().when(gui).drawImage(any(Position.class), any(BufferedImage.class), anyInt(), anyInt());

        Arena arena = new Arena(20, 15);
        Pacman p1 = new Pacman(new Position(1, 1)); p1.setLife(2);
        Pacman p2 = new Pacman(new Position(2, 2)); p2.setLife(1);
        arena.addPacman(p1);
        arena.addPacman(p2);

        AlertMenu menu = new AlertMenu(arena, "PNGs/gameover.png");
        AlertMenuViewer viewer = new AlertMenuViewer(menu.getAlertFilePath());

        viewer.drawElement(gui, menu, 10L);

        // When 2 pacmans, the two lives textboxes are created at x=199 and x=259
        verify(gui, atLeastOnce()).drawCharacter(argThat(pos -> pos.getX() == 199 && pos.getY() == 0), any(BufferedImage.class), any(TextColor.class));
        verify(gui, atLeastOnce()).drawCharacter(argThat(pos -> pos.getX() == 259 && pos.getY() == 0), any(BufferedImage.class), any(TextColor.class));

        // Ensure alert image drawn as well
        verify(gui, atLeastOnce()).drawImage(any(Position.class), any(BufferedImage.class), anyInt(), anyInt());
    }
}
