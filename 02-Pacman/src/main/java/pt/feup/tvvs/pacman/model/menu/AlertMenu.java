package pt.feup.tvvs.pacman.model.menu;

import com.googlecode.lanterna.TextColor;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.menu.element.TextBox;

import java.util.List;

public class AlertMenu extends Menu {
    private final Arena arena;
    private final String alertFilePath;

    public AlertMenu(Arena arena, String alertFilePath) {
        this.arena = arena;
        this.alertFilePath = alertFilePath;
        initializeOptions();
    }

    @Override
    protected List<TextBox> createOptions() {
        return List.of(
                new TextBox("Play Again", new Position(135, 99), new TextColor.RGB(255, 255, 255)),
                new TextBox("Back to main menu", new Position(118, 110), new TextColor.RGB(255, 255, 255))
        );
    }

    @Override
    protected TextBox createTitle() {
        return null;
    }

    public boolean PlayAgainSelected() {
        return getSelectedOption() == 0;
    }

    public boolean ExitSelected() {
        return getSelectedOption() == 1;
    }

    public Arena getArena() {
        return arena;
    }

    public String getAlertFilePath() {
        return this.alertFilePath;
    }
}