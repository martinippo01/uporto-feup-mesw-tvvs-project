package pt.feup.tvvs.pacman.viewer.menu;

import com.googlecode.lanterna.TextColor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.menu.MapSelectionMenu;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MapSelectionMenuViewerWhiteBoxTests {

    @Test
    public void drawElements_withMaps_drawsTitleAndOptions_andRefreshes() throws Exception {
        GUI gui = mock(GUI.class);

        MapSelectionMenu menu = new MapSelectionMenu("singleplayer");
        MapSelectionMenuViewer viewer = new MapSelectionMenuViewer();

        viewer.drawElement(gui, menu, 0L);

        verify(gui, times(1)).clear();
        verify(gui, times(1)).refresh();

        // title should be drawn starting at Position(128, 30)
        verify(gui, atLeastOnce()).drawCharacter(eq(new Position(128, 30)), any(BufferedImage.class), any(TextColor.class));

        // options for maps should be drawn via drawCharacter as well
        verify(gui, atLeastOnce()).drawCharacter(any(Position.class), any(BufferedImage.class), any(TextColor.class));
    }

    @Test
    public void drawElements_refreshThrowsIOException_isWrappedInRuntimeException() throws Exception {
        GUI gui = mock(GUI.class);
        doThrow(new java.io.IOException("boom")).when(gui).refresh();

        MapSelectionMenu menu = new MapSelectionMenu("singleplayer");
        MapSelectionMenuViewer viewer = new MapSelectionMenuViewer();

        assertThatThrownBy(() -> viewer.drawElement(gui, menu, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(java.io.IOException.class);

        verify(gui, times(1)).clear();
    }
}
