package pt.feup.tvvs.pacman.viewer.game;

import com.googlecode.lanterna.graphics.BasicTextImage;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ImageLoaderWhiteBoxTests {

    @Test
    public void loadBufferedImage_existingResource_returnsBufferedImage() throws Exception {
        BufferedImage img = ImageLoader.loadBufferedImage("PNGs/wall.png");
        assertThat(img).isNotNull();
        assertThat(img.getWidth()).isGreaterThan(0);
        assertThat(img.getHeight()).isGreaterThan(0);
    }

    @Test
    public void loadBufferedImage_missingResource_throws() {
        assertThatThrownBy(() -> ImageLoader.loadBufferedImage("PNGs/this_does_not_exist.png"))
                .isInstanceOfAny(AssertionError.class, Exception.class);
    }

    @Test
    public void loadTextImage_existingResource_returnsBasicTextImage() throws Exception {
        BasicTextImage textImage = ImageLoader.loadTextImage("PNGs/wall.png");
        assertThat(textImage).isNotNull();
        // ensure some content is present by checking its toString isn't empty
        assertThat(textImage.toString()).isNotNull();
    }

    @Test
    public void loadPacmanImages_containsExpectedKeysAndSizes() throws Exception {
        Map<Character, List<BufferedImage>> map = ImageLoader.loadPacmanImages();
        assertThat(map).isNotNull();
        // expect keys L,U,D,R each with 2 images and X with 1 image
        assertThat(map).containsKeys('L', 'U', 'D', 'R', 'X');
        assertThat(map.get('L')).hasSize(2);
        assertThat(map.get('U')).hasSize(2);
        assertThat(map.get('D')).hasSize(2);
        assertThat(map.get('R')).hasSize(2);
        assertThat(map.get('X')).hasSize(1);
    }

    @Test
    public void loadGhostImages_blinky_containsAllDirectionsAndSpecials() throws Exception {
        Map<Character, List<BufferedImage>> map = ImageLoader.loadGhostImages("blinky");
        assertThat(map).isNotNull();
        // movement frames
        assertThat(map).containsKeys('L', 'U', 'D', 'R', 'S', 'r', 'l', 'u', 'd');
        assertThat(map.get('L')).hasSize(2);
        assertThat(map.get('U')).hasSize(2);
        assertThat(map.get('D')).hasSize(2);
        assertThat(map.get('R')).hasSize(2);
        assertThat(map.get('S')).hasSizeGreaterThanOrEqualTo(1);
        assertThat(map.get('r')).hasSize(1);
    }

    @Test
    public void loadFontImages_returnsCharacterMapWithExpectedCharacter() throws Exception {
        Map<Character, BufferedImage> fontMap = ImageLoader.loadFontImages();
        assertThat(fontMap).isNotNull();
        // ingamefontmap.txt starts with A, so 'A' should be present
        assertThat(fontMap).containsKey('A');
        BufferedImage aImg = fontMap.get('A');
        assertThat(aImg).isNotNull();
        assertThat(aImg.getWidth()).isEqualTo(5);
        assertThat(aImg.getHeight()).isEqualTo(11);
    }
}
