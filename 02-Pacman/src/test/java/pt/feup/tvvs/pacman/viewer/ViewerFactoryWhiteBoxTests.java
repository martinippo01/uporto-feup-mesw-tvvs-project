package pt.feup.tvvs.pacman.viewer;

import com.googlecode.lanterna.graphics.BasicTextImage;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import pt.feup.tvvs.pacman.model.Element;
import pt.feup.tvvs.pacman.model.game.element.Wall;
import pt.feup.tvvs.pacman.model.game.element.collectibles.Coin;
import pt.feup.tvvs.pacman.model.game.element.GhostGate;
import pt.feup.tvvs.pacman.model.game.element.ghost.Blinky;
import pt.feup.tvvs.pacman.model.game.element.ghost.Clyde;
import pt.feup.tvvs.pacman.model.game.element.ghost.Inky;
import pt.feup.tvvs.pacman.model.game.element.ghost.Pinky;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;
import pt.feup.tvvs.pacman.model.menu.element.TextBox;
import pt.feup.tvvs.pacman.viewer.game.ImageLoader;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mockStatic;

public class ViewerFactoryWhiteBoxTests {

    private Map<Character, BufferedImage> makeFontImages() {
        Map<Character, BufferedImage> font = new HashMap<>();
        font.put('A', new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB));
        font.put('B', new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB));
        return font;
    }

    private Map<Character, List<BufferedImage>> makePacmanImages() {
        Map<Character, List<BufferedImage>> m = new HashMap<>();
        m.put('L', List.of(new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB), new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB)));
        m.put('U', List.of(new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB), new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB)));
        m.put('D', List.of(new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB), new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB)));
        m.put('R', List.of(new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB), new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB)));
        m.put('X', List.of(new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB)));
        return m;
    }

    private Map<Character, List<BufferedImage>> makeGhostImages() {
        Map<Character, List<BufferedImage>> m = new HashMap<>();
        m.put('L', List.of(new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB), new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB)));
        m.put('U', List.of(new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB), new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB)));
        m.put('D', List.of(new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB), new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB)));
        m.put('R', List.of(new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB), new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB)));
        m.put('S', List.of(new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB), new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB)));
        m.put('r', List.of(new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB)));
        m.put('l', List.of(new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB)));
        m.put('u', List.of(new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB)));
        m.put('d', List.of(new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB)));
        return m;
    }

    @Test
    public void createArenaViewers_returns_expected_viewers_and_uses_ImageLoader() throws Exception {
        try (MockedStatic<ImageLoader> loader = mockStatic(ImageLoader.class)) {
            // stub ImageLoader
            loader.when(() -> ImageLoader.loadTextImage("PNGs/wall.png")).thenReturn(new BasicTextImage(11,11));
            loader.when(() -> ImageLoader.loadTextImage("PNGs/ghostgate.png")).thenReturn(new BasicTextImage(11,11));
            loader.when(() -> ImageLoader.loadTextImage("PNGs/items/coin.png")).thenReturn(new BasicTextImage(11,11));
            loader.when(() -> ImageLoader.loadTextImage("PNGs/items/apple.png")).thenReturn(new BasicTextImage(11,11));
            loader.when(() -> ImageLoader.loadTextImage("PNGs/items/cherry.png")).thenReturn(new BasicTextImage(11,11));
            loader.when(() -> ImageLoader.loadTextImage("PNGs/items/key.png")).thenReturn(new BasicTextImage(11,11));
            loader.when(() -> ImageLoader.loadTextImage("PNGs/items/orange.png")).thenReturn(new BasicTextImage(11,11));
            loader.when(() -> ImageLoader.loadTextImage("PNGs/items/strawberry.png")).thenReturn(new BasicTextImage(11,11));
            loader.when(() -> ImageLoader.loadTextImage("PNGs/items/powerup.png")).thenReturn(new BasicTextImage(11,11));

            loader.when(ImageLoader::loadFontImages).thenReturn(makeFontImages());
            loader.when(ImageLoader::loadPacmanImages).thenReturn(makePacmanImages());
            loader.when(() -> ImageLoader.loadGhostImages("blinky")).thenReturn(makeGhostImages());
            loader.when(() -> ImageLoader.loadGhostImages("pinky")).thenReturn(makeGhostImages());
            loader.when(() -> ImageLoader.loadGhostImages("inky")).thenReturn(makeGhostImages());
            loader.when(() -> ImageLoader.loadGhostImages("clyde")).thenReturn(makeGhostImages());

            Map<Class<?>, Viewer<Element>> viewers = ViewerFactory.createArenaViewers();

            // assert viewers contains expected keys
            assertThat(viewers).containsKeys(Wall.class, GhostGate.class, Coin.class, TextBox.class, Pacman.class, Blinky.class, Pinky.class, Inky.class, Clyde.class);

            // assert viewer classes (TextBoxViewer and MovableElementViewer should be present)
            assertThat(viewers.get(TextBox.class)).isInstanceOf(TextBoxViewer.class);
            assertThat(viewers.get(Pacman.class)).isNotNull();
            assertThat(viewers.get(Blinky.class)).isNotNull();

            // verify ImageLoader was called for major resources
            loader.verify(() -> ImageLoader.loadTextImage("PNGs/wall.png"));
            loader.verify(ImageLoader::loadFontImages);
            loader.verify(ImageLoader::loadPacmanImages);
            loader.verify(() -> ImageLoader.loadGhostImages("blinky"));
        }
    }

    @Test
    public void createMainMenuViewers_returns_expected_viewers_and_uses_ImageLoader() throws Exception {
        try (MockedStatic<ImageLoader> loader = mockStatic(ImageLoader.class)) {
            loader.when(ImageLoader::loadFontImages).thenReturn(makeFontImages());
            loader.when(() -> ImageLoader.loadTextImage("PNGs/pacman/pacmanright.png")).thenReturn(new BasicTextImage(11,11));
            loader.when(() -> ImageLoader.loadGhostImages("blinky")).thenReturn(makeGhostImages());
            loader.when(() -> ImageLoader.loadGhostImages("pinky")).thenReturn(makeGhostImages());
            loader.when(() -> ImageLoader.loadGhostImages("inky")).thenReturn(makeGhostImages());
            loader.when(() -> ImageLoader.loadGhostImages("clyde")).thenReturn(makeGhostImages());

            Map<Class<?>, Viewer<Element>> viewers = ViewerFactory.createMainMenuViewers();

            assertThat(viewers).containsKey(TextBox.class);
            assertThat(viewers.get(TextBox.class)).isInstanceOf(TextBoxViewer.class);
            assertThat(viewers).containsKey(Pacman.class);

            loader.verify(ImageLoader::loadFontImages);
            loader.verify(() -> ImageLoader.loadTextImage("PNGs/pacman/pacmanright.png"));
            loader.verify(() -> ImageLoader.loadGhostImages("blinky"));
        }
    }

    @Test
    public void createPauseMapSelectionAndAlertViewers_return_textboxviewer_and_alert_delegates() throws Exception {
        try (MockedStatic<ImageLoader> loader = mockStatic(ImageLoader.class)) {
            loader.when(ImageLoader::loadFontImages).thenReturn(makeFontImages());

            Map<Class<?>, Viewer<Element>> pause = ViewerFactory.createPauseMenuViewers();
            assertThat(pause).containsKey(TextBox.class);
            assertThat(pause.get(TextBox.class)).isInstanceOf(TextBoxViewer.class);

            Map<Class<?>, Viewer<Element>> mapSel = ViewerFactory.createMapSelectionMenuViewers();
            assertThat(mapSel).containsKey(TextBox.class);

            // For alert menu, stub the full arena loaders so delegation occurs
            loader.when(() -> ImageLoader.loadTextImage("PNGs/wall.png")).thenReturn(new BasicTextImage(11,11));
            loader.when(ImageLoader::loadPacmanImages).thenReturn(makePacmanImages());
            loader.when(() -> ImageLoader.loadGhostImages("blinky")).thenReturn(makeGhostImages());
            loader.when(ImageLoader::loadFontImages).thenReturn(makeFontImages());

            Map<Class<?>, Viewer<Element>> alert = ViewerFactory.createAlertMenuViewers();
            assertThat(alert).containsKey(Wall.class);

            loader.verify(ImageLoader::loadFontImages, atLeastOnce());
        }
    }
}
