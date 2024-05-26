package my.snole.pacmannn.model.ghost;

import javafx.geometry.Point2D;
import my.snole.pacmannn.model.pacman.BotPacMan;
import my.snole.pacmannn.core.PacManModel;

import java.util.Iterator;
import java.util.List;

/**
 * Класс для управления привидениями в игре Pac-Man.
 */
public class GhostManager {
    private List<Ghost> ghosts; // список привидений

    /**
     * Конструктор менеджера привидений.
     * @param ghosts список привидений
     */
    public GhostManager(List<Ghost> ghosts) {
        this.ghosts = ghosts;
    }

    /**
     * Двигает привидений в сторону Pac-Man или ботов.
     * @param pacmanLocation местоположение Pac-Man
     * @param bots список ботов Pac-Man
     * @param ghostEatingMode режим поедания привидений
     * @param grid игровая сетка
     */
    public void moveGhosts(Point2D pacmanLocation, List<BotPacMan> bots, boolean ghostEatingMode, PacManModel.CellValue[][] grid) {
        System.out.println("Moving ghosts...");
        for (Ghost ghost : ghosts) {
            ghost.moveTowardsPacmanOrBots(pacmanLocation, bots, ghostEatingMode, grid); // двигаем привидение
            System.out.println("Moved ghost to: " + ghost.getLocation());
            for (Iterator<BotPacMan> iterator = bots.iterator(); iterator.hasNext(); ) {
                BotPacMan bot = iterator.next();
                if (ghost.getLocation().equals(bot.getLocation())) { // проверка на столкновение с ботом
                    iterator.remove(); // удаляем бота, если он столкнулся с привидением
                    break;
                }
            }
        }
    }

    /**
     * Добавляет привидение в список.
     * @param ghost привидение
     */
    public void addGhost(Ghost ghost) {
        ghosts.add(ghost);
    }

    /**
     * Возвращает список привидений.
     * @return список привидений
     */
    public List<Ghost> getGhosts() {
        return ghosts;
    }
}
