package pt.feup.tvvs.pacman.gui;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.BasicTextImage;
import pt.feup.tvvs.pacman.model.Position;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface GUI {
    List<ACTION> getNextAction() throws IOException;

    void drawImage(Position position, BasicTextImage image);

    void drawImage(Position position, BufferedImage image);

    void drawImage(Position position, BufferedImage image, int width, int height);

    void drawCharacter(Position position, BufferedImage character, TextColor color);

    void clear();

    void erase(Position position);

    void refresh() throws IOException;

    void close() throws IOException;

    void resizeScreen(int width, int height, SCREEN_RESOLUTION newResolution) throws URISyntaxException, IOException, FontFormatException;

    SCREEN_RESOLUTION getResolution();

    enum ACTION {UP, RIGHT, DOWN, LEFT, NONE, QUIT, SELECT, W, A, S, D}

    enum SCREEN_RESOLUTION {
        _360p,
        _540p,
        _720p,
        _900p,
        _1080p,
        _1440p,
        _2160p;

        @Override
        public String toString() {
            switch (this) {
                case _360p:
                    return "360p";
                case _540p:
                    return "540p";
                case _720p:
                    return "720p";
                case _900p:
                    return "900p";
                case _1080p:
                    return "1080p";
                case _1440p:
                    return "1440p";
                case _2160p:
                    return "2160p";
            }
            return null;
        }
    }
}
