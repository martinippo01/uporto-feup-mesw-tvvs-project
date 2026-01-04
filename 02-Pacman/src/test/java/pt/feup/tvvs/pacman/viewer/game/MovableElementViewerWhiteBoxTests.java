package pt.feup.tvvs.pacman.viewer.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.element.Wall;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;
import pt.feup.tvvs.pacman.viewer.game.strategies.MultipleElementStrategy;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovableElementViewerWhiteBoxTests {

    @Test
    public void drawElement_usesStrategyAndDrawsAtRealPosition() {
        GUI gui = mock(GUI.class);
        MultipleElementStrategy strategy = mock(MultipleElementStrategy.class);

        BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        when(strategy.getCurrentImage(any(), any(), eq(5L))).thenReturn(img);

        MovableElementViewer viewer = new MovableElementViewer(strategy, Map.of());

        Pacman pacman = new Pacman(new Position(2, 3));
        pacman.setDirection(pt.feup.tvvs.pacman.model.game.element.Direction.RIGHT);
        pacman.setCounter(4);

        // expected real position: x*11 + counterX (RIGHT -> +counter), y*11
        Position expected = new Position(2 * 11 + 4, 3 * 11);

        viewer.drawElement(gui, pacman, 5L);

        verify(strategy, times(1)).getCurrentImage(pacman, Map.of(), 5L);
        verify(gui, times(1)).drawImage(expected, img);
    }

    @Test
    public void drawElement_withNonMovableElement_throwsClassCastException() {
        GUI gui = mock(GUI.class);
        MultipleElementStrategy strategy = mock(MultipleElementStrategy.class);
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        when(strategy.getCurrentImage(any(), any(), anyLong())).thenReturn(img);

        MovableElementViewer viewer = new MovableElementViewer(strategy, Map.of());
        Wall wall = new Wall(new Position(1, 1));

        assertThatThrownBy(() -> viewer.drawElement(gui, wall, 0L)).isInstanceOf(ClassCastException.class);
    }

    @Test
    public void drawElement_strategyThrows_runtimeExceptionPropagates() {
        GUI gui = mock(GUI.class);
        MultipleElementStrategy strategy = mock(MultipleElementStrategy.class);
        when(strategy.getCurrentImage(any(), any(), anyLong())).thenThrow(new RuntimeException("boom"));

        MovableElementViewer viewer = new MovableElementViewer(strategy, Map.of());
        Pacman pacman = new Pacman(new Position(0, 0));

        assertThatThrownBy(() -> viewer.drawElement(gui, pacman, 10L)).hasMessageContaining("boom");
    }
}
