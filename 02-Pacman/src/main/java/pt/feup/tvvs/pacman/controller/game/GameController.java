package pt.feup.tvvs.pacman.controller.game;

import pt.feup.tvvs.pacman.controller.Controller;
import pt.feup.tvvs.pacman.model.game.Arena;

public abstract class GameController extends Controller<Arena> {
    public GameController(Arena arena) {
        super(arena);
    }
}
