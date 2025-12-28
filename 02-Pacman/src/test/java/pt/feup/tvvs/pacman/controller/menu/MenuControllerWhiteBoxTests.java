package pt.feup.tvvs.pacman.controller.menu;

import com.googlecode.lanterna.TextColor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.audio.AudioPlayer;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.menu.Menu;
import pt.feup.tvvs.pacman.model.menu.MenuOptions;
import pt.feup.tvvs.pacman.model.menu.element.TextBox;

import java.awt.FontFormatException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MenuControllerWhiteBoxTests {

    static class TestMenuController extends MenuController<Menu> {
        public TestMenuController(Menu model, AudioManager audioManager) {
            super(model, audioManager);
        }

        public float callHandleVolumeChange(Game game) {
            return handleVolumeChange(game);
        }

        public GUI.SCREEN_RESOLUTION callIncrement(GUI.SCREEN_RESOLUTION s) {
            return incrementResolution(s);
        }

        public GUI.SCREEN_RESOLUTION callDecrement(GUI.SCREEN_RESOLUTION s) {
            return decrementResolution(s);
        }

        // expose step as public (inherited) so no wrapper needed
    }

    private Menu createSimpleMenu() {
        return new Menu() {
            @Override
            protected java.util.List<TextBox> createOptions() {
                return List.of(new TextBox("one", new Position(0,0), new TextColor.RGB(255,255,255)),
                        new TextBox("two", new Position(0,1), new TextColor.RGB(255,255,255)));
            }

            @Override
            protected TextBox createTitle() {
                return new TextBox("title", new Position(0,0), new TextColor.RGB(255,255,255));
            }
        };
    }

    private static class MenuOptionsImpl extends Menu implements MenuOptions {
        private boolean resolutionSelected;
        private boolean masterVolumeSelected;
        private float masterVolume;
        private GUI.SCREEN_RESOLUTION resolution;

        MenuOptionsImpl(boolean resolutionSelected, boolean masterVolumeSelected) {
            this.resolutionSelected = resolutionSelected;
            this.masterVolumeSelected = masterVolumeSelected;
            initializeOptions();
        }

        @Override
        public boolean ResolutionSelected() {
            return resolutionSelected;
        }

        @Override
        public boolean MasterVolumeSelected() {
            return masterVolumeSelected;
        }

        @Override
        public void setMasterVolume(float volume) {
            this.masterVolume = volume;
        }

        @Override
        public void setResolution(GUI.SCREEN_RESOLUTION newResolution) {
            this.resolution = newResolution;
            // also update the option text to simulate behavior
            getOptions().get(2).setText("Resolution: " + newResolution);
        }

        public GUI.SCREEN_RESOLUTION getResolution() {
            return resolution;
        }

        public float getMasterVolume() {
            return masterVolume;
        }

        @Override
        protected java.util.List<TextBox> createOptions() {
            // ensure there are at least 4 options (single/multi/resolution/volume/exit)
            return List.of(new TextBox("Single player", new Position(0,0), new TextColor.RGB(255,255,255)),
                    new TextBox("Multi player", new Position(0,1), new TextColor.RGB(255,255,255)),
                    new TextBox("Resolution: 360p", new Position(0,2), new TextColor.RGB(255,255,255)),
                    new TextBox("Master Volume: 5", new Position(0,3), new TextColor.RGB(255,255,255)),
                    new TextBox("Exit", new Position(0,4), new TextColor.RGB(255,255,255)));
        }

        @Override
        protected TextBox createTitle() {
            return new TextBox("title", new Position(0,0), new TextColor.RGB(255,255,255));
        }
    }

    @Test
    public void constructor_registersAudios_and_setsVolumes() {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer sel = mock(AudioPlayer.class);
        AudioPlayer conf = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(sel);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(conf);

        Menu model = mock(Menu.class);
        new TestMenuController(model, audioManager);

        verify(audioManager).addAudio("menuSelect", "Audio/menuSelect.wav");
        verify(audioManager).getAudio("menuSelect");
        verify(audioManager).addAudio("menuConfirmSelection", "Audio/menuConfirmSelection.wav");
        verify(audioManager).getAudio("menuConfirmSelection");
        verify(sel).setVolume(0.25f);
        verify(conf).setVolume(0.2f);
    }

    @Test
    public void handleVolumeChange_whenMasterIsOne_setsToPointOne() {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer sel = mock(AudioPlayer.class);
        AudioPlayer conf = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(sel);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(conf);

        Menu model = createSimpleMenu();
        TestMenuController controller = new TestMenuController(model, audioManager);

        Game game = mock(Game.class);
        GUI gui = mock(GUI.class);
        when(game.getGui()).thenReturn(gui);
        when(game.getAudioManager()).thenReturn(audioManager);
        when(audioManager.getMasterVolume()).thenReturn(1f);

        float nv = controller.callHandleVolumeChange(game);
        assertThat(nv).isEqualTo(0.1f);
        verify(gui).clear();
        verify(audioManager).setMasterVolume(0.1f);
    }

    @Test
    public void handleVolumeChange_roundsProperly() {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer sel = mock(AudioPlayer.class);
        AudioPlayer conf = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(sel);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(conf);

        Menu model = createSimpleMenu();
        TestMenuController controller = new TestMenuController(model, audioManager);

        Game game = mock(Game.class);
        GUI gui = mock(GUI.class);
        when(game.getGui()).thenReturn(gui);
        when(game.getAudioManager()).thenReturn(audioManager);
        when(audioManager.getMasterVolume()).thenReturn(0.94f);

        float nv = controller.callHandleVolumeChange(game);
        // 0.94 + 0.1 = 1.04 -> *10 = 10.4 -> round = 10 -> /10 = 1.0f
        assertThat(nv).isEqualTo(1.0f);
        verify(audioManager).setMasterVolume(1.0f);
    }

    @Test
    public void step_up_and_down_calls_selectPrevious_and_next() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer sel = mock(AudioPlayer.class);
        AudioPlayer conf = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(sel);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(conf);

        Menu model = mock(Menu.class);
        TestMenuController controller = new TestMenuController(model, audioManager);

        Game game = mock(Game.class);
        controller.step(game, List.of(GUI.ACTION.UP), 0);
        verify(sel).playOnce();
        verify(model).selectPreviousOption();

        controller.step(game, List.of(GUI.ACTION.DOWN), 0);
        verify(sel, times(2)).playOnce();
        verify(model).selectNextOption();
    }

    @Test
    public void step_left_right_onResolution_calls_setResolution_and_play() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer sel = mock(AudioPlayer.class);
        AudioPlayer conf = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(sel);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(conf);

        MenuOptionsImpl menuOptions = new MenuOptionsImpl(true, false);
        Menu model = menuOptions;

        TestMenuController controller = new TestMenuController(model, audioManager);
        Game game = mock(Game.class);
        when(game.getResolution()).thenReturn(GUI.SCREEN_RESOLUTION._360p);

        controller.step(game, List.of(GUI.ACTION.RIGHT), 0);
        verify(game).setResolution(GUI.SCREEN_RESOLUTION._540p);
        assertThat(menuOptions.getResolution()).isEqualTo(GUI.SCREEN_RESOLUTION._540p);
        verify(sel).playOnce();

        // test LEFT
        when(game.getResolution()).thenReturn(GUI.SCREEN_RESOLUTION._540p);
        controller.step(game, List.of(GUI.ACTION.LEFT), 0);
        verify(game).setResolution(GUI.SCREEN_RESOLUTION._360p);
        assertThat(menuOptions.getResolution()).isEqualTo(GUI.SCREEN_RESOLUTION._360p);
    }

    @Test
    public void step_left_right_onMasterVolume_changesVolume_and_clears_gui() throws Exception {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer sel = mock(AudioPlayer.class);
        AudioPlayer conf = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(sel);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(conf);

        MenuOptionsImpl menuOptions = new MenuOptionsImpl(false, true);
        Menu model = menuOptions;

        TestMenuController controller = new TestMenuController(model, audioManager);
        Game game = mock(Game.class);
        GUI gui = mock(GUI.class);
        when(game.getGui()).thenReturn(gui);
        when(game.getAudioManager()).thenReturn(audioManager);
        when(audioManager.getMasterVolume()).thenReturn(0.5f);

        controller.step(game, List.of(GUI.ACTION.RIGHT), 0);
        // newVolume = min(0.5+0.1,1.0) => 0.6 -> rounded 0.6
        verify(audioManager).setMasterVolume(0.6f);
        assertThat(menuOptions.getMasterVolume()).isEqualTo(0.6f);
        verify(gui).clear();
        verify(sel).playOnce();

        // test LEFT
        when(audioManager.getMasterVolume()).thenReturn(0.6f);
        controller.step(game, List.of(GUI.ACTION.LEFT), 0);
        verify(audioManager).setMasterVolume(0.5f);
        assertThat(menuOptions.getMasterVolume()).isEqualTo(0.5f);
    }

    @Test
    public void increment_and_decrement_resolution_cover_all_cases() {
        AudioManager audioManager = mock(AudioManager.class);
        AudioPlayer sel = mock(AudioPlayer.class);
        AudioPlayer conf = mock(AudioPlayer.class);
        when(audioManager.getAudio("menuSelect")).thenReturn(sel);
        when(audioManager.getAudio("menuConfirmSelection")).thenReturn(conf);

        Menu model = createSimpleMenu();
        TestMenuController controller = new TestMenuController(model, audioManager);

        GUI.SCREEN_RESOLUTION[] values = GUI.SCREEN_RESOLUTION.values();
        for (GUI.SCREEN_RESOLUTION v : values) {
            GUI.SCREEN_RESOLUTION inc = controller.callIncrement(v);
            GUI.SCREEN_RESOLUTION dec = controller.callDecrement(v);
            assertThat(inc).isNotNull();
            assertThat(dec).isNotNull();
        }
    }
}
