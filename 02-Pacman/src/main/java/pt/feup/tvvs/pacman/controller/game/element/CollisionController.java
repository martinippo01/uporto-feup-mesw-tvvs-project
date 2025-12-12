package pt.feup.tvvs.pacman.controller.game.element;

import pt.feup.tvvs.pacman.Game;
import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.audio.AudioPlayer;
import pt.feup.tvvs.pacman.controller.game.GameController;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.game.element.collectibles.PowerUp;
import pt.feup.tvvs.pacman.model.game.element.ghost.Ghost;
import pt.feup.tvvs.pacman.model.game.element.ghost.GhostState;
import pt.feup.tvvs.pacman.model.game.element.pacman.Pacman;
import pt.feup.tvvs.pacman.states.game.DyingState;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class CollisionController extends GameController {
    private final AudioPlayer ghostEatenAudio;
    private final AudioPlayer collectibleEatenAudio;
    private final AudioPlayer ghostsAliveSiren;
    private final AudioPlayer ghostsScaredSiren;
    private int deadPacmanTimeCounter; //counter for one dead pacman on multiplayer
    private int ghostsEaten; //ghosts eaten in current scared state
    private int scaredTimeLeft;

    public CollisionController(Arena arena, AudioManager audioManager) {
        super(arena);

        audioManager.addAudio("ghostEaten", "Audio/ghostEaten.wav");
        this.ghostEatenAudio = audioManager.getAudio("ghostEaten");
        this.ghostEatenAudio.setVolume(0.40f);

        audioManager.addAudio("collectibleEaten", "Audio/collectibleEaten.wav");
        this.collectibleEatenAudio = audioManager.getAudio("collectibleEaten");
        collectibleEatenAudio.setVolume(0.25f);

        audioManager.addAudio("ghostsAliveSiren", "Audio/ghostsAlive.wav");
        this.ghostsAliveSiren = audioManager.getAudio("ghostsAliveSiren");
        this.ghostsAliveSiren.setVolume(0.2f);

        audioManager.addAudio("ghostsScaredSiren", "Audio/ghostsScared.wav");
        this.ghostsScaredSiren = audioManager.getAudio("ghostsScaredSiren");
        this.ghostsScaredSiren.setVolume(0.25f);

        this.deadPacmanTimeCounter = 0;
        this.ghostsEaten = 0;
        this.scaredTimeLeft = 0;
    }

    private void checkPacmanGhostCollision(Game game) throws IOException, URISyntaxException {
        for (Pacman pacman : getModel().getPacmans()) {
            if (pacman.isDying()) continue; //don't process collisions with dead pacmans
            outer:
            for (Ghost ghost : getModel().getGhosts()) {
                if (ghost.collidingWith(pacman)) {
                    switch (ghost.getState()) {
                        case ALIVE:
                            pacman.decreaseLife();
                            pacman.setDying(true);
                            pacman.setSpeed(Arena.PACMAN_NORMAL_SPEED);
                            long alivePacmans = getModel().getPacmans().stream().filter(pacman1 -> !pacman1.isDying()).count();
                            //first condition is for multiplayer, second is for single player
                            if (alivePacmans == 0) {
                                game.getAudioManager().stopAllAudios();
                                game.setState(new DyingState(getModel(), game.getAudioManager()));
                            }
                            //if no pacman is dead before set counter to freeze the dead pacman (multiplayer only)
                            else deadPacmanTimeCounter = 110;
                            break outer;
                        case SCARED:
                            ghostEatenAudio.playOnce();
                            ghost.setState(GhostState.DEAD);
                            ghost.setSpeed(Arena.GHOST_DEAD_SPEED);
                            //each ghost is worth double the one eaten before in the current scared state, starting from 200
                            getModel().incrementScore((long) (200 * Math.pow(2, ghostsEaten++)));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    private void checkPacmanCollectibleCollision() {
        for (Pacman pacman : getModel().getPacmans()) {
            if (pacman.isDying()) continue;
            getModel().getCollectibles().removeIf(collectible -> { //safe remove while iterating
                if (pacman.getPosition().equals(collectible.getPosition())) {
                    if (collectible.getClass() == PowerUp.class) {
                        scaredTimeLeft = 1500;
                        ghostsEaten = 0;
                        ghostsAliveSiren.stopPlaying();
                        if (!ghostsScaredSiren.isPlaying()) ghostsScaredSiren.playInLoop();

                        getModel().getGhosts().forEach(ghost -> {
                            if (!ghost.isDead()) {
                                ghost.setState(GhostState.SCARED);
                                ghost.setSpeed(Arena.GHOST_SCARED_SPEED);
                                if (!ghost.getPosition().equals(getModel().getGhostGate().getPosition()))
                                    ghost.invertDirection();
                            }
                        });
                        for (Pacman p : getModel().getPacmans()) p.setSpeed(Arena.PACMAN_BOOSTED_SPEED);
                    }
                    collectibleEatenAudio.playOnce();
                    getModel().addBlankPosition(new Position(collectible.getPosition())); //new position to be cleared
                    getModel().incrementScore(collectible.getValue());
                    getModel().incrementCollectedCollectibles();
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    public void step(Game game, List<GUI.ACTION> actions, long time) throws IOException, URISyntaxException {
        if (!ghostsAliveSiren.isPlaying() && !ghostsScaredSiren.isPlaying()) {
            if (scaredTimeLeft == 0) ghostsAliveSiren.playInLoop(); //start of the game or leaving pause menu
            else ghostsScaredSiren.playInLoop(); //leaving pause menu and scared state was on before
        }

        //when playing multiplayer and one pacman is still alive, the other comes to life
        if (deadPacmanTimeCounter > 0 && --deadPacmanTimeCounter == 0) {
            for (Pacman pacman : getModel().getPacmans())
                if (pacman.isDying() && pacman.getLife() > 0) {
                    pacman.setDying(false);
                    pacman.setPosition(pacman.getRespawnPosition());
                    pacman.setCounter(0);
                }
        }

        //if scared time ends or if all ghosts are eaten scared time ends
        if ((scaredTimeLeft > 0 && --scaredTimeLeft == 0) || ghostsEaten == getModel().getGhosts().size()) {
            getModel().getGhosts().forEach(ghost -> {
                if (ghost.isScared()) {
                    ghost.setState(GhostState.ALIVE);
                    ghost.setSpeed(Arena.GHOST_NORMAL_SPEED);
                }
            });
            ghostsScaredSiren.stopPlaying();
            ghostsAliveSiren.playInLoop();
            for (Pacman pacman : getModel().getPacmans()) pacman.setSpeed(Arena.PACMAN_NORMAL_SPEED);
            ghostsEaten = 0;
        }

        checkPacmanGhostCollision(game); //check and process collisions between ghosts and pacmans
        checkPacmanCollectibleCollision(); //check and process collisions between collectibles and pacmans

    }
}
