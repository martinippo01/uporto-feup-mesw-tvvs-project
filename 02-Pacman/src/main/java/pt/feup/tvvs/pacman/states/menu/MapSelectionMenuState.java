package pt.feup.tvvs.pacman.states.menu;

import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.controller.Controller;
import pt.feup.tvvs.pacman.controller.menu.MapSelectionMenuController;
import pt.feup.tvvs.pacman.model.menu.MapSelectionMenu;
import pt.feup.tvvs.pacman.states.State;
import pt.feup.tvvs.pacman.viewer.Viewer;
import pt.feup.tvvs.pacman.viewer.menu.MapSelectionMenuViewer;

import java.io.IOException;
import java.net.URISyntaxException;

public class MapSelectionMenuState extends State<MapSelectionMenu> {
    public MapSelectionMenuState(MapSelectionMenu model, AudioManager audioManager) throws IOException, URISyntaxException {
        super(model, audioManager);
    }

    @Override
    public Viewer<MapSelectionMenu> createViewer() throws IOException {
        return new MapSelectionMenuViewer();
    }

    @Override
    public Controller<MapSelectionMenu> createController(AudioManager audioManager) {
        return new MapSelectionMenuController(getModel(), audioManager);
    }
}
