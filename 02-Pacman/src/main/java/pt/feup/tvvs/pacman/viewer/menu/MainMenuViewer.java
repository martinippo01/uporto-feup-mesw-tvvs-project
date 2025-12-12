package pt.feup.tvvs.pacman.viewer.menu;

import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.menu.MainMenu;
import pt.feup.tvvs.pacman.viewer.ModelViewer;
import pt.feup.tvvs.pacman.viewer.ViewerFactory;

import java.io.IOException;

public class MainMenuViewer extends ModelViewer<MainMenu> {

    public MainMenuViewer() throws IOException {
        super(ViewerFactory.createMainMenuViewers());
    }

    @Override
    public void drawElements(GUI gui, MainMenu menu, long frameCount) {
        menu.getBlankPositions().forEach(position -> gui.erase(new Position(position.getX() * 11, position.getY() * 11)));
        menu.getOptions().forEach(textBox -> drawElement(gui, textBox, frameCount));
        drawElement(gui, menu.getPacman(), frameCount);

        drawElement(gui, menu.getBlinky(), frameCount);
        drawElement(gui, menu.getPinky(), frameCount);
        drawElement(gui, menu.getInky(), frameCount);
        drawElement(gui, menu.getClyde(), frameCount);

        drawElement(gui, menu.getTitle(), frameCount);
    }

}
