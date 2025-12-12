package pt.feup.tvvs.pacman.controller.game;

import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.controller.game.element.CollisionController;
import pt.feup.tvvs.pacman.controller.game.element.GhostController;
import pt.feup.tvvs.pacman.controller.game.element.PacmanController;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.menu.AlertMenu;
import pt.feup.tvvs.pacman.model.menu.PauseMenu;
import pt.feup.tvvs.pacman.states.menu.AlertMenuState;
import pt.feup.tvvs.pacman.states.menu.PauseMenuState;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class ArenaController extends GameController {
    private final PacmanController pacmanController;
    private final CollisionController collisionController;
    private final GhostController ghostController;

    public ArenaController(Arena arena, AudioManager audioManager) {
        super(arena);

        this.pacmanController = new PacmanController(arena);
        this.collisionController = new CollisionController(arena, audioManager);
        this.ghostController = new GhostController(arena);
    }

    @Override
    public void step(Game game, List<GUI.ACTION> actions, long time) throws IOException, URISyntaxException {
        for (GUI.ACTION action : actions) {
            if (action == GUI.ACTION.QUIT) {
                game.getAudioManager().stopAllAudios();
                game.setState(new PauseMenuState(new PauseMenu(game.getState(), game.getResolution(), game.getAudioManager().getMasterVolume()), game.getAudioManager()));
                return;
            }
        }
        if (getModel().getCollectibles().isEmpty()) {
            game.getAudioManager().stopAllAudios();
            game.setState(new AlertMenuState(new AlertMenu(getModel(), "PNGs/youwin.png"), game.getAudioManager()));
            return;
        }
        //all the controllers here me thinks
        pacmanController.step(game, actions, time);
        ghostController.step(game, actions, time);
        collisionController.step(game, actions, time);
    }
}
