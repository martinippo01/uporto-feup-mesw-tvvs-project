package pt.feup.tvvs.pacman.controller.menu;

import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.element.Direction;
import pt.feup.tvvs.pacman.model.game.element.ghost.Blinky;
import pt.feup.tvvs.pacman.model.game.element.ghost.Clyde;
import pt.feup.tvvs.pacman.model.game.element.ghost.Inky;
import pt.feup.tvvs.pacman.model.game.element.ghost.Pinky;
import pt.feup.tvvs.pacman.model.menu.MainMenu;
import pt.feup.tvvs.pacman.model.menu.MapSelectionMenu;
import pt.feup.tvvs.pacman.states.menu.MapSelectionMenuState;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class MainMenuController extends MenuController<MainMenu> {
    public MainMenuController(MainMenu model, AudioManager audioManager) {
        super(model, audioManager);
    }

    @Override
    public void step(Game game, List<GUI.ACTION> actions, long time) throws URISyntaxException, IOException, FontFormatException {
        super.step(game, actions, time);
        for (GUI.ACTION action : actions) {
            if (action == GUI.ACTION.SELECT) {
                menuConfirmSelection.playOnce();
                if (getModel().singlePLayerSelected()) {
                    MapSelectionMenu mapSelectionMenu = new MapSelectionMenu("singleplayer"); // Create a new map selection menu model
                    game.setState(new MapSelectionMenuState(mapSelectionMenu, game.getAudioManager())); // Switch to map selection menu
                } else if (getModel().multiplayerSelected()) {
                    MapSelectionMenu mapSelectionMenu = new MapSelectionMenu("multiplayer"); // Create a new map selection menu model
                    game.setState(new MapSelectionMenuState(mapSelectionMenu, game.getAudioManager())); // Switch to map selection menu
                } else if (getModel().ExitSelected()) { //exit game
                    game.setState(null);
                } else if (getModel().ResolutionSelected()) { //change the resolution
                    GUI.SCREEN_RESOLUTION newResolution = incrementResolution(game.getResolution());
                    game.setResolution(newResolution);
                    getModel().setResolution(newResolution);
                } else if (getModel().MasterVolumeSelected()) { //change master volume
                    float newVolume = super.handleVolumeChange(game);
                    getModel().setMasterVolume(newVolume);
                }
            }
        }

        Blinky blinky = getModel().getBlinky();
        blinky.incrementCounter();
        if (blinky.getPosition().equals(new Position(3, 4))) blinky.setDirection(Direction.RIGHT);
        else if (blinky.getPosition().equals(new Position(7, 4))) blinky.setDirection(Direction.LEFT);

        Inky inky = getModel().getInky();
        inky.incrementCounter();
        if (inky.getPosition().equals(new Position(5, 8))) inky.setDirection(Direction.DOWN);
        else if (inky.getPosition().equals(new Position(5, 13))) inky.setDirection(Direction.UP);

        Pinky pinky = getModel().getPinky();
        pinky.incrementCounter();
        if (pinky.getPosition().equals(new Position(19, 13))) pinky.setDirection(Direction.RIGHT);
        else if (pinky.getPosition().equals(new Position(26, 13))) pinky.setDirection(Direction.LEFT);

        Clyde clyde = getModel().getClyde();
        clyde.incrementCounter();
        if (clyde.getPosition().equals(new Position(24, 3))) clyde.setDirection(Direction.DOWN);
        else if (clyde.getPosition().equals(new Position(24, 10))) clyde.setDirection(Direction.UP);
    }
}