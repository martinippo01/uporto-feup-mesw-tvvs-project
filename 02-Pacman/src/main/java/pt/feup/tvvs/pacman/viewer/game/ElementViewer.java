package pt.feup.tvvs.pacman.viewer.game;

import com.googlecode.lanterna.graphics.BasicTextImage;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Element;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.viewer.Viewer;

public class ElementViewer extends Viewer<Element> {
    private final BasicTextImage image;

    public ElementViewer(BasicTextImage image) {
        this.image = image;
    }

    @Override
    public void drawElement(GUI gui, Element element, long frameCount) {
        Position drawPos = new Position(element.getPosition().getX() * 11, element.getPosition().getY() * 11);
        gui.drawImage(drawPos, image);
    }
}
