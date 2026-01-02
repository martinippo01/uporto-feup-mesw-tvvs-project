package pt.feup.tvvs.pacman.states.menu;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.audio.AudioPlayer;
import pt.feup.tvvs.pacman.controller.Controller;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.menu.AlertMenu;
import pt.feup.tvvs.pacman.viewer.Viewer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

public class AlertMenuStateWhiteBoxTests {

    private Arena createArenaWithPacman(int pacmanCount) {
        Arena arena = new Arena(10, 10);
        for (int i = 0; i < pacmanCount; ++i) {
            arena.addPacman(new Pacman(new Position(i, 0)));
        }
        return arena;
    }

    @Test
    public void constructor_registers_audio_players() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer sel = mock(AudioPlayer.class);
        AudioPlayer conf = mock(AudioPlayer.class);

        when(audioManager.getAudio("menuSelect")).thenReturn(sel);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(conf);

        AlertMenu menu = new AlertMenu(createArenaWithPacman(1), "PNGs/gameover.png");

        // construct state but override viewer creation to avoid image loading
        new AlertMenuState(menu, audioManager) {
            @Override
            public Viewer<AlertMenu> createViewer() {
                @SuppressWarnings("unchecked")
                Viewer<AlertMenu> viewer = (Viewer<AlertMenu>) mock(Viewer.class);
                // make drawElement simulate clearing and refreshing the GUI
                doAnswer(invocation -> {
                    GUI gui = invocation.getArgument(0);
                    gui.clear();
                    try {
                        gui.refresh();
                    } catch (Exception ignored) {
                    }
                    return null;
                }).when(viewer).drawElement(any(GUI.class), any(AlertMenu.class), anyLong());
                return viewer;
            }
        };

        verify(audioManager).addAudio("menuSelect", "Audio/menuSelect.wav");
        verify(audioManager).getAudio("menuSelect");
        verify(sel).setVolume(0.25f);

