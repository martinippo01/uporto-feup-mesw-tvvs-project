package pt.feup.tvvs.pacman.viewer.menu;

import com.googlecode.lanterna.TextColor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.menu.MainMenu;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MainMenuViewerWhiteBoxTests {

    @Test
    public void drawElements_erasesBlankAndDrawsAllElements_andRefreshes() throws Exception {
        GUI gui = mock(GUI.class);

        MainMenu menu = new MainMenu(GUI.SCREEN_RESOLUTION._720p, 0.5f);
        MainMenuViewer viewer = new MainMenuViewer();

        // call draw
        viewer.drawElement(gui, menu, 0L);

        // verify initial clear and final refresh
        verify(gui, times(1)).clear();
        verify(gui, times(1)).refresh();

        // verify at least one blank position was erased (positions are scaled by 11)
        // pick a known blank from MainMenu.createBlankPosition: (3,4) should be present
        verify(gui, atLeastOnce()).erase(new Position(3 * 11, 4 * 11));

        // verify that characters/images were drawn for options/title/pacman/ghosts (calls to drawCharacter/drawImage happen)
        verify(gui, atLeastOnce()).drawCharacter(any(Position.class), any(java.awt.image.BufferedImage.class), any(TextColor.class));
        verify(gui, atLeastOnce()).drawImage(any(Position.class), any(java.awt.image.BufferedImage.class));
    }

    @Test
    public void drawElements_refreshThrowsIOException_isWrappedInRuntimeException() throws Exception {
        GUI gui = mock(GUI.class);
        doThrow(new java.io.IOException("boom")).when(gui).refresh();

        MainMenu menu = new MainMenu(GUI.SCREEN_RESOLUTION._720p, 0.5f);
        MainMenuViewer viewer = new MainMenuViewer();

        assertThatThrownBy(() -> viewer.drawElement(gui, menu, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(java.io.IOException.class);

        verify(gui, times(1)).clear();
    }
}
