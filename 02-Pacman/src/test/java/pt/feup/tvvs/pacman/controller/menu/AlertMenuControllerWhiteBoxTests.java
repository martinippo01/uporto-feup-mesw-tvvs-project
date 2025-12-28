package pt.feup.tvvs.pacman.controller.menu;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.audio.AudioPlayer;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;
import pt.feup.tvvs.pacman.model.menu.AlertMenu;
import pt.feup.tvvs.pacman.states.menu.MapSelectionMenuState;
import pt.feup.tvvs.pacman.states.menu.MainMenuState;

import java.io.IOException;
import java.net.URISyntaxException;
import java.awt.FontFormatException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import org.mockito.MockedConstruction;

public class AlertMenuControllerWhiteBoxTests {

    @Test
    public void select_onPlayAgain_shouldStopAudios_andSetMapSelectionState_singleplayer() throws URISyntaxException, IOException, FontFormatException {
        // Arrange
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer selectPlayer = mock(AudioPlayer.class);
        AudioPlayer confirmPlayer = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(selectPlayer);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(confirmPlayer);

        Arena arena = new Arena(10, 10);
        arena.addPacman(new Pacman(new pt.feup.tvvs.pacman.model.Position(1,1)));
        AlertMenu menu = new AlertMenu(arena, "someAlert");
        // default selected option is 0 -> PlayAgainSelected()

        AlertMenuController controller = new AlertMenuController(menu, audioManager);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);
        when(game.getResolution()).thenReturn(GUI.SCREEN_RESOLUTION._900p);

        // Mock construction of MapSelectionMenuState to avoid heavy viewer construction
        try (MockedConstruction<MapSelectionMenuState> ignored = mockConstruction(MapSelectionMenuState.class, (mock, ctx) -> {})) {
            // Act
            controller.step(game, List.of(GUI.ACTION.SELECT), 0);
        }

        // Assert
        verify(confirmPlayer, times(1)).playOnce();
        verify(audioManager, times(1)).stopAllAudios();

        ArgumentCaptor<pt.feup.tvvs.pacman.states.State> captor = ArgumentCaptor.forClass(pt.feup.tvvs.pacman.states.State.class);
        verify(game).setState(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(MapSelectionMenuState.class);
    }

    @Test
    public void select_onPlayAgain_multiplayer_mode_setsMapSelectionState_modeMultiplayer() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer selectPlayer = mock(AudioPlayer.class);
        AudioPlayer confirmPlayer = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(selectPlayer);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(confirmPlayer);

        Arena arena = new Arena(10, 10);
        // add two pacmans to trigger multiplayer mode
        arena.addPacman(new Pacman(new pt.feup.tvvs.pacman.model.Position(1,1)));
        arena.addPacman(new Pacman(new pt.feup.tvvs.pacman.model.Position(2,2)));
        AlertMenu menu = new AlertMenu(arena, "someAlert");

        AlertMenuController controller = new AlertMenuController(menu, audioManager);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);
        when(game.getResolution()).thenReturn(GUI.SCREEN_RESOLUTION._900p);

        try (MockedConstruction<MapSelectionMenuState> ignored = mockConstruction(MapSelectionMenuState.class, (mock, ctx) -> {})) {
            controller.step(game, List.of(GUI.ACTION.SELECT), 0);
        }

        verify(confirmPlayer, times(1)).playOnce();
        verify(audioManager, times(1)).stopAllAudios();

        ArgumentCaptor<pt.feup.tvvs.pacman.states.State> captor = ArgumentCaptor.forClass(pt.feup.tvvs.pacman.states.State.class);
        verify(game).setState(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(MapSelectionMenuState.class);
    }

    @Test
    public void select_onExit_shouldStopAudios_andSetMainMenuState() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer selectPlayer = mock(AudioPlayer.class);
        AudioPlayer confirmPlayer = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(selectPlayer);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(confirmPlayer);

        Arena arena = new Arena(10, 10);
        AlertMenu menu = new AlertMenu(arena, "someAlert");
        menu.setSelectedOption(1); // select "Back to main menu"

        AlertMenuController controller = new AlertMenuController(menu, audioManager);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);
        when(game.getResolution()).thenReturn(GUI.SCREEN_RESOLUTION._900p);
        when(audioManager.getMasterVolume()).thenReturn(0.5f);

        try (MockedConstruction<MainMenuState> ignored = mockConstruction(MainMenuState.class, (mock, ctx) -> {})) {
            controller.step(game, List.of(GUI.ACTION.SELECT), 0);
        }

        verify(confirmPlayer, times(1)).playOnce();
        verify(audioManager, times(1)).stopAllAudios();

        ArgumentCaptor<pt.feup.tvvs.pacman.states.State> captor = ArgumentCaptor.forClass(pt.feup.tvvs.pacman.states.State.class);
        verify(game).setState(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(MainMenuState.class);
    }

    @Test
    public void nonSelect_actions_doNotTriggerStateChange_butDelegateToParent() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer selectPlayer = mock(AudioPlayer.class);
        AudioPlayer confirmPlayer = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(selectPlayer);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(confirmPlayer);

        Arena arena = new Arena(10, 10);
        AlertMenu menu = new AlertMenu(arena, "someAlert");

        AlertMenuController controller = new AlertMenuController(menu, audioManager);

        Game game = mock(Game.class);

        // use UP action -> parent MenuController handles it (calls menuSelect.playOnce and selectPrevious/next)
        controller.step(game, List.of(GUI.ACTION.UP), 0);

        verify(selectPlayer, times(1)).playOnce();
        // no setState should be invoked
        verify(game, never()).setState(any());
    }
}
