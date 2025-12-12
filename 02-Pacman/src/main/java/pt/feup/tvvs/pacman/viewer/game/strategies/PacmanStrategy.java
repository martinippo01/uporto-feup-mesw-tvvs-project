package pt.feup.tvvs.pacman.viewer.game.strategies;

import pt.feup.tvvs.pacman.model.Element;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;


public class PacmanStrategy extends MultipleElementStrategy {
    @Override
    public BufferedImage getCurrentImage(Element element, Map<Character, List<BufferedImage>> images, long frameCount) {
        Pacman pacman = (Pacman) element;
        //first use the module operator to limit the value to only 20 different values, then assign the first ten to the first image and the rest to the second image
        int index = (frameCount % 20 < 10 ? 0 : 1);
        if (pacman.isDying())
            return images.get('X').get(0);

        switch (pacman.getDirection()) {
            case UP:
                return images.get('U').get(index);
            case DOWN:
                return images.get('D').get(index);
            case RIGHT:
                return images.get('R').get(index);
            case LEFT:
                return images.get('L').get(index);
        }
        return null;
    }
}
