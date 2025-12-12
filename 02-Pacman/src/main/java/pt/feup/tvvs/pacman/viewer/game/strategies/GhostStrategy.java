package pt.feup.tvvs.pacman.viewer.game.strategies;

import pt.feup.tvvs.pacman.model.Element;
import pt.feup.tvvs.pacman.model.game.element.ghost.Ghost;
import pt.feup.tvvs.pacman.model.game.element.ghost.GhostState;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public class GhostStrategy extends MultipleElementStrategy {
    @Override
    public BufferedImage getCurrentImage(Element element, Map<Character, List<BufferedImage>> images, long frameCount) {
        Ghost ghost = (Ghost) element;
        //first use the module operator to limit the value to only 20 different values, then assign the first ten to the first image and the rest to the second image
        int index = (ghost.getState() != GhostState.DEAD ? (frameCount % 20 < 10 ? 0 : 1) : 0);
        if (ghost.isScared()) return images.get('S').get(index);
        switch (ghost.getDirection()) {
            case UP:
                return images.get(ghost.getState() == GhostState.ALIVE ? 'U' : 'u').get(index);
            case DOWN:
                return images.get(ghost.getState() == GhostState.ALIVE ? 'D' : 'd').get(index);
            case RIGHT:
                return images.get(ghost.getState() == GhostState.ALIVE ? 'R' : 'r').get(index);
            case LEFT:
                return images.get(ghost.getState() == GhostState.ALIVE ? 'L' : 'l').get(index);
        }
        return null;
    }
}
