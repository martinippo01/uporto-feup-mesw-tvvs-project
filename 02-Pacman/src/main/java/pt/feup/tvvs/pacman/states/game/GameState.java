package pt.feup.tvvs.pacman.states.game;

import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.controller.Controller;
import pt.feup.tvvs.pacman.controller.game.ArenaController;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.states.State;
import pt.feup.tvvs.pacman.viewer.Viewer;
import pt.feup.tvvs.pacman.viewer.game.ArenaViewer;

import java.io.IOException;
import java.net.URISyntaxException;

public class GameState extends State<Arena> {

    public GameState(Arena model, AudioManager audioManager) throws IOException, URISyntaxException {
        super(model, audioManager);
    }

    @Override
    public Viewer<Arena> createViewer() throws IOException {
        return new ArenaViewer();
    }

    @Override
    public Controller<Arena> createController(AudioManager audioManager) {
        return new ArenaController(getModel(), audioManager);
    }
}
