package pt.feup.tvvs.pacman.states.game;

import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.controller.Controller;
import pt.feup.tvvs.pacman.controller.game.DyingStateController;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.states.State;
import pt.feup.tvvs.pacman.viewer.Viewer;
import pt.feup.tvvs.pacman.viewer.game.ArenaViewer;

import java.io.IOException;
import java.net.URISyntaxException;

public class DyingState extends State<Arena> {
    public DyingState(Arena model, AudioManager audioManager) throws IOException, URISyntaxException {
        super(model, audioManager);
    }

    @Override
    public Viewer<Arena> createViewer() throws IOException {
        return new ArenaViewer();
    }

    @Override
    public Controller<Arena> createController(AudioManager audioManager) {
        return new DyingStateController(getModel(), audioManager);
    }

}
