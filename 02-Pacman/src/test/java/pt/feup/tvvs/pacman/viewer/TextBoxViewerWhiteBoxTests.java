package pt.feup.tvvs.pacman.viewer;

import com.googlecode.lanterna.TextColor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.menu.element.TextBox;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TextBoxViewerWhiteBoxTests {

    @Test
    public void draws_each_available_character_with_correct_position_and_color() {
        Map<Character, BufferedImage> chars = new HashMap<>();
        // prepare simple images for H E L O
        BufferedImage H = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        BufferedImage E = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        BufferedImage L = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        BufferedImage O = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        chars.put('H', H);
        chars.put('E', E);
        chars.put('L', L);
        chars.put('O', O);

        TextBoxViewer viewer = new TextBoxViewer(chars);

        GUI gui = mock(GUI.class);
        TextColor color = new TextColor.RGB(10, 20, 30);
        TextBox box = new TextBox("HeLlo!", new Position(10, 5), color);

        viewer.drawElement(gui, box, 0L);

        ArgumentCaptor<Position> posCap = ArgumentCaptor.forClass(Position.class);
        ArgumentCaptor<BufferedImage> imgCap = ArgumentCaptor.forClass(BufferedImage.class);
        ArgumentCaptor<TextColor> colCap = ArgumentCaptor.forClass(TextColor.class);

        // expected calls: H (index 0)->H, e->E, L->L, l->L, o->O, ! not present -> skip
        verify(gui, times(5)).drawCharacter(posCap.capture(), imgCap.capture(), colCap.capture());

        // verify positions and images in order
        var positions = posCap.getAllValues();
        var images = imgCap.getAllValues();
        var colors = colCap.getAllValues();

        assertThat(positions.get(0)).isEqualTo(new Position(10 + 5 * 0, 5)); // H
        assertThat(images.get(0)).isSameAs(H);
        assertThat(colors.get(0)).isSameAs(color);

        assertThat(positions.get(1)).isEqualTo(new Position(10 + 5 * 1, 5)); // e -> E
        assertThat(images.get(1)).isSameAs(E);

        assertThat(positions.get(2)).isEqualTo(new Position(10 + 5 * 2, 5)); // L
        assertThat(images.get(2)).isSameAs(L);

        assertThat(positions.get(3)).isEqualTo(new Position(10 + 5 * 3, 5)); // l
        assertThat(images.get(3)).isSameAs(L);

        assertThat(positions.get(4)).isEqualTo(new Position(10 + 5 * 4, 5)); // o
        assertThat(images.get(4)).isSameAs(O);
    }

    @Test
    public void skips_characters_not_in_map_and_handles_empty_text() {
        Map<Character, BufferedImage> chars = new HashMap<>();
        BufferedImage A = new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB);
        chars.put('A', A);

        TextBoxViewer viewer = new TextBoxViewer(chars);
        GUI gui = mock(GUI.class);

        // text with no available characters
        TextBox box1 = new TextBox("xyz", new Position(0,0), new TextColor.RGB(0,0,0));
        viewer.drawElement(gui, box1, 0L);
        verifyNoInteractions(gui);

        // empty text
        TextBox box2 = new TextBox("", new Position(1,1), new TextColor.RGB(0,0,0));
        viewer.drawElement(gui, box2, 0L);
        verifyNoInteractions(gui);
    }
}
