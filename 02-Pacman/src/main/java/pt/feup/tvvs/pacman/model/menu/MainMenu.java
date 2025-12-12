package pt.feup.tvvs.pacman.model.menu;

import com.googlecode.lanterna.TextColor;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.element.Direction;
import pt.feup.tvvs.pacman.model.game.element.ghost.Blinky;
import pt.feup.tvvs.pacman.model.game.element.ghost.Clyde;
import pt.feup.tvvs.pacman.model.game.element.ghost.Inky;
import pt.feup.tvvs.pacman.model.game.element.ghost.Pinky;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;
import pt.feup.tvvs.pacman.model.menu.element.TextBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainMenu extends Menu implements MenuOptions {
    private final Set<Position> blankPositions;
    private final Pacman pacman;

    private final Blinky blinky;
    private final Inky inky;
    private final Pinky pinky;
    private final Clyde clyde;

    public MainMenu(GUI.SCREEN_RESOLUTION resolution, float volume) {
        initializeOptions();
        blankPositions = createBlankPosition();
        pacman = new Pacman(new Position(14, 4));

        blinky = new Blinky(new Position(7, 4));
        inky = new Inky(new Position(5, 8));
        inky.setDirection(Direction.DOWN);
        pinky = new Pinky(new Position(21, 13));
        pinky.setDirection(Direction.RIGHT);
        clyde = new Clyde(new Position(24, 10));
        clyde.setDirection(Direction.UP);

        setResolution(resolution);
        setMasterVolume(volume);
    }

    @Override
    public List<TextBox> createOptions() {
        return new ArrayList<>(List.of(
                new TextBox("Single player", new Position(130, 80), new TextColor.RGB(255, 255, 255)),
                new TextBox("Multi player", new Position(132, 91), new TextColor.RGB(255, 255, 255)),
                new TextBox("Resolution: 900p", new Position(118, 102), new TextColor.RGB(255, 255, 255)),
                new TextBox("Master Volume: 10", new Position(118, 113), new TextColor.RGB(255, 255, 255)),
                new TextBox("Exit", new Position(150, 124), new TextColor.RGB(255, 255, 255))
        ));
    }

    @Override
    protected TextBox createTitle() {
        return new TextBox("PACMAN", new Position(145, 30), new TextColor.RGB(255, 255, 255));
    }

    private Set<Position> createBlankPosition() {
        HashSet<Position> blankPos = new HashSet<>();
        for (int i = 3; i <= 7; ++i) blankPos.add(new Position(i, 4)); //Blinky's movement
        for (int i = 8; i <= 13; ++i) blankPos.add(new Position(5, i)); //Inky's movement
        for (int i = 19; i <= 26; ++i) blankPos.add(new Position(i, 13)); //Pinky's movement
        for (int i = 3; i <= 10; ++i) blankPos.add(new Position(24, i)); //Clyde's movement
        return blankPos;
    }

    public boolean singlePLayerSelected() {
        return getSelectedOption() == 0;
    }

    public boolean multiplayerSelected() {
        return getSelectedOption() == 1;
    }

    @Override
    public boolean ResolutionSelected() {
        return getSelectedOption() == 2;
    }

    @Override
    public boolean MasterVolumeSelected() {
        return getSelectedOption() == 3;
    }

    public boolean ExitSelected() {
        return getSelectedOption() == 4;
    }


    public Pacman getPacman() {
        return pacman;
    }

    @Override
    public void setResolution(GUI.SCREEN_RESOLUTION newResolution) {
        getOptions().get(2).setText("Resolution: " + newResolution);
    }

    @Override
    public void setMasterVolume(float volume) {
        getOptions().get(3).setText("Master Volume: " + (int) (volume * 10));
    }

    public Blinky getBlinky() {
        return blinky;
    }

    public Clyde getClyde() {
        return clyde;
    }

    public Pinky getPinky() {
        return pinky;
    }

    public Inky getInky() {
        return inky;
    }

    public Set<Position> getBlankPositions() {
        return blankPositions;
    }
}
