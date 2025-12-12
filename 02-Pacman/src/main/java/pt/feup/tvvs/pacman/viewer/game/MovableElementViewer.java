package pt.feup.tvvs.pacman.viewer.game;

import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Element;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.element.MovableElement;
import pt.feup.tvvs.pacman.viewer.Viewer;
import pt.feup.tvvs.pacman.viewer.game.strategies.MultipleElementStrategy;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public class MovableElementViewer extends Viewer<Element> {
    private final Map<Character, List<BufferedImage>> images;
    private final MultipleElementStrategy strategy;

    public MovableElementViewer(MultipleElementStrategy strategy, Map<Character, List<BufferedImage>> images) {
        this.images = images;
        this.strategy = strategy;
    }

    @Override
    public void drawElement(GUI gui, Element element, long frameCount) {
        BufferedImage image = strategy.getCurrentImage(element, images, frameCount);
        assert image != null;
        MovableElement movableElement = (MovableElement) element;
        Position drawPos = movableElement.getRealPosition();
        gui.drawImage(drawPos, image);
    }
}
