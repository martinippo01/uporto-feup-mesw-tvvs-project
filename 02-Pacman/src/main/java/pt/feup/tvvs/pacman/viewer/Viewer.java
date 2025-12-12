package pt.feup.tvvs.pacman.viewer;

import pt.feup.tvvs.pacman.gui.GUI;

public abstract class Viewer<T> {
    public abstract void drawElement(GUI gui, T model, long frameCount);
}

