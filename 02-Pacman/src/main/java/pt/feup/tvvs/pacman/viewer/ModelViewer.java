package pt.feup.tvvs.pacman.viewer;

import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Element;

import java.io.IOException;
import java.util.Map;

public abstract class ModelViewer<T> extends Viewer<T> {
    private final Map<Class<?>, Viewer<Element>> viewers;
    private boolean initialClear;

    public ModelViewer(Map<Class<?>, Viewer<Element>> viewers) {
        this.viewers = viewers;
        this.initialClear = false;
    }

    public void drawElement(GUI gui, Element element, long frameCount) {
        Viewer<Element> viewer = viewers.get(element.getClass());
        if (viewer != null) {
            viewer.drawElement(gui, element, frameCount);
        }
    }

    public abstract void drawElements(GUI gui, T model, long frameCount);

    @Override
    public void drawElement(GUI gui, T model, long frameCount) {
        if (!initialClear) {
            gui.clear();
            initialClear = true;
        }
        drawElements(gui, model, frameCount);
        try {
            gui.refresh();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
