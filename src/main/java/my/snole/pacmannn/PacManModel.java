package my.snole.pacmannn;

import javafx.geometry.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class PacManModel {
    public enum CellValue {
        EMPTY, SMALLDOT, BIGDOT, WALL, GHOST1HOME, GHOST2HOME, PACMANHOME
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT, NONE
    }

    private int rowCount;
    private int columnCount;
    private CellValue[][] grid;
    private int score;
    private int level;
    private int dotCount;
    private static boolean gameOver;
    private static boolean youWon;
    private static boolean ghostEatingMode;
    private PacMan pacman;
    private List<Ghost> ghosts;
    private static Direction lastDirection;
    private static Direction currentDirection;
    private List<BotPacMan> botPacMen;
    private static final int GHOST_EATING_MODE_DURATION = 25;
    private static int ghostEatingModeCounter;

    public PacManModel() {
        this.startNewGame();
    }

    public void initializeLevel(String fileName) {
        // Прежняя логика инициализации уровня
        File file = new File(fileName);
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Scanner lineScanner = new Scanner(line);
            while (lineScanner.hasNext()) {
                lineScanner.next();
                columnCount++;
            }
            rowCount++;
        }
        columnCount = columnCount / rowCount;
        Scanner scanner2 = null;
        try {
            scanner2 = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        grid = new CellValue[rowCount][columnCount];
        int row = 0;
        int pacmanRow = 0;
        int pacmanColumn = 0;
        int ghost1Row = 0;
        int ghost1Column = 0;
        int ghost2Row = 0;
        int ghost2Column = 0;

        int botPacmanRow = 0;
        int botPacmanColumn = 0;

        while (scanner2.hasNextLine()) {
            int column = 0;
            String line = scanner2.nextLine();
            Scanner lineScanner = new Scanner(line);
            while (lineScanner.hasNext()) {
                String value = lineScanner.next();
                CellValue thisValue;
                if (value.equals("W")) {
                    thisValue = CellValue.WALL;
                } else if (value.equals("S")) {
                    thisValue = CellValue.SMALLDOT;
                    dotCount++;
                } else if (value.equals("B")) {
                    thisValue = CellValue.BIGDOT;
                    dotCount++;
                } else if (value.equals("1")) {
                    thisValue = CellValue.GHOST1HOME;
                    ghost1Row = row;
                    ghost1Column = column;
                } else if (value.equals("2")) {
                    thisValue = CellValue.GHOST2HOME;
                    ghost2Row = row;
                    ghost2Column = column;
                } else if (value.equals("P")) {
                    thisValue = CellValue.PACMANHOME;
                    pacmanRow = row;
                    pacmanColumn = column;
                } else if (value.equals("Q")) {
                    thisValue = CellValue.PACMANHOME;
                    botPacmanRow = row;
                    botPacmanColumn = column;
                } else {
                    thisValue = CellValue.EMPTY;
                }
                grid[row][column] = thisValue;
                column++;
            }
            row++;
        }

        Point2D pacmanLocation = new Point2D(pacmanRow, pacmanColumn);
        Point2D ghost1Location = new Point2D(ghost1Row, ghost1Column);
        Point2D ghost2Location = new Point2D(ghost2Row, ghost2Column);
        Point2D botPacmanLocation = new Point2D(botPacmanRow, botPacmanColumn);

        System.out.println("PacMan initialized at: " + pacmanLocation);

        this.pacman = new PacMan(pacmanLocation, new Point2D(0, 0));
        this.ghosts = new ArrayList<>();
        this.ghosts.add(new Ghost(ghost1Location, new Point2D(-1, 0)));
        this.ghosts.add(new Ghost(ghost2Location, new Point2D(-1, 0)));
        this.botPacMen = new ArrayList<>();
        this.botPacMen.add(new BotPacMan(botPacmanLocation, new Point2D(0, 0)));

        currentDirection = Direction.NONE;
        lastDirection = Direction.NONE;
    }

    public void startNewGame() {
        gameOver = false;
        youWon = false;
        ghostEatingMode = false;
        dotCount = 0;
        rowCount = 0;
        columnCount = 0;
        score = 0;
        level = 1;
        this.initializeLevel(Controller.getLevelFile(0));
    }

    public void startNextLevel() {
        if (this.isLevelComplete()) {
            this.level++;
            rowCount = 0;
            columnCount = 0;
            youWon = false;
            ghostEatingMode = false;
            try {
                this.initializeLevel(Controller.getLevelFile(level - 1));
            }
            catch (ArrayIndexOutOfBoundsException e) {
                //if there are no levels left in the level array, the game ends
                youWon = true;
                gameOver = true;
                level--;
            }
        }
    }

    public void step(Direction direction) {
        if (direction == null) {
            direction = Direction.NONE;
        }
        this.pacman.move(direction, grid);
        for (BotPacMan botPacMan : botPacMen) {
            botPacMan.move(grid);
        }
        this.moveGhosts();

        // Прежняя логика обработки шагов игры
        // Если Пакман находит еду
        CellValue pacmanLocationCellValue = grid[(int) pacman.getLocation().getX()][(int) pacman.getLocation().getY()];
        if (pacmanLocationCellValue == CellValue.SMALLDOT) {
            grid[(int) pacman.getLocation().getX()][(int) pacman.getLocation().getY()] = CellValue.EMPTY;
            dotCount--;
            score += 10;
        }
        if (pacmanLocationCellValue == CellValue.BIGDOT) {
            grid[(int) pacman.getLocation().getX()][(int) pacman.getLocation().getY()] = CellValue.EMPTY;
            dotCount--;
            score += 50;
            ghostEatingMode = true;
            setGhostEatingModeCounter();
        }

        // Если бот Пакман находит еду
        for (BotPacMan botPacMan : botPacMen) {
            CellValue botPacmanLocationCellValue = grid[(int) botPacMan.getLocation().getX()][(int) botPacMan.getLocation().getY()];
            if (botPacmanLocationCellValue == CellValue.SMALLDOT) {
                grid[(int) botPacMan.getLocation().getX()][(int) botPacMan.getLocation().getY()] = CellValue.EMPTY;
                dotCount--;
                score += 10;
            }
            if (botPacmanLocationCellValue == CellValue.BIGDOT) {
                grid[(int) botPacMan.getLocation().getX()][(int) botPacMan.getLocation().getY()] = CellValue.EMPTY;
                dotCount--;
                score += 50;
                ghostEatingMode = true;
                setGhostEatingModeCounter();
            }
        }

        if (ghostEatingMode) {
            if (pacman.getLocation().equals(ghosts.get(0).getLocation())) {
                sendGhostHome(ghosts.get(0));
                score += 100;
            }
            if (pacman.getLocation().equals(ghosts.get(1).getLocation())) {
                sendGhostHome(ghosts.get(1));
                score += 100;
            }
        } else {
            if (pacman.getLocation().equals(ghosts.get(0).getLocation()) || pacman.getLocation().equals(ghosts.get(1).getLocation())) {
                gameOver = true;
                pacman.setVelocity(new Point2D(0, 0));
            }
        }

        // Если уровень завершен
        if (this.isLevelComplete()) {
            pacman.setVelocity(new Point2D(0, 0));
            startNextLevel();
        }
    }

    public void moveGhosts() {
        for (Ghost ghost : ghosts) {
            ghost.moveTowardsPacman(pacman.getLocation(), ghostEatingMode, grid);
        }
    }

    public void sendGhostHome(Ghost ghost) {
        for (int row = 0; row < this.rowCount; row++) {
            for (int column = 0; column < this.columnCount; column++) {
                if (grid[row][column] == (ghost == ghosts.get(0) ? CellValue.GHOST1HOME : CellValue.GHOST2HOME)) {
                    ghost.setLocation(new Point2D(row, column));
                    ghost.setVelocity(new Point2D(-1, 0));
                }
            }
        }
    }

    public boolean isLevelComplete() {
        return this.dotCount == 0;
    }

    public static boolean isGhostEatingMode() {
        return ghostEatingMode;
    }

    public static boolean isGameOver() {
        return gameOver;
    }

    public static boolean isYouWon() {
        return youWon;
    }

    public int getScore() {
        return score;
    }

    public int getLevel() {
        return level;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public CellValue[][] getGrid() {
        return grid;
    }

    public CellValue getCellValue(int row, int column) {
        assert row >= 0 && row < this.grid.length && column >= 0 && column < this.grid[0].length;
        return this.grid[row][column];
    }

    public static Direction getCurrentDirection() {
        return currentDirection;
    }

    public void setCurrentDirection(Direction direction) {
        currentDirection = direction;
    }

    public static Direction getLastDirection() {
        return lastDirection;
    }

    public static void setLastDirection(Direction direction) {
        lastDirection = direction;
    }

    public static void setGhostEatingModeCounter() {
        ghostEatingModeCounter = GHOST_EATING_MODE_DURATION;
    }

    public static void decrementGhostEatingModeCounter() {
        if (ghostEatingModeCounter > 0) {
            ghostEatingModeCounter--;
            if (ghostEatingModeCounter == 0) {
                ghostEatingMode = false;
            }
        }
    }

    public static int getGhostEatingModeCounter() {
        return ghostEatingModeCounter;
    }

    public static void setGhostEatingMode(boolean ghostEatingModeBool) {
        ghostEatingMode = ghostEatingModeBool;
    }
    public Point2D getPacmanLocation() {
        return pacman.getLocation();
    }

    public Point2D getGhost1Location() {
        return ghosts.get(0).getLocation();
    }

    public Point2D getGhost2Location() {
        return ghosts.get(1).getLocation();
    }

    public Point2D getBotPacmanLocation() {
        return botPacMen.get(0).getLocation(); // Если у вас есть несколько ботов, нужно будет изменить реализацию
    }

    public BotPacMan getBotPacMan() {
        return botPacMen.get(0); // Если у вас несколько ботов, измените реализацию
    }

}
