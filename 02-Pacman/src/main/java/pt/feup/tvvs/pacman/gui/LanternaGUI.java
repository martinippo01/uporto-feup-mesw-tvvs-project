package pt.feup.tvvs.pacman.gui;


import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.BasicTextImage;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.AWTTerminalFontConfiguration;
import pt.feup.tvvs.pacman.model.Position;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LanternaGUI implements GUI {
    private Screen screen;
    private SCREEN_RESOLUTION resolution;

    public LanternaGUI(int width, int height, SCREEN_RESOLUTION resolution) throws IOException, FontFormatException, URISyntaxException {
        AWTTerminalFontConfiguration fontConfig = loadSquareFont(resolutionToFontSize(resolution));
        Terminal terminal = createTerminal(width, height, fontConfig);
        this.screen = createScreen(terminal);
        this.resolution = resolution;
    }

    public LanternaGUI(Screen screen, SCREEN_RESOLUTION resolution) {
        this.screen = screen;
        this.resolution = resolution;
    }

    private Screen createScreen(Terminal terminal) throws IOException {
        final Screen screen = new TerminalScreen(terminal);

        screen.setCursorPosition(null);
        screen.startScreen();
        screen.doResizeIfNecessary();
        return screen;
    }

    private Terminal createTerminal(int width, int height, AWTTerminalFontConfiguration fontConfig) throws IOException {
        TerminalSize terminalSize = new TerminalSize(width, height + 1);
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory().setInitialTerminalSize(terminalSize);
        terminalFactory.setForceAWTOverSwing(true);
        terminalFactory.setTerminalEmulatorFontConfiguration(fontConfig);
        return terminalFactory.createTerminal();
    }

    private AWTTerminalFontConfiguration loadSquareFont(int fontSize) throws URISyntaxException, FontFormatException, IOException {
        URL resource = getClass().getClassLoader().getResource("Fonts/square.ttf");
        assert resource != null;
        File fontFile = new File(resource.toURI());
        Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(font);

        Font loadedFont = font.deriveFont(Font.PLAIN, fontSize);
        return AWTTerminalFontConfiguration.newInstance(loadedFont);
    }

    @Override
    public void resizeScreen(int width, int height, SCREEN_RESOLUTION newResolution) throws URISyntaxException, IOException, FontFormatException {
        screen.close();
        AWTTerminalFontConfiguration fontConfig = loadSquareFont(resolutionToFontSize(newResolution));
        Terminal terminal = createTerminal(width, height, fontConfig);
        this.screen = createScreen(terminal);
        this.resolution = newResolution;
    }

    private int resolutionToFontSize(SCREEN_RESOLUTION resolution) {
        switch (resolution) {
            case _360p:
                return 2;
            case _540p:
                return 3;
            case _720p:
                return 4;
            case _900p:
                return 5;
            case _1080p:
                return 6;
            case _1440p:
                return 8;
            case _2160p:
                return 12;
        }
        return 2;
    }

    @Override
    public SCREEN_RESOLUTION getResolution() {
        return resolution;
    }

    @Override
    public List<ACTION> getNextAction() throws IOException {
        List<ACTION> actions = new ArrayList<>();
        KeyStroke keyStroke;
        while ((keyStroke = screen.pollInput()) != null) {
            if (keyStroke.getKeyType() == KeyType.EOF || keyStroke.getKeyType() == KeyType.Escape)
                actions.add(ACTION.QUIT);
            else if (keyStroke.getKeyType() == KeyType.Character && keyStroke.getCharacter() == 'q')
                actions.add(ACTION.QUIT);

            else if (keyStroke.getKeyType() == KeyType.ArrowUp) actions.add(ACTION.UP);
            else if (keyStroke.getKeyType() == KeyType.ArrowRight) actions.add(ACTION.RIGHT);
            else if (keyStroke.getKeyType() == KeyType.ArrowDown) actions.add(ACTION.DOWN);
            else if (keyStroke.getKeyType() == KeyType.ArrowLeft) actions.add(ACTION.LEFT);

            else if (keyStroke.getKeyType() == KeyType.Character && keyStroke.getCharacter() == 'w')
                actions.add(ACTION.W);
            else if (keyStroke.getKeyType() == KeyType.Character && keyStroke.getCharacter() == 'a')
                actions.add(ACTION.A);
            else if (keyStroke.getKeyType() == KeyType.Character && keyStroke.getCharacter() == 's')
                actions.add(ACTION.S);
            else if (keyStroke.getKeyType() == KeyType.Character && keyStroke.getCharacter() == 'd')
                actions.add(ACTION.D);


            else if (keyStroke.getKeyType() == KeyType.Enter) actions.add(ACTION.SELECT);
        }

        return actions;
    }

    @Override
    public void drawImage(Position position, BasicTextImage image) {
        screen.newTextGraphics().drawImage(position.toTerminalPosition(), image);
    }

    @Override
    public void drawImage(Position position, BufferedImage image) {
        TextGraphics tg = screen.newTextGraphics();
        int posX = position.getX();
        int posY = position.getY();

        for (int y = 0; y < 11; y++) {
            for (int x = 0; x < 11; x++) {
                if (image.getRGB(x, y) == 0) continue;

                int RGB = image.getRGB(x, y);
                int red = RGB >> 16 & 0xFF;
                int green = RGB >> 8 & 0xFF;
                int blue = RGB & 0xFF;

                tg.setBackgroundColor(new TextColor.RGB(red, green, blue));
                tg.setCharacter(posX + x, posY + y, ' ');
            }
        }
    }

    @Override
    public void drawImage(Position position, BufferedImage image, int width, int height) {
        TextGraphics tg = screen.newTextGraphics();
        int posX = position.getX();
        int posY = position.getY();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (image.getRGB(x, y) == 0) continue;

                int RGB = image.getRGB(x, y);
                int red = RGB >> 16 & 0xFF;
                int green = RGB >> 8 & 0xFF;
                int blue = RGB & 0xFF;

                tg.setBackgroundColor(new TextColor.RGB(red, green, blue));
                tg.setCharacter(posX + x, posY + y, ' ');
            }
        }
    }

    @Override
    public void drawCharacter(Position position, BufferedImage character, TextColor color) {
        TextGraphics tg = screen.newTextGraphics();
        tg.setBackgroundColor(color);
        int posX = position.getX();
        int posY = position.getY();

        for (int y = 0; y < 11; y++) {
            for (int x = 0; x < 5; x++) {
                if (character.getRGB(x, y) == 0) continue;
                tg.setCharacter(posX + x, posY + y, ' ');
            }
        }
    }

    @Override
    public void erase(Position position) {
        TextGraphics tg = screen.newTextGraphics();
        tg.setBackgroundColor(new TextColor.RGB(0, 0, 0));
        tg.fillRectangle(position.toTerminalPosition(), new TerminalSize(11, 11), ' ');
    }

    @Override
    public void clear() {
        screen.clear();
    }

    @Override
    public void refresh() throws IOException {
        screen.refresh();
    }

    @Override
    public void close() throws IOException {
        screen.close();
    }
}
