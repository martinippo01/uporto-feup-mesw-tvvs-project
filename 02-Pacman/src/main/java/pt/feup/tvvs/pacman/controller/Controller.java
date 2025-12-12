package pt.feup.tvvs.pacman.controller;

import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.gui.GUI;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public abstract class Controller<T> {
    protected final T model;

    public Controller(T model) {
        this.model = model;
    }

    public T getModel() {
        return model;
    }

    public abstract void step(Game game, List<GUI.ACTION> actions, long time) throws IOException, URISyntaxException, FontFormatException;
}