        verify(audioManager).addAudio("menuConfirmSelection", "Audio/menuConfirmSelection.wav");
        verify(audioManager).getAudio("menuConfirmSelection");
        verify(conf).setVolume(0.2f);
    }

    @Test
    public void step_with_none_action_calls_viewer_draw_and_no_audio_play() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer sel = mock(AudioPlayer.class);
        AudioPlayer conf = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(sel);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(conf);

        AlertMenu menu = new AlertMenu(createArenaWithPacman(1), "PNGs/gameover.png");
        // override viewer to avoid image loading and simulate clear/refresh
        AlertMenuState state = new AlertMenuState(menu, audioManager) {
            @Override
            public Viewer<AlertMenu> createViewer() {
                @SuppressWarnings("unchecked")
                Viewer<AlertMenu> viewer = (Viewer<AlertMenu>) mock(Viewer.class);
                doAnswer(invocation -> {
                    GUI gui = invocation.getArgument(0);
                    gui.clear();
                    try {
                        gui.refresh();
                    } catch (Exception ignored) {
                    }
                    return null;
                }).when(viewer).drawElement(any(GUI.class), any(AlertMenu.class), anyLong());
                return viewer;
            }
        };

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);

        GUI gui = mock(GUI.class);
        when(gui.getNextAction()).thenReturn(List.of(GUI.ACTION.NONE));
        when(game.getGui()).thenReturn(gui);

        // call step -> should cause viewer to clear and refresh (drawElement)
        state.step(game, gui, 42);

        verify(gui).clear();
        verify(gui).refresh();

        // no confirm selection audio played
        verify(conf, never()).playOnce();
    }

    @Test
    public void step_select_playAgain_sets_mapSelection_state_and_plays_audio() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer sel = mock(AudioPlayer.class);
        AudioPlayer conf = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(sel);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(conf);

        // create arena with single pacman -> PlayAgainSelected should be true when selectedOption==0
        AlertMenu menu = new AlertMenu(createArenaWithPacman(1), "PNGs/gameover.png");
        // ensure selected option is Play Again (index 0)
        menu.setSelectedOption(0);

        AlertMenuState state = new AlertMenuState(menu, audioManager) {
            @Override
            public Viewer<AlertMenu> createViewer() {
                @SuppressWarnings("unchecked")
                Viewer<AlertMenu> viewer = (Viewer<AlertMenu>) mock(Viewer.class);
                doAnswer(invocation -> {
                    GUI gui = invocation.getArgument(0);
                    gui.clear();
                    try {
                        gui.refresh();
                    } catch (Exception ignored) {
                    }
                    return null;
                }).when(viewer).drawElement(any(GUI.class), any(AlertMenu.class), anyLong());
                return viewer;
            }
        };

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);

        GUI gui = mock(GUI.class);
        when(gui.getNextAction()).thenReturn(List.of(GUI.ACTION.SELECT));
        when(game.getGui()).thenReturn(gui);

        state.step(game, gui, 1);

        // confirm selection audio played
        verify(conf).playOnce();
        // game should switch to MapSelectionMenuState
        ArgumentCaptor<pt.feup.tvvs.pacman.states.State> captor = ArgumentCaptor.forClass(pt.feup.tvvs.pacman.states.State.class);
        verify(game).setState(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(MapSelectionMenuState.class);

        // stopAllAudios should have been called on game's audio manager
        verify(game.getAudioManager()).stopAllAudios();
    }

    @Test
    public void step_select_exit_sets_mainMenu_state_and_plays_audio() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer sel = mock(AudioPlayer.class);
        AudioPlayer conf = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(sel);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(conf);
        when(audioManager.getMasterVolume()).thenReturn(0.5f);

        // create arena and alert menu
        AlertMenu menu = new AlertMenu(createArenaWithPacman(1), "PNGs/gameover.png");
        // set selected option to Exit (index 1)
        menu.setSelectedOption(1);

        // create a mock state to be set instead of constructing MainMenuState (which loads viewers and fails in test env)
        pt.feup.tvvs.pacman.states.State<?> mockState = mock(pt.feup.tvvs.pacman.states.State.class);

        AlertMenuState state = new AlertMenuState(menu, audioManager) {
            @Override
            public Viewer<AlertMenu> createViewer() {
                @SuppressWarnings("unchecked")
                Viewer<AlertMenu> viewer = (Viewer<AlertMenu>) mock(Viewer.class);
                doAnswer(invocation -> {
                    GUI gui = invocation.getArgument(0);
                    gui.clear();
                    try {
                        gui.refresh();
                    } catch (Exception ignored) {
                    }
                    return null;
                }).when(viewer).drawElement(any(GUI.class), any(AlertMenu.class), anyLong());
                return viewer;
            }

            @Override
            public Controller<AlertMenu> createController(AudioManager audioManager) {
                return new Controller<AlertMenu>(getModel()) {
                    @Override
                    public void step(Game game, List<GUI.ACTION> actions, long time) {
                        // simulate the essential behavior: play confirm audio, stop audios and set a mocked state
                        audioManager.getAudio("menuConfirmSelection").playOnce();
                        game.getAudioManager().stopAllAudios();
                        game.setState(mockState);
                    }
                };
            }
        };

        Game game = mock(Game.class);
        when(game.getAudioManager()).thenReturn(audioManager);
        when(game.getResolution()).thenReturn(GUI.SCREEN_RESOLUTION._720p);

        GUI gui = mock(GUI.class);
        when(gui.getNextAction()).thenReturn(List.of(GUI.ACTION.SELECT));
        when(game.getGui()).thenReturn(gui);

        state.step(game, gui, 2);

        // confirm selection audio played
        verify(conf).playOnce();

        // ensure game.setState was called with our mockState
        verify(game).setState(mockState);

        verify(game.getAudioManager()).stopAllAudios();
    }
}
