package pt.feup.tvvs.pacman;

import pt.feup.tvvs.pacman.audio.AudioManager;
import pt.feup.tvvs.pacman.audio.AudioPlayer;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.gui.LanternaGUI;
import pt.feup.tvvs.pacman.model.menu.MainMenu;
import pt.feup.tvvs.pacman.states.State;
import pt.feup.tvvs.pacman.states.menu.MainMenuState;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

public class Game {
    private static final int SCREEN_WIDTH = 320;
    private static final int SCREEN_HEIGHT = 180;
    private static Game instance;
    private final GUI gui;
    private final AudioManager audioManager;
    private State state;


    private Game(GUI gui, AudioManager audioManager) throws IOException, URISyntaxException {
        this.gui = gui;
        this.audioManager = audioManager;
        this.audioManager.setMasterVolume(1f);
        this.state = createInitialState();
    }

    public static Game getInstance(GUI gui, AudioManager audioManager) throws IOException, URISyntaxException, FontFormatException {
        if (instance == null) {
            instance = new Game(gui, audioManager);
        }
        return instance;
    }

    public static void main(String[] args) throws IOException, URISyntaxException, FontFormatException, InterruptedException {
        GUI gui = new LanternaGUI(SCREEN_WIDTH, SCREEN_HEIGHT, GUI.SCREEN_RESOLUTION._900p);
        AudioManager audioManager = AudioManager.getInstance();
        Game game = Game.getInstance(gui, audioManager);
        game.start();
    }

    // User only for tests
    public static void main(GUI gui, AudioManager audioManager) throws IOException, URISyntaxException, FontFormatException, InterruptedException {
        Game game = Game.getInstance(gui, audioManager);
        game.start();
    }

    private State createInitialState() throws IOException, URISyntaxException {
        return new MainMenuState(
                new MainMenu(gui.getResolution(), audioManager.getMasterVolume()),
                audioManager
        );
    }

    public GUI getGui() {
        return gui;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public GUI.SCREEN_RESOLUTION getResolution() {
        return gui.getResolution();
    }

    public void setResolution(GUI.SCREEN_RESOLUTION newResolution) throws URISyntaxException, IOException, FontFormatException {
        gui.resizeScreen(SCREEN_WIDTH, SCREEN_HEIGHT, newResolution);
    }

    private void start() throws IOException, InterruptedException {
        GameLoop gameLoop = new GameLoop(60);
        AudioPlayer mainMusic = initializeMusic();

        try {
            while (this.state != null) {
                gameLoop.update(() -> {
                    try {
                        state.step(this, gui, gameLoop.getFrameCount());
                    } catch (IOException | URISyntaxException | FontFormatException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } finally {
            cleanup(mainMusic);
        }
    }

    private AudioPlayer initializeMusic() {
        AudioPlayer mainMusic = new AudioPlayer("Audio/music.wav");
        mainMusic.setVolume(0.05f);
        mainMusic.playInLoop();
        audioManager.setMainMusic(mainMusic);
        return mainMusic;
    }

    private void cleanup(AudioPlayer mainMusic) throws IOException {
        mainMusic.stopPlaying();
        gui.close();
    }
}

class GameLoop {
    private final long frameTime;
    private long frameCount;

    public GameLoop(int fps) {
        this.frameTime = 1000 / fps;
        this.frameCount = 0;
    }

    public void update(Runnable updateLogic) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        updateLogic.run();

        long elapsedTime = System.currentTimeMillis() - startTime;
        long sleepTime = frameTime - elapsedTime;

        if (sleepTime > 0) Thread.sleep(sleepTime);
        ++frameCount;
    }

    public long getFrameCount() {
        return frameCount;
    }
}
