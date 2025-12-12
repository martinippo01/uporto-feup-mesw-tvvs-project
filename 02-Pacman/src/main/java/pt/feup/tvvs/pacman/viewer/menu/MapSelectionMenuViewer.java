package pt.feup.tvvs.pacman.viewer.menu;

import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.menu.MapSelectionMenu;
import pt.feup.tvvs.pacman.viewer.ModelViewer;
import pt.feup.tvvs.pacman.viewer.ViewerFactory;

import java.io.IOException;

public class MapSelectionMenuViewer extends ModelViewer<MapSelectionMenu> {

    public MapSelectionMenuViewer() throws IOException {
        super(ViewerFactory.createMapSelectionMenuViewers());
    }

    @Override
    public void drawElements(GUI gui, MapSelectionMenu menu, long frameCount) {
        menu.getOptions().forEach(textBox -> drawElement(gui, textBox, frameCount));
        drawElement(gui, menu.getTitle(), frameCount);
    }

}
