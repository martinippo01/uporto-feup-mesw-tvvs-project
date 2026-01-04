package pt.feup.tvvs.pacman.viewer.game;

import com.googlecode.lanterna.graphics.BasicTextImage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.element.Wall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ElementViewerWhiteBoxTests {

    @Test
    public void drawElement_scalesPositionAndDrawsImage() {
        GUI gui = mock(GUI.class);

        BasicTextImage image = new BasicTextImage(2, 2);
        ElementViewer viewer = new ElementViewer(image);

        Wall wall = new Wall(new Position(2, 3));

        viewer.drawElement(gui, wall, 0L);

        // Expected scaled position: x*11, y*11
        Position expected = new Position(2 * 11, 3 * 11);
        verify(gui, times(1)).drawImage(expected, image);
    }

    @Test
    public void drawElement_afterPositionChange_usesUpdatedPosition() {
        GUI gui = mock(GUI.class);

        BasicTextImage image = new BasicTextImage(1, 1);
        ElementViewer viewer = new ElementViewer(image);

        Wall wall = new Wall(new Position(0, 0));
        // first draw at 0,0
        viewer.drawElement(gui, wall, 0L);
        verify(gui, times(1)).drawImage(new Position(0, 0), image);

        // change position of element and draw again
        wall.setPosition(new Position(4, 5));
        viewer.drawElement(gui, wall, 1L);

        // verify second call uses updated scaled position
        verify(gui, times(1)).drawImage(new Position(4 * 11, 5 * 11), image);
    }

    @Test
    public void drawElement_withNullImage_callsGuiWithNullImage() {
        GUI gui = mock(GUI.class);

        ElementViewer viewer = new ElementViewer(null);

        Wall wall = new Wall(new Position(1, 1));

        viewer.drawElement(gui, wall, 0L);

        ArgumentCaptor<Position> posCaptor = ArgumentCaptor.forClass(Position.class);
        ArgumentCaptor<BasicTextImage> imgCaptor = ArgumentCaptor.forClass(BasicTextImage.class);

        verify(gui).drawImage(posCaptor.capture(), imgCaptor.capture());

        Position capturedPos = posCaptor.getValue();
        assertThat(capturedPos.getX()).isEqualTo(1 * 11);
        assertThat(capturedPos.getY()).isEqualTo(1 * 11);
        assertThat(imgCaptor.getValue()).isNull();
    }
}
