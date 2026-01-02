package pt.feup.tvvs.pacman.viewer.game.strategies;

import org.junit.jupiter.api.Test;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.element.ghost.Blinky;
import pt.feup.tvvs.pacman.model.game.element.ghost.GhostState;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class GhostStrategyWhiteBoxTests {

    private Map<Character, List<BufferedImage>> makeImages() {
        Map<Character, List<BufferedImage>> images = new HashMap<>();
        // helper to create two distinct images for each character
        for (char c : new char[]{'S','U','D','R','L','r','l','u','d'}) {
            List<BufferedImage> list = new ArrayList<>();
            // image 0
            BufferedImage img0 = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
            img0.setRGB(0,0,c);
            // image 1
            BufferedImage img1 = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
            img1.setRGB(0,0,c + 1);
            list.add(img0);
            list.add(img1);
            images.put(c, list);
        }
        // Also add lowercase r/l/u/d uppercase maps if missing
        if (!images.containsKey('L')) images.put('L', images.get('l'));
        if (!images.containsKey('R')) images.put('R', images.get('r'));
        if (!images.containsKey('U')) images.put('U', images.get('u'));
        if (!images.containsKey('D')) images.put('D', images.get('d'));
        return images;
    }

    @Test
    public void scaredGhost_uses_S_images_with_correct_index() {
        Blinky ghost = new Blinky(new Position(1,1));
        ghost.setState(GhostState.SCARED);

        Map<Character, List<BufferedImage>> images = makeImages();
        GhostStrategy strategy = new GhostStrategy();

        BufferedImage img0 = strategy.getCurrentImage(ghost, images, 0L);
        BufferedImage expected0 = images.get('S').get(0);
        assertThat(img0).isSameAs(expected0);

        BufferedImage img1 = strategy.getCurrentImage(ghost, images, 15L); // frameCount%20 = 15 -> index 1
        BufferedImage expected1 = images.get('S').get(1);
        assertThat(img1).isSameAs(expected1);
    }

    @Test
    public void aliveGhost_directions_map_to_uppercase_keys_and_indexes() {
        Blinky ghost = new Blinky(new Position(1,1));
        ghost.setState(GhostState.ALIVE);

        Map<Character, List<BufferedImage>> images = makeImages();
        GhostStrategy strategy = new GhostStrategy();

        // UP
        ghost.setDirection(pt.feup.tvvs.pacman.model.game.element.Direction.UP);
        assertThat(strategy.getCurrentImage(ghost, images, 0L)).isSameAs(images.get('U').get(0));
        assertThat(strategy.getCurrentImage(ghost, images, 11L)).isSameAs(images.get('U').get(1));

        // DOWN
        ghost.setDirection(pt.feup.tvvs.pacman.model.game.element.Direction.DOWN);
        assertThat(strategy.getCurrentImage(ghost, images, 0L)).isSameAs(images.get('D').get(0));
        assertThat(strategy.getCurrentImage(ghost, images, 19L)).isSameAs(images.get('D').get(1));

        // RIGHT
        ghost.setDirection(pt.feup.tvvs.pacman.model.game.element.Direction.RIGHT);
        assertThat(strategy.getCurrentImage(ghost, images, 0L)).isSameAs(images.get('R').get(0));
        assertThat(strategy.getCurrentImage(ghost, images, 12L)).isSameAs(images.get('R').get(1));

        // LEFT
        ghost.setDirection(pt.feup.tvvs.pacman.model.game.element.Direction.LEFT);
        assertThat(strategy.getCurrentImage(ghost, images, 0L)).isSameAs(images.get('L').get(0));
        assertThat(strategy.getCurrentImage(ghost, images, 18L)).isSameAs(images.get('L').get(1));
    }

    @Test
    public void deadGhost_uses_lowercase_keys_and_index_zero() {
        Blinky ghost = new Blinky(new Position(2,2));
        ghost.setState(GhostState.DEAD);

        Map<Character, List<BufferedImage>> images = makeImages();
        GhostStrategy strategy = new GhostStrategy();

        // set various directions - index should be 0 regardless of frameCount because dead branch sets index=0
        ghost.setDirection(pt.feup.tvvs.pacman.model.game.element.Direction.UP);
        assertThat(strategy.getCurrentImage(ghost, images, 15L)).isSameAs(images.get('u').get(0));

        ghost.setDirection(pt.feup.tvvs.pacman.model.game.element.Direction.DOWN);
        assertThat(strategy.getCurrentImage(ghost, images, 19L)).isSameAs(images.get('d').get(0));

        ghost.setDirection(pt.feup.tvvs.pacman.model.game.element.Direction.RIGHT);
        assertThat(strategy.getCurrentImage(ghost, images, 7L)).isSameAs(images.get('r').get(0));

        ghost.setDirection(pt.feup.tvvs.pacman.model.game.element.Direction.LEFT);
        assertThat(strategy.getCurrentImage(ghost, images, 9L)).isSameAs(images.get('l').get(0));
    }

    @Test
    public void uses_map_and_list_get_with_expected_keys_and_index_when_mocked() {
        // Use Mockito to verify interactions with the images map when ghost is alive and direction UP
        Blinky ghost = new Blinky(new Position(3,3));
        ghost.setState(GhostState.ALIVE);
        ghost.setDirection(pt.feup.tvvs.pacman.model.game.element.Direction.UP);

        @SuppressWarnings("unchecked")
        Map<Character, List<BufferedImage>> images = (Map<Character, List<BufferedImage>>) mock(Map.class);
        @SuppressWarnings("unchecked")
        List<BufferedImage> list = (List<BufferedImage>) mock(List.class);
        BufferedImage fake = new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);

        when(images.get('U')).thenReturn(list);
        when(list.get(0)).thenReturn(fake);

        GhostStrategy strategy = new GhostStrategy();
        BufferedImage result = strategy.getCurrentImage(ghost, images, 0L);

        assertThat(result).isSameAs(fake);
        verify(images).get('U');
        verify(list).get(0);
    }
}
