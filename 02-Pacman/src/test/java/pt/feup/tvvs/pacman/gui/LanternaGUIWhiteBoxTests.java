package pt.feup.tvvs.pacman.gui;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.BasicTextImage;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import org.junit.jupiter.api.Test;
import pt.feup.tvvs.pacman.model.Position;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class LanternaGUIWhiteBoxTests {

    @Test
    public void getNextAction_maps_keystrokes_to_actions() throws IOException {
        Screen screen = mock(Screen.class);
        TextGraphics tg = mock(TextGraphics.class);
        when(screen.newTextGraphics()).thenReturn(tg);

        KeyStroke ksUp = mock(KeyStroke.class);
        when(ksUp.getKeyType()).thenReturn(KeyType.ArrowUp);
        KeyStroke ksRight = mock(KeyStroke.class);
        when(ksRight.getKeyType()).thenReturn(KeyType.ArrowRight);
        KeyStroke ksDown = mock(KeyStroke.class);
        when(ksDown.getKeyType()).thenReturn(KeyType.ArrowDown);
        KeyStroke ksLeft = mock(KeyStroke.class);
        when(ksLeft.getKeyType()).thenReturn(KeyType.ArrowLeft);
        KeyStroke ksEnter = mock(KeyStroke.class);
        when(ksEnter.getKeyType()).thenReturn(KeyType.Enter);
        KeyStroke ksQ = mock(KeyStroke.class);
        when(ksQ.getKeyType()).thenReturn(KeyType.Character);
        when(ksQ.getCharacter()).thenReturn('q');
        KeyStroke ksEscape = mock(KeyStroke.class);
        when(ksEscape.getKeyType()).thenReturn(KeyType.Escape);

        when(screen.pollInput()).thenReturn(ksUp, ksRight, ksDown, ksLeft, ksEnter, ksQ, ksEscape, null);

        LanternaGUI gui = new LanternaGUI(screen, GUI.SCREEN_RESOLUTION._900p);

        List<GUI.ACTION> actions = gui.getNextAction();

        assertThat(actions).containsExactly(GUI.ACTION.UP, GUI.ACTION.RIGHT, GUI.ACTION.DOWN, GUI.ACTION.LEFT, GUI.ACTION.SELECT, GUI.ACTION.QUIT, GUI.ACTION.QUIT);
    }

    @Test
    public void getNextAction_maps_wasd_to_actions() throws IOException {
        Screen screen = mock(Screen.class);
        TextGraphics tg = mock(TextGraphics.class);
        when(screen.newTextGraphics()).thenReturn(tg);

        KeyStroke ksw = mock(KeyStroke.class);
        when(ksw.getKeyType()).thenReturn(KeyType.Character);
        when(ksw.getCharacter()).thenReturn('w');
        KeyStroke ksa = mock(KeyStroke.class);
        when(ksa.getKeyType()).thenReturn(KeyType.Character);
        when(ksa.getCharacter()).thenReturn('a');
        KeyStroke kss = mock(KeyStroke.class);
        when(kss.getKeyType()).thenReturn(KeyType.Character);
        when(kss.getCharacter()).thenReturn('s');
        KeyStroke ksd = mock(KeyStroke.class);
        when(ksd.getKeyType()).thenReturn(KeyType.Character);
        when(ksd.getCharacter()).thenReturn('d');
        when(screen.pollInput()).thenReturn(ksw, ksa, kss, ksd, null);

        LanternaGUI gui = new LanternaGUI(screen, GUI.SCREEN_RESOLUTION._720p);
        List<GUI.ACTION> actions = gui.getNextAction();
        assertThat(actions).containsExactly(GUI.ACTION.W, GUI.ACTION.A, GUI.ACTION.S, GUI.ACTION.D);
    }

    @Test
    public void drawImage_basicTextImage_delegates_to_textgraphics() {
        Screen screen = mock(Screen.class);
        TextGraphics tg = mock(TextGraphics.class);
        when(screen.newTextGraphics()).thenReturn(tg);
        LanternaGUI gui = new LanternaGUI(screen, GUI.SCREEN_RESOLUTION._720p);

        BasicTextImage image = mock(BasicTextImage.class);
        Position pos = new Position(2, 3);

        gui.drawImage(pos, image);

        verify(tg).drawImage(eq(pos.toTerminalPosition()), eq(image));
    }

    @Test
    public void drawImage_bufferedImage_paints_nonzero_pixels_and_sets_background_and_char() {
        Screen screen = mock(Screen.class);
        TextGraphics tg = mock(TextGraphics.class);
        when(screen.newTextGraphics()).thenReturn(tg);
        LanternaGUI gui = new LanternaGUI(screen, GUI.SCREEN_RESOLUTION._720p);

        BufferedImage img = new BufferedImage(11, 11, BufferedImage.TYPE_INT_RGB);
        // set two non-zero pixels with known colors
        img.setRGB(1, 1, (255 << 16) | (128 << 8) | 64); // red=255, green=128, blue=64
        img.setRGB(3, 4, (10 << 16) | (20 << 8) | 30);

        Position pos = new Position(5, 6);
        gui.drawImage(pos, img);

        // should set background color and character for each non-zero pixel
        verify(tg, atLeastOnce()).setBackgroundColor(any(TextColor.RGB.class));
        verify(tg).setCharacter(5 + 1, 6 + 1, ' ');
        verify(tg).setCharacter(5 + 3, 6 + 4, ' ');
    }

    @Test
    public void drawImage_with_width_height_respects_given_dimensions() {
        Screen screen = mock(Screen.class);
        TextGraphics tg = mock(TextGraphics.class);
        when(screen.newTextGraphics()).thenReturn(tg);
        LanternaGUI gui = new LanternaGUI(screen, GUI.SCREEN_RESOLUTION._720p);

        BufferedImage img = new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, (1 << 16));
        img.setRGB(2, 1, (2 << 8));

        Position pos = new Position(0, 0);
        gui.drawImage(pos, img, 3, 2); // only iterate x<3, y<2

        verify(tg).setCharacter(0 + 0, 0 + 0, ' ');
        // (2,1) is within width=3 and height=2 -> y=1<x height, so should be painted
        verify(tg).setCharacter(0 + 2, 0 + 1, ' ');
    }

    @Test
    public void drawCharacter_paints_character_pixels_and_sets_background_color() {
        Screen screen = mock(Screen.class);
        TextGraphics tg = mock(TextGraphics.class);
        when(screen.newTextGraphics()).thenReturn(tg);
        LanternaGUI gui = new LanternaGUI(screen, GUI.SCREEN_RESOLUTION._900p);

        BufferedImage ch = new BufferedImage(5, 11, BufferedImage.TYPE_INT_RGB);
        ch.setRGB(0, 0, (123 << 16) | (45 << 8) | 67);
        ch.setRGB(4, 10, (1 << 16) | (2 << 8) | 3);

        Position pos = new Position(2, 2);
        gui.drawCharacter(pos, ch, new TextColor.RGB(10, 20, 30));

        verify(tg).setBackgroundColor(eq(new TextColor.RGB(10, 20, 30)));
        verify(tg).setCharacter(2 + 0, 2 + 0, ' ');
        verify(tg).setCharacter(2 + 4, 2 + 10, ' ');
    }

    @Test
    public void erase_clears_rectangle_and_clear_refresh_close_delegate() throws IOException {
        Screen screen = mock(Screen.class);
        TextGraphics tg = mock(TextGraphics.class);
        when(screen.newTextGraphics()).thenReturn(tg);
        LanternaGUI gui = new LanternaGUI(screen, GUI.SCREEN_RESOLUTION._720p);

        Position pos = new Position(7, 8);
        gui.erase(pos);
        verify(tg).setBackgroundColor(eq(new TextColor.RGB(0, 0, 0)));
        // fillRectangle called with position and TerminalSize(11,11)
        verify(tg).fillRectangle(eq(pos.toTerminalPosition()), any(), eq(' '));

        gui.clear();
        verify(screen).clear();

        gui.refresh();
        verify(screen).refresh();

        gui.close();
        verify(screen).close();
    }

    @Test
    public void getResolution_returns_constructed_resolution() {
        Screen screen = mock(Screen.class);
        LanternaGUI gui = new LanternaGUI(screen, GUI.SCREEN_RESOLUTION._1080p);
        assertThat(gui.getResolution()).isEqualTo(GUI.SCREEN_RESOLUTION._1080p);
    }

    @Test
    public void resolutionToFontSize_private_method_covers_all_enum_cases() throws Exception {
        Screen screen = mock(Screen.class);
        LanternaGUI gui = new LanternaGUI(screen, GUI.SCREEN_RESOLUTION._360p);

        java.lang.reflect.Method m = LanternaGUI.class.getDeclaredMethod("resolutionToFontSize", GUI.SCREEN_RESOLUTION.class);
        m.setAccessible(true);

        assertThat((int) m.invoke(gui, GUI.SCREEN_RESOLUTION._360p)).isEqualTo(2);
        assertThat((int) m.invoke(gui, GUI.SCREEN_RESOLUTION._540p)).isEqualTo(3);
        assertThat((int) m.invoke(gui, GUI.SCREEN_RESOLUTION._720p)).isEqualTo(4);
        assertThat((int) m.invoke(gui, GUI.SCREEN_RESOLUTION._900p)).isEqualTo(5);
        assertThat((int) m.invoke(gui, GUI.SCREEN_RESOLUTION._1080p)).isEqualTo(6);
        assertThat((int) m.invoke(gui, GUI.SCREEN_RESOLUTION._1440p)).isEqualTo(8);
        assertThat((int) m.invoke(gui, GUI.SCREEN_RESOLUTION._2160p)).isEqualTo(12);
    }

    @Test
    public void int_constructor_either_constructs_or_throws_expected_exceptions() {
        try {
            LanternaGUI gui = new LanternaGUI(40, 20, GUI.SCREEN_RESOLUTION._360p);
            // if construction succeeds, ensure resolution set
            assertThat(gui.getResolution()).isEqualTo(GUI.SCREEN_RESOLUTION._360p);
        } catch (Exception e) {
            // acceptable exceptions: IOException, FontFormatException, URISyntaxException
            assertThat(e).isInstanceOfAny(IOException.class, java.awt.FontFormatException.class, java.net.URISyntaxException.class);
        }
    }

    @Test
    public void getNextAction_maps_eof_to_quit() throws IOException {
        Screen screen = mock(Screen.class);
        TextGraphics tg = mock(TextGraphics.class);
        when(screen.newTextGraphics()).thenReturn(tg);

        KeyStroke ksEof = mock(KeyStroke.class);
        when(ksEof.getKeyType()).thenReturn(KeyType.EOF);
        when(screen.pollInput()).thenReturn(ksEof, null);

        LanternaGUI gui = new LanternaGUI(screen, GUI.SCREEN_RESOLUTION._720p);
        List<GUI.ACTION> actions = gui.getNextAction();
        assertThat(actions).containsExactly(GUI.ACTION.QUIT);
    }

    @Test
    public void screenResolution_toString_allValues() {
        for (GUI.SCREEN_RESOLUTION r : GUI.SCREEN_RESOLUTION.values()) {
            String s = r.toString();
            // ensure mapping matches expected patterns (e.g., _360p -> "360p")
            assertThat(s).isNotNull();
            assertThat(s).endsWith("p");
            assertThat(s).matches("\\d+p");
        }
    }

    @Test
    public void action_enum_contains_expected_values() {
        GUI.ACTION[] actions = GUI.ACTION.values();
        assertThat(actions).extracting(Enum::name).contains("UP", "DOWN", "LEFT", "RIGHT", "QUIT", "SELECT", "W", "A", "S", "D");
    }
}
