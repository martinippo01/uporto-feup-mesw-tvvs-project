package pt.feup.tvvs.pacman.viewer.menu;

import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.menu.PauseMenu;
import pt.feup.tvvs.pacman.viewer.ModelViewer;
import pt.feup.tvvs.pacman.viewer.ViewerFactory;

import java.io.IOException;

public class PauseMenuViewer extends ModelViewer<PauseMenu> {

    public PauseMenuViewer() throws IOException {
        super(ViewerFactory.createPauseMenuViewers());
    }

    @Override
    public void drawElements(GUI gui, PauseMenu menu, long frameCount) {
        menu.getOptions().forEach(textBox -> drawElement(gui, textBox, frameCount));
        drawElement(gui, menu.getPauseSign(), frameCount);
        drawElement(gui, menu.getTitle(), frameCount);
    }

}
