package pt.feup.tvvs.pacman.model.menu;

import com.googlecode.lanterna.TextColor;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.menu.element.TextBox;
import pt.feup.tvvs.pacman.states.State;

import java.util.ArrayList;
import java.util.List;

public class PauseMenu extends Menu implements MenuOptions {
    private final TextBox pauseSign;
    private final State<?> pausedState;

    public PauseMenu(State<?> pausedState, GUI.SCREEN_RESOLUTION resolution, float volume) {
        this.pausedState = pausedState;
        this.pauseSign = new TextBox("||", new Position(155, 41), new TextColor.RGB(255, 255, 255));
        initializeOptions();
        setResolution(resolution);
        setMasterVolume(volume);
    }

    @Override
    public List<TextBox> createOptions() {
        return new ArrayList<>(List.of(
                new TextBox("Resume", new Position(145, 80), new TextColor.RGB(255, 255, 255)),
                new TextBox("Resolution: 900p", new Position(118, 91), new TextColor.RGB(255, 255, 255)),
                new TextBox("Master Volume: 10", new Position(118, 102), new TextColor.RGB(255, 255, 255)),
                new TextBox("Exit to main menu", new Position(118, 113), new TextColor.RGB(255, 255, 255))
        ));
    }

    @Override
    protected TextBox createTitle() {
        return new TextBox("Paused", new Position(145, 30), new TextColor.RGB(255, 255, 255));
    }

    public State<?> getPausedState() {
        return pausedState;
    }

    public TextBox getPauseSign() {
        return pauseSign;
    }

    public boolean ResumeSelected() {
        return getSelectedOption() == 0;
    }

    @Override
    public boolean ResolutionSelected() {
        return getSelectedOption() == 1;
    }

    @Override
    public boolean MasterVolumeSelected() {
        return getSelectedOption() == 2;
    }

    public boolean ExitSelected() {
        return getSelectedOption() == 3;
    }

    @Override
    public void setResolution(GUI.SCREEN_RESOLUTION newResolution) {
        getOptions().get(1).setText("Resolution: " + newResolution);
    }

    @Override
    public void setMasterVolume(float volume) {
        getOptions().get(2).setText("Master Volume: " + (int) (volume * 10));
    }
}
