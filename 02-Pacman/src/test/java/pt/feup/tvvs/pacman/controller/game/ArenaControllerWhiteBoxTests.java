package pt.feup.tvvs.pacman.controller.game;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.audio.AudioPlayer;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.states.State;
import pt.feup.tvvs.pacman.states.menu.AlertMenuState;
import pt.feup.tvvs.pacman.states.menu.PauseMenuState;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.collectibles.Coin;
import pt.feup.tvvs.pacman.model.Position;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ArenaControllerWhiteBoxTests {

    @Test
    public void quitActionShouldStopAudiosAndSetPauseState() throws IOException, URISyntaxException {
        // Arrange
        Arena arena = new Arena(10, 10);
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer audioPlayer = mock(AudioPlayer.class);
        // avoid NPEs in CollisionController constructor by stubbing addAudio/getAudio
        doNothing().when(audioManager).addAudio(anyString(), anyString());
        when(audioManager.getAudio(anyString())).thenReturn(audioPlayer);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);
        when(game.getState()).thenReturn(null);
        when(game.getResolution()).thenReturn(GUI.SCREEN_RESOLUTION._900p);

        ArenaController controller = new ArenaController(arena, audioManager);

        // Mock construction of PauseMenuState so its heavy init doesn't run
        try (var mocked = mockConstruction(PauseMenuState.class)) {
            // Act
            controller.step(game, List.of(GUI.ACTION.QUIT), 0L);

            // Assert
            verify(audioManager, times(1)).stopAllAudios();
            ArgumentCaptor<State> captor = ArgumentCaptor.forClass(State.class);
            verify(game, times(1)).setState(captor.capture());
            State<?> newState = captor.getValue();
            // the setState receives the mocked PauseMenuState instance
            assertThat(newState).isInstanceOf(PauseMenuState.class);
        }
    }

    @Test
    public void emptyCollectiblesShouldStopAudiosAndSetAlertState() throws IOException, URISyntaxException {
        // Arrange
        Arena arena = new Arena(10, 10);
        // ensure collectibles empty (default)
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer audioPlayer = mock(AudioPlayer.class);
        doNothing().when(audioManager).addAudio(anyString(), anyString());
        when(audioManager.getAudio(anyString())).thenReturn(audioPlayer);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);

        ArenaController controller = new ArenaController(arena, audioManager);

        // Mock construction of AlertMenuState to avoid heavy viewer setup
        try (var mocked = mockConstruction(AlertMenuState.class)) {
            // Act
            controller.step(game, List.of(), 0L);

            // Assert
            verify(audioManager, times(1)).stopAllAudios();
            ArgumentCaptor<State> captor = ArgumentCaptor.forClass(State.class);
            verify(game, times(1)).setState(captor.capture());
            State<?> newState = captor.getValue();
            assertThat(newState).isInstanceOf(AlertMenuState.class);
        }
    }

    @Test
    public void shouldDelegateToInternalControllersWhenNoQuitAndCollectiblesPresent() throws Exception {
        // Arrange
        Arena arena = new Arena(10, 10);
        // add a collectible so the controller doesn't trigger the win alert
        arena.addCollectible(new Coin(new Position(1, 1)));

        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer audioPlayer = mock(AudioPlayer.class);
        doNothing().when(audioManager).addAudio(anyString(), anyString());
        when(audioManager.getAudio(anyString())).thenReturn(audioPlayer);

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);

        ArenaController controller = new ArenaController(arena, audioManager);

        // Replace internal controllers with mocks to verify delegation
        Object pacmanMock = mock(pt.feup.tvvs.pacman.controller.game.element.PacmanController.class);
        Object ghostMock = mock(pt.feup.tvvs.pacman.controller.game.element.GhostController.class);
        Object collisionMock = mock(pt.feup.tvvs.pacman.controller.game.element.CollisionController.class);

        setPrivateField(controller, "pacmanController", pacmanMock);
        setPrivateField(controller, "ghostController", ghostMock);
        setPrivateField(controller, "collisionController", collisionMock);

        // Act
        List<GUI.ACTION> actions = List.of(GUI.ACTION.NONE);
        controller.step(game, actions, 1L);

        // Assert - verify each internal controller had its step called
        verify((pt.feup.tvvs.pacman.controller.game.element.PacmanController) pacmanMock, times(1)).step(game, actions, 1L);
        verify((pt.feup.tvvs.pacman.controller.game.element.GhostController) ghostMock, times(1)).step(game, actions, 1L);
        verify((pt.feup.tvvs.pacman.controller.game.element.CollisionController) collisionMock, times(1)).step(game, actions, 1L);
    }

    // reflection helper
    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        // best-effort: try to set directly and ignore modifier removal which isn't available on newer JDKs
        f.set(target, value);
    }
}
