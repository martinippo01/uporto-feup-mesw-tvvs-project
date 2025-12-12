package pt.feup.tvvs.pacman.viewer.game;


import com.googlecode.lanterna.TextColor;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Position;
import pt.feup.tvvs.pacman.model.game.Arena;
import pt.feup.tvvs.pacman.model.menu.element.TextBox;
import pt.feup.tvvs.pacman.viewer.ModelViewer;
import pt.feup.tvvs.pacman.viewer.ViewerFactory;

import java.io.IOException;

public class ArenaViewer extends ModelViewer<Arena> {
    public ArenaViewer() throws IOException {
        super(ViewerFactory.createArenaViewers());
    }

    @Override
    public void drawElements(GUI gui, Arena arena, long frameCount) {
        //before drawing all the elements erase the blank positions
        arena.getBlankPositions().forEach(position -> gui.erase(new Position(position.getX() * 11, position.getY() * 11)));

        arena.getWalls().forEach(wall -> drawElement(gui, wall, frameCount));
        drawElement(gui, arena.getGhostGate(), frameCount);
        arena.getCollectibles().forEach(collectible -> drawElement(gui, collectible, frameCount));
        arena.getGhosts().forEach(ghost -> drawElement(gui, ghost, frameCount));
        arena.getPacmans().forEach(pacman -> drawElement(gui, pacman, frameCount));
        drawElement(gui, new TextBox("Score:" + arena.getScore(), new Position(11, 0), new TextColor.RGB(255, 255, 255)), frameCount);
        if (arena.getPacmans().size() == 2) {
            drawElement(gui, new TextBox("Lives P1:" + arena.getPacmans().get(0).getLife(), new Position(199, 0), new TextColor.RGB(255, 255, 255)), frameCount);
            drawElement(gui, new TextBox("Lives P2:" + arena.getPacmans().get(1).getLife(), new Position(259, 0), new TextColor.RGB(255, 255, 255)), frameCount);
        } else
            drawElement(gui, new TextBox("Lives:" + arena.getPacmans().get(0).getLife(), new Position(274, 0), new TextColor.RGB(255, 255, 255)), frameCount);
    }

}
