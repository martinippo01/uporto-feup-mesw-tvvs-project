package pt.feup.tvvs.pacman.viewer;

import pt.feup.tvvs.pacman.model.Element;
import pt.feup.tvvs.pacman.model.game.element.GhostGate;
import pt.feup.tvvs.pacman.model.game.element.Wall;
import pt.feup.tvvs.pacman.model.game.element.collectibles.*;
import pt.feup.tvvs.pacman.model.game.element.collectibles.*;
import pt.feup.tvvs.pacman.model.game.element.ghost.Blinky;
import pt.feup.tvvs.pacman.model.game.element.ghost.Clyde;
import pt.feup.tvvs.pacman.model.game.element.ghost.Inky;
import pt.feup.tvvs.pacman.model.game.element.ghost.Pinky;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;
import pt.feup.tvvs.pacman.model.menu.element.TextBox;
import pt.feup.tvvs.pacman.viewer.game.ElementViewer;
import pt.feup.tvvs.pacman.viewer.game.ImageLoader;
import pt.feup.tvvs.pacman.viewer.game.MovableElementViewer;
import pt.feup.tvvs.pacman.viewer.game.strategies.GhostStrategy;
import pt.feup.tvvs.pacman.viewer.game.strategies.PacmanStrategy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ViewerFactory {
    public static Map<Class<?>, Viewer<Element>> createArenaViewers() throws IOException {
        Map<Class<?>, Viewer<Element>> viewers = new HashMap<>();

        viewers.put(Wall.class, new ElementViewer(ImageLoader.loadTextImage("PNGs/wall.png")));
        viewers.put(GhostGate.class, new ElementViewer(ImageLoader.loadTextImage("PNGs/ghostgate.png")));
        viewers.put(Coin.class, new ElementViewer(ImageLoader.loadTextImage("PNGs/items/coin.png")));
        viewers.put(Apple.class, new ElementViewer(ImageLoader.loadTextImage("PNGs/items/apple.png")));
        viewers.put(Cherry.class, new ElementViewer(ImageLoader.loadTextImage("PNGs/items/cherry.png")));
        viewers.put(Key.class, new ElementViewer(ImageLoader.loadTextImage("PNGs/items/key.png")));
        viewers.put(Orange.class, new ElementViewer(ImageLoader.loadTextImage("PNGs/items/orange.png")));
        viewers.put(Strawberry.class, new ElementViewer(ImageLoader.loadTextImage("PNGs/items/strawberry.png")));
        viewers.put(PowerUp.class, new ElementViewer(ImageLoader.loadTextImage("PNGs/items/powerup.png")));

        viewers.put(TextBox.class, new TextBoxViewer(ImageLoader.loadFontImages()));

        viewers.put(Pacman.class, new MovableElementViewer(new PacmanStrategy(), ImageLoader.loadPacmanImages()));

        viewers.put(Blinky.class, new MovableElementViewer(new GhostStrategy(), ImageLoader.loadGhostImages("blinky")));
        viewers.put(Pinky.class, new MovableElementViewer(new GhostStrategy(), ImageLoader.loadGhostImages("pinky")));
        viewers.put(Inky.class, new MovableElementViewer(new GhostStrategy(), ImageLoader.loadGhostImages("inky")));
        viewers.put(Clyde.class, new MovableElementViewer(new GhostStrategy(), ImageLoader.loadGhostImages("clyde")));

        return viewers;
    }

    public static Map<Class<?>, Viewer<Element>> createMainMenuViewers() throws IOException {
        Map<Class<?>, Viewer<Element>> viewers = new HashMap<>();
        viewers.put(TextBox.class, new TextBoxViewer(ImageLoader.loadFontImages()));

        viewers.put(Pacman.class, new ElementViewer(ImageLoader.loadTextImage("PNGs/pacman/pacmanright.png")));

        viewers.put(Blinky.class, new MovableElementViewer(new GhostStrategy(), ImageLoader.loadGhostImages("blinky")));
        viewers.put(Pinky.class, new MovableElementViewer(new GhostStrategy(), ImageLoader.loadGhostImages("pinky")));
        viewers.put(Inky.class, new MovableElementViewer(new GhostStrategy(), ImageLoader.loadGhostImages("inky")));
        viewers.put(Clyde.class, new MovableElementViewer(new GhostStrategy(), ImageLoader.loadGhostImages("clyde")));

        return viewers;
    }

    public static Map<Class<?>, Viewer<Element>> createPauseMenuViewers() throws IOException {
        Map<Class<?>, Viewer<Element>> viewers = new HashMap<>();
        viewers.put(TextBox.class, new TextBoxViewer(ImageLoader.loadFontImages()));
        return viewers;
    }

    public static Map<Class<?>, Viewer<Element>> createMapSelectionMenuViewers() throws IOException {
        Map<Class<?>, Viewer<Element>> viewers = new HashMap<>();
        viewers.put(TextBox.class, new TextBoxViewer(ImageLoader.loadFontImages()));
        return viewers;
    }

    public static Map<Class<?>, Viewer<Element>> createAlertMenuViewers() throws IOException {
        return createArenaViewers();
    }
}
