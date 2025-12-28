package pt.feup.tvvs.pacman.controller.menu;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.audio.AudioPlayer;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.Direction;
import pt.feup.tvvs.pacman.model.game.element.ghost.Blinky;
import pt.feup.tvvs.pacman.model.game.element.ghost.Clyde;
import pt.feup.tvvs.pacman.model.game.element.ghost.Inky;
import pt.feup.tvvs.pacman.model.game.element.ghost.Pinky;
import pt.feup.tvvs.pacman.model.menu.MainMenu;
import pt.feup.tvvs.pacman.model.menu.MapSelectionMenu;
import pt.feup.tvvs.pacman.states.menu.MapSelectionMenuState;
import pt.feup.tvvs.pacman.states.menu.MainMenuState;

import java.awt.FontFormatException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MainMenuControllerWhiteBoxTests {

    private AlertHelper createControllerWithMockedAudio(MainMenu menu, AudioManager audioManager, AudioPlayer select, AudioPlayer confirm) {
        when(audioManager.getAudio("menuSelect")).thenReturn(select);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(confirm);
        return new AlertHelper(menu, audioManager);
    }

    private static class AlertHelper {
        final MainMenuController controller;
        final MainMenu menu;
        final AudioManager audioManager;

        AlertHelper(MainMenu menu, AudioManager audioManager) {
            this.menu = menu;
            this.audioManager = audioManager;
            this.controller = new MainMenuController(menu, audioManager);
        }
    }

    @Test
    public void select_singlePlayer_setsMapSelectionState() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer sel = mock(AudioPlayer.class);
        AudioPlayer conf = mock(AudioPlayer.class);

        MainMenu menu = new MainMenu(GUI.SCREEN_RESOLUTION._900p, 1f);
        // ensure single player selected
        menu.setSelectedOption(0);

        AlertHelper h = createControllerWithMockedAudio(menu, audioManager, sel, conf);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);
        when(game.getResolution()).thenReturn(GUI.SCREEN_RESOLUTION._900p);

        try (MockedConstruction<MapSelectionMenuState> ignored = mockConstruction(MapSelectionMenuState.class)) {
            h.controller.step(game, List.of(GUI.ACTION.SELECT), 0);
        }

        verify(conf, times(1)).playOnce();
        // MainMenuController does not stop all audios; do not assert on audioManager.stopAllAudios()

        ArgumentCaptor<pt.feup.tvvs.pacman.states.State> captor = ArgumentCaptor.forClass(pt.feup.tvvs.pacman.states.State.class);
        verify(game).setState(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(MapSelectionMenuState.class);
    }

    @Test
    public void select_multiplayer_setsMapSelectionState() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer sel = mock(AudioPlayer.class);
        AudioPlayer conf = mock(AudioPlayer.class);

        MainMenu menu = new MainMenu(GUI.SCREEN_RESOLUTION._900p, 1f);
        // make multiplayer selected
        menu.setSelectedOption(0);
        // add a second pacman to the menu's model to simulate multiplayer
        menu.getPacman(); // already present; we'll rely on controller logic in MainMenuController which checks model.multiplayerSelected()
        // to force multiplayerSelected true, set selected option to multiplayer and add pacman to underlying set isn't necessary; but MainMenu.multiplayerSelected() returns selected==1; we'll set that and still expect MapSelection
        menu.setSelectedOption(1);

        AlertHelper h = createControllerWithMockedAudio(menu, audioManager, sel, conf);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);
        when(game.getResolution()).thenReturn(GUI.SCREEN_RESOLUTION._900p);

        try (MockedConstruction<MapSelectionMenuState> ignored = mockConstruction(MapSelectionMenuState.class)) {
            h.controller.step(game, List.of(GUI.ACTION.SELECT), 0);
        }

        verify(conf, times(1)).playOnce();
        // MainMenuController does not stop all audios; do not assert on audioManager.stopAllAudios()

        ArgumentCaptor<pt.feup.tvvs.pacman.states.State> captor = ArgumentCaptor.forClass(pt.feup.tvvs.pacman.states.State.class);
        verify(game).setState(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(MapSelectionMenuState.class);
    }

    @Test
    public void select_exit_setsStateNull() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer sel = mock(AudioPlayer.class);
        AudioPlayer conf = mock(AudioPlayer.class);

        MainMenu menu = new MainMenu(GUI.SCREEN_RESOLUTION._900p, 1f);
        menu.setSelectedOption(4); // Exit

        AlertHelper h = createControllerWithMockedAudio(menu, audioManager, sel, conf);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);
        when(game.getResolution()).thenReturn(GUI.SCREEN_RESOLUTION._900p);

        h.controller.step(game, List.of(GUI.ACTION.SELECT), 0);

        verify(conf, times(1)).playOnce();
        // MainMenuController does not stop all audios; do not assert on audioManager.stopAllAudios()
        verify(game).setState(null);
    }

    @Test
    public void select_resolution_changesResolution() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer sel = mock(AudioPlayer.class);
        AudioPlayer conf = mock(AudioPlayer.class);

        MainMenu menu = new MainMenu(GUI.SCREEN_RESOLUTION._360p, 0.5f);
        menu.setSelectedOption(2); // Resolution option

        AlertHelper h = createControllerWithMockedAudio(menu, audioManager, sel, conf);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);
        when(game.getResolution()).thenReturn(GUI.SCREEN_RESOLUTION._360p);

        h.controller.step(game, List.of(GUI.ACTION.SELECT), 0);

        // after selection, resolution should have been incremented and set on game and model
        verify(game).setResolution(GUI.SCREEN_RESOLUTION._540p);
        assertThat(menu.getOptions().get(2).getText()).contains("540p");
    }

    @Test
    public void select_masterVolume_changesVolume() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer sel = mock(AudioPlayer.class);
        AudioPlayer conf = mock(AudioPlayer.class);
        when(audioManager.getMasterVolume()).thenReturn(0.5f);

        MainMenu menu = new MainMenu(GUI.SCREEN_RESOLUTION._900p, 0.5f);
        menu.setSelectedOption(3); // Master Volume

        AlertHelper h = createControllerWithMockedAudio(menu, audioManager, sel, conf);

        Game game = mock(Game.class);
        GUI gui = mock(GUI.class);
        when(game.getAudioManager()).thenReturn(audioManager);
        when(game.getGui()).thenReturn(gui);

        h.controller.step(game, List.of(GUI.ACTION.SELECT), 0);

        verify(audioManager).setMasterVolume(0.6f);
        assertThat(menu.getOptions().get(3).getText()).contains("Master Volume");
    }

    @Test
    public void blinky_direction_changes_based_on_position() throws Exception {
        MainMenu menu = new MainMenu(GUI.SCREEN_RESOLUTION._900p, 1f);
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer sel = mock(AudioPlayer.class);
        AudioPlayer conf = mock(AudioPlayer.class);
        AlertHelper h = createControllerWithMockedAudio(menu, audioManager, sel, conf);

        Blinky blinky = menu.getBlinky();
        // set to left boundary -> should set RIGHT
        blinky.setPosition(new Position(3,4));
        blinky.setDirection(Direction.LEFT);

        h.controller.step(mock(Game.class), List.of(), 0);
        assertThat(blinky.getDirection()).isEqualTo(Direction.RIGHT);
        assertThat(blinky.getCounter()).isGreaterThan(0);

        // set to right boundary -> should set LEFT
        blinky.setPosition(new Position(7,4));
        blinky.setDirection(Direction.RIGHT);
        h.controller.step(mock(Game.class), List.of(), 0);
        assertThat(blinky.getDirection()).isEqualTo(Direction.LEFT);
    }

    @Test
    public void inky_pinky_clyde_direction_changes_based_on_position() throws Exception {
        MainMenu menu = new MainMenu(GUI.SCREEN_RESOLUTION._900p, 1f);
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer sel = mock(AudioPlayer.class);
        AudioPlayer conf = mock(AudioPlayer.class);
        AlertHelper h = createControllerWithMockedAudio(menu, audioManager, sel, conf);

        Inky inky = menu.getInky();
        inky.setPosition(new Position(5,8));
        inky.setDirection(Direction.UP);
        h.controller.step(mock(Game.class), List.of(), 0);
        assertThat(inky.getDirection()).isEqualTo(Direction.DOWN);

        inky.setPosition(new Position(5,13));
        inky.setDirection(Direction.DOWN);
        h.controller.step(mock(Game.class), List.of(), 0);
        assertThat(inky.getDirection()).isEqualTo(Direction.UP);

        Pinky pinky = menu.getPinky();
        pinky.setPosition(new Position(19,13));
        pinky.setDirection(Direction.LEFT);
        h.controller.step(mock(Game.class), List.of(), 0);
        assertThat(pinky.getDirection()).isEqualTo(Direction.RIGHT);

        pinky.setPosition(new Position(26,13));
        pinky.setDirection(Direction.RIGHT);
        h.controller.step(mock(Game.class), List.of(), 0);
        assertThat(pinky.getDirection()).isEqualTo(Direction.LEFT);

        Clyde clyde = menu.getClyde();
        clyde.setPosition(new Position(24,3));
        clyde.setDirection(Direction.UP);
        h.controller.step(mock(Game.class), List.of(), 0);
        assertThat(clyde.getDirection()).isEqualTo(Direction.DOWN);

        clyde.setPosition(new Position(24,10));
        clyde.setDirection(Direction.DOWN);
        h.controller.step(mock(Game.class), List.of(), 0);
        assertThat(clyde.getDirection()).isEqualTo(Direction.UP);
    }
}
