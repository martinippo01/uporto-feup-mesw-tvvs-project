package pt.feup.tvvs.pacman.states.menu;

import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.controller.Controller;
import pt.feup.tvvs.pacman.controller.menu.MainMenuController;
import pt.feup.tvvs.pacman.model.menu.MainMenu;
import pt.feup.tvvs.pacman.states.State;
import pt.feup.tvvs.pacman.viewer.Viewer;
import pt.feup.tvvs.pacman.viewer.menu.MainMenuViewer;

import java.io.IOException;
import java.net.URISyntaxException;

public class MainMenuState extends State<MainMenu> {

    public MainMenuState(MainMenu model, AudioManager audioManager) throws IOException, URISyntaxException {
        super(model, audioManager);
    }

    @Override
    public Viewer<MainMenu> createViewer() throws IOException {
        return new MainMenuViewer();
    }

    @Override
    public Controller<MainMenu> createController(AudioManager audioManager) {
        return new MainMenuController(getModel(), audioManager);
    }
}
