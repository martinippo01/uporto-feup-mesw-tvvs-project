package pt.feup.tvvs.pacman.viewer.game;

import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.BasicTextImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ImageLoader {

    public static Map<Character, List<BufferedImage>> loadGhostImages(String ghostName) throws IOException {
        BufferedImage leftResource1 = loadBufferedImage("PNGs/ghosts/" + ghostName + "/" + ghostName + "left1.png");
        BufferedImage upResource1 = loadBufferedImage("PNGs/ghosts/" + ghostName + "/" + ghostName + "up1.png");
        BufferedImage downResource1 = loadBufferedImage("PNGs/ghosts/" + ghostName + "/" + ghostName + "down1.png");
        BufferedImage rightResource1 = loadBufferedImage("PNGs/ghosts/" + ghostName + "/" + ghostName + "right1.png");
        BufferedImage leftResource2 = loadBufferedImage("PNGs/ghosts/" + ghostName + "/" + ghostName + "left2.png");
        BufferedImage upResource2 = loadBufferedImage("PNGs/ghosts/" + ghostName + "/" + ghostName + "up2.png");
        BufferedImage downResource2 = loadBufferedImage("PNGs/ghosts/" + ghostName + "/" + ghostName + "down2.png");
        BufferedImage rightResource2 = loadBufferedImage("PNGs/ghosts/" + ghostName + "/" + ghostName + "right2.png");
        BufferedImage scaredResource1 = loadBufferedImage("PNGs/ghosts/common/scaredghost1.png");
        BufferedImage scaredResource2 = loadBufferedImage("PNGs/ghosts/common/scaredghost2.png");
        BufferedImage deadRightResource = loadBufferedImage("PNGs/ghosts/common/deadghostright.png");
        BufferedImage deadLeftResource = loadBufferedImage("PNGs/ghosts/common/deadghostleft.png");
        BufferedImage deadUpResource = loadBufferedImage("PNGs/ghosts/common/deadghostup.png");
        BufferedImage deadDownResource = loadBufferedImage("PNGs/ghosts/common/deadghostdown.png");


        return Map.of(
                'L', Arrays.asList(leftResource1, leftResource2),
                'U', Arrays.asList(upResource1, upResource2),
                'D', Arrays.asList(downResource1, downResource2),
                'R', Arrays.asList(rightResource1, rightResource2),
                'S', Arrays.asList(scaredResource1, scaredResource2),
                'r', Collections.singletonList(deadRightResource),
                'l', Collections.singletonList(deadLeftResource),
                'u', Collections.singletonList(deadUpResource),
                'd', Collections.singletonList(deadDownResource)
        );
    }

    public static Map<Character, List<BufferedImage>> loadPacmanImages() throws IOException {
        BufferedImage leftResource = loadBufferedImage("PNGs/pacman/pacmanleft.png");
        BufferedImage upResource = loadBufferedImage("PNGs/pacman/pacmanup.png");
        BufferedImage downResource = loadBufferedImage("PNGs/pacman/pacmandown.png");
        BufferedImage rightResource = loadBufferedImage("PNGs/pacman/pacmanright.png");
        BufferedImage closedResource = loadBufferedImage("PNGs/pacman/pacmanclosed.png");
        BufferedImage deadResource = loadBufferedImage("PNGs/pacman/pacmandead.png");

        return Map.of(
                'L', Arrays.asList(leftResource, closedResource),
                'U', Arrays.asList(upResource, closedResource),
                'D', Arrays.asList(downResource, closedResource),
                'R', Arrays.asList(rightResource, closedResource),
                'X', Collections.singletonList(deadResource)
        );
    }


    public static BufferedImage loadBufferedImage(String filePath) throws IOException {
        URL resource = ImageLoader.class.getClassLoader().getResource(filePath);
        assert resource != null;
        return ImageIO.read(resource);
    }

    public static BasicTextImage loadTextImage(String filePath) throws IOException {
        URL resource = ImageLoader.class.getClassLoader().getResource(filePath);
        assert resource != null;
        return toTextImage(ImageIO.read(resource));
    }

    private static BasicTextImage toTextImage(BufferedImage image) {
        BasicTextImage textImage = new BasicTextImage(11, 11);

        for (int y = 0; y < 11; y++) {
            for (int x = 0; x < 11; x++) {
                if (image.getRGB(x, y) == 0) continue;

                int RGB = image.getRGB(x, y);
                int red = RGB >> 16 & 0xFF;
                int green = RGB >> 8 & 0xFF;
                int blue = RGB & 0xFF;

                textImage.setCharacterAt(x, y, TextCharacter.fromCharacter(' ', null, new TextColor.RGB(red, green, blue))[0]);
            }
        }
        return textImage;
    }

    public static Map<Character, BufferedImage> loadFontImages() throws IOException {
        InputStream fontMapResource = ImageLoader.class.getClassLoader().getResourceAsStream("Fonts/ingamefontmap.txt");
        URL fontResource = ImageLoader.class.getClassLoader().getResource("Fonts/ingamefont.png");
        assert fontMapResource != null;
        assert fontResource != null;

        Map<Character, BufferedImage> characters = new HashMap<>();
        String fontMap = new Scanner(fontMapResource, StandardCharsets.UTF_8).next();
        fontMapResource.close();
        BufferedImage font = ImageIO.read(fontResource);

        int x = 0, y = 0;
        for (int i = 0; i < fontMap.length(); ++i) {
            //for each character on the font map, load a 5*11 subimage which is the corresponding letter
            characters.put(fontMap.charAt(i), font.getSubimage(x, y, 5, 11));
            x += 5;
        }

        return characters;
    }
}
