package pt.feup.tvvs.pacman.controller.menu;

import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.audio.AudioPlayer;
import pt.feup.tvvs.pacman.controller.Controller;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.menu.Menu;
import pt.feup.tvvs.pacman.model.menu.MenuOptions;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public abstract class MenuController<T extends Menu> extends Controller<T> {
    protected final AudioPlayer menuSelect;
    protected final AudioPlayer menuConfirmSelection;

    public MenuController(T model, AudioManager audioManager) {
        super(model);

        audioManager.addAudio("menuSelect", "Audio/menuSelect.wav");
        this.menuSelect = audioManager.getAudio("menuSelect");
        this.menuSelect.setVolume(0.25f);

        audioManager.addAudio("menuConfirmSelection", "Audio/menuConfirmSelection.wav");
        this.menuConfirmSelection = audioManager.getAudio("menuConfirmSelection");
        this.menuConfirmSelection.setVolume(0.2f);
    }

    protected float handleVolumeChange(Game game) {
        game.getGui().clear();
        float newVolume = game.getAudioManager().getMasterVolume() == 1f ? 0.1f
                : Math.round((game.getAudioManager().getMasterVolume() + 0.1f) * 10) / 10f;

        game.getAudioManager().setMasterVolume(newVolume);
        return newVolume;
    }

    private void handleArrowKeyAction(Game game, GUI.ACTION action, MenuOptions menuOptions) throws URISyntaxException, IOException, FontFormatException {
        if (menuOptions.ResolutionSelected()) {
            GUI.SCREEN_RESOLUTION newResolution = action == GUI.ACTION.RIGHT
                    ? incrementResolution(game.getResolution())
                    : decrementResolution(game.getResolution());
            game.setResolution(newResolution);
            menuOptions.setResolution(newResolution);
            menuSelect.playOnce();
        } else if (menuOptions.MasterVolumeSelected()) {
            float currentVolume = game.getAudioManager().getMasterVolume();
            float newVolume = currentVolume;

            if (action == GUI.ACTION.RIGHT) {
                newVolume = Math.min(currentVolume + 0.1f, 1.0f);
            } else if (action == GUI.ACTION.LEFT) {
                newVolume = Math.max(currentVolume - 0.1f, 0.1f);
            }

            newVolume = Math.round(newVolume * 10) / 10f;

            game.getAudioManager().setMasterVolume(newVolume);
            menuOptions.setMasterVolume(newVolume);

            game.getGui().clear();

            menuSelect.playOnce();
        }
    }

    protected GUI.SCREEN_RESOLUTION incrementResolution(GUI.SCREEN_RESOLUTION current) {
        switch (current) {
            case _360p:
                return GUI.SCREEN_RESOLUTION._540p;
            case _540p:
                return GUI.SCREEN_RESOLUTION._720p;
            case _720p:
                return GUI.SCREEN_RESOLUTION._900p;
            case _900p:
                return GUI.SCREEN_RESOLUTION._1080p;
            case _1080p:
                return GUI.SCREEN_RESOLUTION._1440p;
            case _1440p:
                return GUI.SCREEN_RESOLUTION._2160p;
            case _2160p:
                return GUI.SCREEN_RESOLUTION._360p;
        }
        return null;
    }

    protected GUI.SCREEN_RESOLUTION decrementResolution(GUI.SCREEN_RESOLUTION current) {
        switch (current) {
            case _360p:
                return GUI.SCREEN_RESOLUTION._2160p;
            case _540p:
                return GUI.SCREEN_RESOLUTION._360p;
            case _720p:
                return GUI.SCREEN_RESOLUTION._540p;
            case _900p:
                return GUI.SCREEN_RESOLUTION._720p;
            case _1080p:
                return GUI.SCREEN_RESOLUTION._900p;
            case _1440p:
                return GUI.SCREEN_RESOLUTION._1080p;
            case _2160p:
                return GUI.SCREEN_RESOLUTION._1440p;
        }
        return null;
    }

    @Override
    public void step(Game game, List<GUI.ACTION> actions, long time) throws URISyntaxException, IOException, FontFormatException {
        for (GUI.ACTION action : actions) {
            if (action == GUI.ACTION.UP) {
                menuSelect.playOnce();
                getModel().selectPreviousOption();
            } else if (action == GUI.ACTION.DOWN) {
                menuSelect.playOnce();
                getModel().selectNextOption();
            } else if (action == GUI.ACTION.LEFT || action == GUI.ACTION.RIGHT) {
                if (getModel() instanceof MenuOptions) {
                    MenuOptions menuOptions = (MenuOptions) getModel();
                    handleArrowKeyAction(game, action, menuOptions);
                }
            }
        }
    }
}
