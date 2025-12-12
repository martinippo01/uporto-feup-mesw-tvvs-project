package pt.feup.tvvs.pacman.states;

import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.controller.Controller;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.viewer.Viewer;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public abstract class State<T> {
    private final T model;
    private final Viewer<T> viewer;
    private final Controller<T> controller;
    private final AudioManager audioManager;

    public State(T model, AudioManager audioManager) throws IOException, URISyntaxException {
        this.model = model;
        this.viewer = createViewer();
        this.controller = createController(audioManager);
        this.audioManager = audioManager;
    }

    public T getModel() {
        return model;
    }

    public abstract Viewer<T> createViewer() throws IOException;

    public abstract Controller<T> createController(AudioManager audioManager);

    public void step(Game game, GUI gui, long frameTime) throws IOException, URISyntaxException, FontFormatException {
        List<GUI.ACTION> actions = gui.getNextAction();
        controller.step(game, actions, frameTime);
        viewer.drawElement(gui, model, frameTime);
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }
}
