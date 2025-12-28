package pt.feup.tvvs.pacman.controller.menu;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.audio.AudioPlayer;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.menu.MapSelectionMenu;
import pt.feup.tvvs.pacman.states.game.GameState;
import pt.feup.tvvs.pacman.states.menu.MainMenuState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class MapSelectionMenuControllerWhiteBoxTests {

    private final Path mapsRoot = Path.of("src/main/resources/Maps");

    @AfterEach
    public void cleanup() throws IOException {
        // leave created files if any; try to delete our test folder if exists
        Path testDir = mapsRoot.resolve("testmaps");
        if (Files.exists(testDir)) {
            Files.list(testDir).forEach(p -> p.toFile().delete());
            testDir.toFile().delete();
        }
    }

    @Test
    public void select_should_load_map_and_set_game_state() throws Exception {
        // prepare test map directory and file
        Path folder = mapsRoot.resolve("testmaps");
        Files.createDirectories(folder);
        Path mapFile = folder.resolve("mymap.txt");
        Files.writeString(mapFile, "   \n   \n");

        MapSelectionMenu model = new MapSelectionMenu("testmaps");

        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer selectPlayer = mock(AudioPlayer.class);
        AudioPlayer confirmPlayer = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(selectPlayer);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(confirmPlayer);

        MapSelectionMenuController controller = new MapSelectionMenuController(model, audioManager);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);

        try (MockedConstruction<GameState> mocked = mockConstruction(GameState.class)) {
            controller.step(game, List.of(GUI.ACTION.SELECT), 0);

            verify(confirmPlayer, times(1)).playOnce();

            // verify game.setState called with the constructed GameState mock
            ArgumentCaptor<pt.feup.tvvs.pacman.states.State> captor = ArgumentCaptor.forClass(pt.feup.tvvs.pacman.states.State.class);
            verify(game).setState(captor.capture());
            assertThat(captor.getValue()).isInstanceOf(GameState.class);
            // also ensure we actually constructed one GameState
            assertThat(mocked.constructed()).hasSize(1);
        }
    }

    @Test
    public void quit_should_set_main_menu_state() throws Exception {
        Path folder = mapsRoot.resolve("testmaps");
        Files.createDirectories(folder);
        Path mapFile = folder.resolve("mymap.txt");
        Files.writeString(mapFile, "  \n");

        MapSelectionMenu model = new MapSelectionMenu("testmaps");

        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer selectPlayer = mock(AudioPlayer.class);
        AudioPlayer confirmPlayer = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(selectPlayer);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(confirmPlayer);

        MapSelectionMenuController controller = new MapSelectionMenuController(model, audioManager);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);
        when(game.getResolution()).thenReturn(GUI.SCREEN_RESOLUTION._900p);
        when(audioManager.getMasterVolume()).thenReturn(0.7f);

        try (MockedConstruction<MainMenuState> mocked = mockConstruction(MainMenuState.class)) {
            controller.step(game, List.of(GUI.ACTION.QUIT), 0);

            ArgumentCaptor<pt.feup.tvvs.pacman.states.State> captor = ArgumentCaptor.forClass(pt.feup.tvvs.pacman.states.State.class);
            verify(game).setState(captor.capture());
            assertThat(captor.getValue()).isInstanceOf(MainMenuState.class);
            assertThat(mocked.constructed()).hasSize(1);
        }
    }

    @Test
    public void select_when_map_file_removed_throws_ioexception() throws Exception {
        Path folder = mapsRoot.resolve("testmaps");
        Files.createDirectories(folder);
        Path mapFile = folder.resolve("mymap.txt");
        Files.writeString(mapFile, "   \n");

        MapSelectionMenu model = new MapSelectionMenu("testmaps");

        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer selectPlayer = mock(AudioPlayer.class);
        AudioPlayer confirmPlayer = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(selectPlayer);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(confirmPlayer);

        MapSelectionMenuController controller = new MapSelectionMenuController(model, audioManager);

        // remove the actual file to cause loadMap to fail
        Files.deleteIfExists(mapFile);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);

        assertThrows(IOException.class, () -> controller.step(game, List.of(GUI.ACTION.SELECT), 0));
    }
}
