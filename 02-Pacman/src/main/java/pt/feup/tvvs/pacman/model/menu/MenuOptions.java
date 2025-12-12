package pt.feup.tvvs.pacman.model.menu;

import pt.feup.tvvs.pacman.gui.GUI;

public interface MenuOptions {
    boolean ResolutionSelected();

    boolean MasterVolumeSelected();

    void setMasterVolume(float volume);

    void setResolution(GUI.SCREEN_RESOLUTION newResolution);
}
