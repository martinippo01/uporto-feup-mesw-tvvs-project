package pt.feup.tvvs.pacman.viewer.menu;

import com.googlecode.lanterna.TextColor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.menu.PauseMenu;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PauseMenuViewerWhiteBoxTests {

    @Test
    public void drawElements_drawsOptionsPauseSignAndTitle_andRefreshes() throws Exception {
        GUI gui = mock(GUI.class);

        PauseMenu menu = new PauseMenu(null, GUI.SCREEN_RESOLUTION._720p, 0.5f);
        PauseMenuViewer viewer = new PauseMenuViewer();

        viewer.drawElement(gui, menu, 0L);

        // clear and refresh
        verify(gui, times(1)).clear();
        verify(gui, times(1)).refresh();

        // pause sign is at (155,41) and title at (145,30) — both drawn via drawCharacter
        verify(gui, atLeastOnce()).drawCharacter(eq(new Position(155, 41)), any(java.awt.image.BufferedImage.class), any(TextColor.class));
        verify(gui, atLeastOnce()).drawCharacter(eq(new Position(145, 30)), any(java.awt.image.BufferedImage.class), any(TextColor.class));

        // options are drawn via drawCharacter as well
        verify(gui, atLeastOnce()).drawCharacter(any(Position.class), any(java.awt.image.BufferedImage.class), any(TextColor.class));
    }

    @Test
    public void drawElements_refreshThrowsIOException_isWrappedInRuntimeException() throws Exception {
        GUI gui = mock(GUI.class);
        doThrow(new java.io.IOException("boom")).when(gui).refresh();

        PauseMenu menu = new PauseMenu(null, GUI.SCREEN_RESOLUTION._720p, 0.5f);
        PauseMenuViewer viewer = new PauseMenuViewer();

        assertThatThrownBy(() -> viewer.drawElement(gui, menu, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(java.io.IOException.class);

        verify(gui, times(1)).clear();
    }
}
