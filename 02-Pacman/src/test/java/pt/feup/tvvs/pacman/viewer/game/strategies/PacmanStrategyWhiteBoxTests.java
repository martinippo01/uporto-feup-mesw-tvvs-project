package pt.feup.tvvs.pacman.viewer.game.strategies;

import org.junit.jupiter.api.Test;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PacmanStrategyWhiteBoxTests {

    private Map<Character, List<BufferedImage>> makeImages() {
        Map<Character, List<BufferedImage>> images = new HashMap<>();
        for (char c : new char[]{'X','U','D','R','L'}) {
            List<BufferedImage> list = new ArrayList<>();
            BufferedImage img0 = new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB);
            img0.setRGB(0,0,c);
            BufferedImage img1 = new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB);
            img1.setRGB(0,0,c+1);
            list.add(img0);
            list.add(img1);
            images.put(c, list);
        }
        return images;
    }

    @Test
    public void dyingPacman_uses_X_image() {
        Pacman pacman = new Pacman(new Position(0,0));
        pacman.setDying(true);

        Map<Character, List<BufferedImage>> images = makeImages();
        PacmanStrategy strategy = new PacmanStrategy();

        BufferedImage img = strategy.getCurrentImage(pacman, images, 0L);
        assertThat(img).isSameAs(images.get('X').get(0));
    }

    @Test
    public void directionBranches_return_expected_images_with_indexes() {
        Pacman pacman = new Pacman(new Position(1,1));
        pacman.setDying(false);

        Map<Character, List<BufferedImage>> images = makeImages();
        PacmanStrategy strategy = new PacmanStrategy();

        // UP
        pacman.setDirection(pt.feup.tvvs.pacman.model.game.element.Direction.UP);
        assertThat(strategy.getCurrentImage(pacman, images, 0L)).isSameAs(images.get('U').get(0));
        assertThat(strategy.getCurrentImage(pacman, images, 11L)).isSameAs(images.get('U').get(1));

        // DOWN
        pacman.setDirection(pt.feup.tvvs.pacman.model.game.element.Direction.DOWN);
        assertThat(strategy.getCurrentImage(pacman, images, 0L)).isSameAs(images.get('D').get(0));
        assertThat(strategy.getCurrentImage(pacman, images, 19L)).isSameAs(images.get('D').get(1));

        // RIGHT
        pacman.setDirection(pt.feup.tvvs.pacman.model.game.element.Direction.RIGHT);
        assertThat(strategy.getCurrentImage(pacman, images, 0L)).isSameAs(images.get('R').get(0));
        assertThat(strategy.getCurrentImage(pacman, images, 12L)).isSameAs(images.get('R').get(1));

        // LEFT
        pacman.setDirection(pt.feup.tvvs.pacman.model.game.element.Direction.LEFT);
        assertThat(strategy.getCurrentImage(pacman, images, 0L)).isSameAs(images.get('L').get(0));
        assertThat(strategy.getCurrentImage(pacman, images, 18L)).isSameAs(images.get('L').get(1));
    }

    @Test
    public void uses_map_and_list_get_with_expected_keys_and_index_when_mocked() {
        Pacman pacman = new Pacman(new Position(2,2));
        pacman.setDying(false);
        pacman.setDirection(pt.feup.tvvs.pacman.model.game.element.Direction.UP);

        @SuppressWarnings("unchecked")
        Map<Character, List<BufferedImage>> images = (Map<Character, List<BufferedImage>>) mock(Map.class);
        @SuppressWarnings("unchecked")
        List<BufferedImage> list = (List<BufferedImage>) mock(List.class);
        BufferedImage fake = new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);

        when(images.get('U')).thenReturn(list);
        when(list.get(1)).thenReturn(fake);

        PacmanStrategy strategy = new PacmanStrategy();
        // use frameCount such that index=1
        BufferedImage result = strategy.getCurrentImage(pacman, images, 11L);

        assertThat(result).isSameAs(fake);
        verify(images).get('U');
        verify(list).get(1);
    }
}
