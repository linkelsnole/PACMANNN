package my.snole.pacmannn.model.ghost;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import my.snole.pacmannn.model.pacman.BotPacMan;
import my.snole.pacmannn.core.PacManModel;

import java.util.Iterator;
import java.util.List;

public class GhostManager {
    private List<Ghost> ghosts;

    public GhostManager(List<Ghost> ghosts) {
        this.ghosts = ghosts;
    }

    public void moveGhosts(Point2D pacmanLocation, List<BotPacMan> bots, boolean ghostEatingMode, PacManModel.CellValue[][] grid) {
        System.out.println("Moving ghosts...");
        for (Ghost ghost : ghosts) {
            ghost.moveTowardsPacmanOrBots(pacmanLocation, bots, ghostEatingMode, grid);
            System.out.println("Moved ghost to: " + ghost.getLocation());
            for (Iterator<BotPacMan> iterator = bots.iterator(); iterator.hasNext(); ) {
                BotPacMan bot = iterator.next();
                if (ghost.getLocation().equals(bot.getLocation())) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    public void addGhost(Ghost ghost) {
        ghosts.add(ghost);
    }

    public List<Ghost> getGhosts() {
        return ghosts;
    }
}
