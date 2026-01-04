package pt.feup.tvvs.pacman.viewer.game;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.BasicTextImage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.ghost.Blinky;
import pt.feup.tvvs.pacman.model.game.element.Wall;
import pt.feup.tvvs.pacman.model.game.element.collectibles.Coin;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ArenaViewerWhiteBoxTests {

    @Test
    public void drawElements_singlePacman_erasesAndDrawsAndRefreshes() throws Exception {
        GUI gui = mock(GUI.class);

        Arena arena = new Arena(10, 10);
        // add blank position that should be erased (multiplied by 11 inside ArenaViewer)
        arena.addBlankPosition(new Position(2, 3));

        // add elements that will be drawn
        arena.addWall(new Wall(new Position(1, 1)));
        arena.addCollectible(new Coin(new Position(4, 4)));
        arena.addGhost(new Blinky(new Position(5, 5)));
        Pacman p = new Pacman(new Position(6, 6));
        p.setLife(5);
        arena.addPacman(p);
        arena.setScore(123L);

        ArenaViewer viewer = new ArenaViewer();

        // call draw (this invokes clear on first call and refresh)
        viewer.drawElement(gui, arena, 0L);

        // verify clear and refresh called
        verify(gui, times(1)).clear();
        verify(gui, times(1)).refresh();

        // verify blank position erase (position multiplied by 11)
        verify(gui, times(1)).erase(new Position(2 * 11, 3 * 11));

        // viewers will call drawImage/drawCharacter at least once for the elements and textboxes
        verify(gui, atLeast(1)).drawImage(any(Position.class), any(BasicTextImage.class));
        verify(gui, atLeast(1)).drawImage(any(Position.class), any(BufferedImage.class));
        verify(gui, atLeast(1)).drawCharacter(any(Position.class), any(BufferedImage.class), any(TextColor.class));
    }

    @Test
    public void drawElements_twoPacmans_drawsTwoLivesBoxes() throws Exception {
        GUI gui = mock(GUI.class);

        Arena arena = new Arena(10, 10);

        Pacman p1 = new Pacman(new Position(1, 1));
        p1.setLife(3);
        Pacman p2 = new Pacman(new Position(2, 2));
        p2.setLife(1);
        arena.addPacman(p1);
        arena.addPacman(p2);

        ArenaViewer viewer = new ArenaViewer();

        viewer.drawElement(gui, arena, 42L);

        // clear and refresh called once
        verify(gui, times(1)).clear();
        verify(gui, times(1)).refresh();

        // since there are two pacmans the viewer draws two separate lives textboxes -> drawCharacter called
        verify(gui, atLeast(1)).drawCharacter(any(Position.class), any(BufferedImage.class), any(TextColor.class));
    }

    @Test
    public void drawElement_refreshThrowsIOException_isWrappedInRuntimeException() throws Exception {
        GUI gui = mock(GUI.class);
        doThrow(new IOException("boom")).when(gui).refresh();

        Arena arena = new Arena(5, 5);
        Pacman p = new Pacman(new Position(0, 0));
        arena.addPacman(p);

        ArenaViewer viewer = new ArenaViewer();

        assertThatThrownBy(() -> viewer.drawElement(gui, arena, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IOException.class);

        // even when refresh throws, clear should have been called before
        verify(gui, times(1)).clear();
    }
}
