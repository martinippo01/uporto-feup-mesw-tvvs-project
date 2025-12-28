package pt.feup.tvvs.pacman.gui;

import org.junit.jupiter.api.Test;
import pt.feup.tvvs.pacman.model.Position;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GUIWhiteBoxTests {

    @Test
    public void screenResolution_toString_allValues() {
        assertThat(GUI.SCREEN_RESOLUTION._360p.toString()).isEqualTo("360p");
        assertThat(GUI.SCREEN_RESOLUTION._540p.toString()).isEqualTo("540p");
        assertThat(GUI.SCREEN_RESOLUTION._720p.toString()).isEqualTo("720p");
        assertThat(GUI.SCREEN_RESOLUTION._900p.toString()).isEqualTo("900p");
        assertThat(GUI.SCREEN_RESOLUTION._1080p.toString()).isEqualTo("1080p");
        assertThat(GUI.SCREEN_RESOLUTION._1440p.toString()).isEqualTo("1440p");
        assertThat(GUI.SCREEN_RESOLUTION._2160p.toString()).isEqualTo("2160p");
    }

    @Test
    public void action_enum_contains_expectedValues() {
        GUI.ACTION[] actions = GUI.ACTION.values();
        // ensure core actions exist
        assertThat(actions).extracting(Enum::name).contains("UP", "DOWN", "LEFT", "RIGHT", "SELECT", "QUIT", "NONE");
    }

    @Test
    public void anonymous_gui_implementation_methods_are_callable_and_exceptions_propagate() throws Exception {
        // Create a minimal GUI implementation for exercising the interface methods
        GUI gui = new GUI() {
            private GUI.SCREEN_RESOLUTION resolution = GUI.SCREEN_RESOLUTION._720p;

            @Override
            public List<ACTION> getNextAction() {
                return List.of(ACTION.SELECT);
            }

            @Override
            public void drawImage(Position position, com.googlecode.lanterna.graphics.BasicTextImage image) {
                // no-op
            }

            @Override
            public void drawImage(Position position, BufferedImage image) {
                // no-op
            }

            @Override
            public void drawImage(Position position, BufferedImage image, int width, int height) {
                // no-op
            }

            @Override
            public void drawCharacter(Position position, BufferedImage character, com.googlecode.lanterna.TextColor color) {
                // no-op
            }

            @Override
            public void clear() {
                // no-op
            }

            @Override
            public void erase(Position position) {
                // no-op
            }

            @Override
            public void refresh() {
                // no-op (no exception)
            }

            @Override
            public void close() {
                // no-op
            }

            @Override
            public void resizeScreen(int width, int height, SCREEN_RESOLUTION newResolution) throws URISyntaxException, IOException, java.awt.FontFormatException {
                if (width < 0 || height < 0) throw new URISyntaxException("bad","negative");
                this.resolution = newResolution;
            }

            @Override
            public SCREEN_RESOLUTION getResolution() {
                return resolution;
            }
        };

        // call methods and assert behavior
        assertThat(gui.getNextAction()).contains(GUI.ACTION.SELECT);
        gui.clear();
        gui.erase(new Position(0,0));
        gui.drawImage(new Position(1,1), (BufferedImage) null);
        gui.drawImage(new Position(1,1), (com.googlecode.lanterna.graphics.BasicTextImage) null);
        gui.refresh();
        gui.close();

        // resize with valid args
        gui.resizeScreen(100, 200, GUI.SCREEN_RESOLUTION._900p);
        assertThat(gui.getResolution()).isEqualTo(GUI.SCREEN_RESOLUTION._900p);

        // negative size should throw URISyntaxException
        assertThrows(URISyntaxException.class, () -> gui.resizeScreen(-1, 10, GUI.SCREEN_RESOLUTION._360p));
    }
}
