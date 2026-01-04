package pt.feup.tvvs.pacman.viewer.menu;

import com.googlecode.lanterna.TextColor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.Wall;
import pt.feup.tvvs.pacman.model.game.element.ghost.Blinky;
import pt.feup.tvvs.pacman.model.game.element.collectibles.Coin;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;
import pt.feup.tvvs.pacman.model.menu.AlertMenu;
import pt.feup.tvvs.pacman.viewer.game.ImageLoader;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlertMenuViewerWhiteBoxTests {

    @Test
    public void drawElements_singlePacman_erasesAndDrawsAlertAndOptions() throws Exception {
        GUI gui = mock(GUI.class);
        doNothing().when(gui).refresh();

        Arena arena = new Arena(20, 15);
        arena.addBlankPosition(new Position(2, 3));
        arena.addWall(new Wall(new Position(1, 1)));
        arena.addCollectible(new Coin(new Position(4, 4)));
        arena.addGhost(new Blinky(new Position(5, 5)));
        Pacman p = new Pacman(new Position(6, 6));
        p.setLife(3);
        arena.addPacman(p);
        arena.setScore(77L);

        AlertMenu menu = new AlertMenu(arena, "PNGs/gameover.png");
        AlertMenuViewer viewer = new AlertMenuViewer(menu.getAlertFilePath());

        BufferedImage alertImg = ImageLoader.loadBufferedImage("PNGs/gameover.png");
        int expectedX = (arena.getWidth() * 11 - alertImg.getWidth()) / 2;

        viewer.drawElement(gui, menu, 0L);

        // verify erase called for blank position (scaled)
        verify(gui, atLeastOnce()).erase(new Position(2 * 11, 3 * 11));

        // verify alert image draw with width/height
        // capture the buffered image and position used in the drawImage call with width/height
        ArgumentCaptor<Position> posCaptorImg = ArgumentCaptor.forClass(Position.class);
        ArgumentCaptor<BufferedImage> imgCaptor = ArgumentCaptor.forClass(BufferedImage.class);
        verify(gui, atLeastOnce()).drawImage(posCaptorImg.capture(), imgCaptor.capture(), anyInt(), anyInt());
        Position imgPos = posCaptorImg.getValue();
        BufferedImage passedImg = imgCaptor.getValue();
        assertThat(passedImg.getWidth()).isEqualTo(alertImg.getWidth());
        assertThat(passedImg.getHeight()).isEqualTo(alertImg.getHeight());
        assertThat(imgPos.getX()).isEqualTo(expectedX);

        // verify options (textboxes) drawn via drawCharacter
        verify(gui, atLeastOnce()).drawCharacter(any(Position.class), any(BufferedImage.class), any(TextColor.class));
    }

    @Test
    public void drawElements_twoPacmans_drawsTwoLivesTextBoxes() throws Exception {
        GUI gui = mock(GUI.class);
        doNothing().when(gui).refresh();

        Arena arena = new Arena(20, 15);
        Pacman p1 = new Pacman(new Position(1, 1));
        p1.setLife(2);
        Pacman p2 = new Pacman(new Position(2, 2));
        p2.setLife(1);
        arena.addPacman(p1);
        arena.addPacman(p2);

        AlertMenu menu = new AlertMenu(arena, "PNGs/gameover.png");
        AlertMenuViewer viewer = new AlertMenuViewer(menu.getAlertFilePath());

        viewer.drawElement(gui, menu, 10L);

        // when 2 pacmans, two separate lives textboxes should be drawn (drawCharacter used for each)
        verify(gui, atLeast(2)).drawCharacter(any(Position.class), any(BufferedImage.class), any(TextColor.class));
    }

    @Test
    public void constructor_missingImage_throws() {
        assertThatThrownBy(() -> new AlertMenuViewer("PNGs/this_does_not_exist.png"))
                .isInstanceOfAny(AssertionError.class, IOException.class, RuntimeException.class);
    }
}
