package pt.feup.tvvs.pacman.states.menu;

import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.controller.Controller;
import pt.feup.tvvs.pacman.controller.menu.PauseMenuController;
import pt.feup.tvvs.pacman.model.menu.PauseMenu;
import pt.feup.tvvs.pacman.states.State;
import pt.feup.tvvs.pacman.viewer.Viewer;
import pt.feup.tvvs.pacman.viewer.menu.PauseMenuViewer;

import java.io.IOException;
import java.net.URISyntaxException;

public class PauseMenuState extends State<PauseMenu> {
    public PauseMenuState(PauseMenu pauseMenu, AudioManager audioManager) throws IOException, URISyntaxException {
        super(pauseMenu, audioManager);
    }

    @Override
    public Viewer<PauseMenu> createViewer() throws IOException {
        return new PauseMenuViewer();
    }

    @Override
    public Controller<PauseMenu> createController(AudioManager audioManager) {
        return new PauseMenuController(getModel(), audioManager);
    }
}
