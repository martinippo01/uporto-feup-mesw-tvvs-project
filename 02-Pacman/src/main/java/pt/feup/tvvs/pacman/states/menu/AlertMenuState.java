package pt.feup.tvvs.pacman.states.menu;

import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.controller.Controller;
import pt.feup.tvvs.pacman.controller.menu.AlertMenuController;
import pt.feup.tvvs.pacman.model.menu.AlertMenu;
import pt.feup.tvvs.pacman.states.State;
import pt.feup.tvvs.pacman.viewer.Viewer;
import pt.feup.tvvs.pacman.viewer.menu.AlertMenuViewer;

import java.io.IOException;
import java.net.URISyntaxException;

public class AlertMenuState extends State<AlertMenu> {
    public AlertMenuState(AlertMenu model, AudioManager audioManager) throws IOException, URISyntaxException {
        super(model, audioManager);
    }

    @Override
    public Viewer<AlertMenu> createViewer() throws IOException {
        return new AlertMenuViewer(getModel().getAlertFilePath());
    }

    @Override
    public Controller<AlertMenu> createController(AudioManager audioManager) {
        return new AlertMenuController(getModel(), audioManager);
    }
}
