package pt.feup.tvvs.pacman.states.menu;

import com.googlecode.lanterna.graphics.BasicTextImage;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.audio.AudioPlayer;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.menu.MainMenu;
import pt.feup.tvvs.pacman.viewer.game.ImageLoader;

import java.awt.image.BufferedImage;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MainMenuStateMutationTests {

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
    public void step_with_real_viewer_and_controller_does_not_throw() throws Exception {
        // Mock ImageLoader statics so ViewerFactory/CreateViewer succeed
        try (MockedStatic<ImageLoader> loader = org.mockito.Mockito.mockStatic(ImageLoader.class)) {
            loader.when(ImageLoader::loadFontImages).thenReturn(makeFontImages());
            loader.when(() -> ImageLoader.loadTextImage("PNGs/pacman/pacmanright.png")).thenReturn(new BasicTextImage(11,11));
            loader.when(() -> ImageLoader.loadGhostImages("blinky")).thenReturn(makeGhostImages());
            loader.when(() -> ImageLoader.loadGhostImages("pinky")).thenReturn(makeGhostImages());
            loader.when(() -> ImageLoader.loadGhostImages("inky")).thenReturn(makeGhostImages());
            loader.when(() -> ImageLoader.loadGhostImages("clyde")).thenReturn(makeGhostImages());
            loader.when(ImageLoader::loadPacmanImages).thenReturn(makePacmanImages());

            AudioManager audioManager = mock(AudioManager.class);
            AudioPlayer sel = mock(AudioPlayer.class);
            AudioPlayer conf = mock(AudioPlayer.class);
            when(audioManager.getAudio("menuSelect")).thenReturn(sel);
            when(audioManager.getAudio("menuConfirmSelection")).thenReturn(conf);

            MainMenu menu = new MainMenu(GUI.SCREEN_RESOLUTION._900p, 1f);

            // Construct MainMenuState without overrides -- this will call createViewer/createController
            MainMenuState state = new MainMenuState(menu, audioManager);

            Game game = mock(Game.class);
            GUI gui = mock(GUI.class);
            when(gui.getNextAction()).thenReturn(List.of(GUI.ACTION.NONE));
            when(game.getAudioManager()).thenReturn(audioManager);
            when(game.getGui()).thenReturn(gui);

            // If createViewer or createController returned null (mutant), this would throw NPE. We assert it doesn't.
            assertThatCode(() -> state.step(game, gui, 10L)).doesNotThrowAnyException();
        }
    }

}
