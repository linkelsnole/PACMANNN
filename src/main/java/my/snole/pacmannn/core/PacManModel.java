package my.snole.pacmannn.core;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import my.snole.pacmannn.model.ghost.*;
import my.snole.pacmannn.model.pacman.BotPacMan;
import my.snole.pacmannn.model.pacman.PacMan;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.util.*;

public class PacManModel {
    public enum CellValue {
        EMPTY, SMALLDOT, BIGDOT, WALL, GHOST1HOME, GHOST2HOME, GHOST3HOME, GHOST4HOME, PACMANHOME
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT, NONE
    }

    private int rowCount;
    private int columnCount;
    private CellValue[][] grid;
    public static int score;
    private int level;
    public static int dotCount;
    private static boolean gameOver;
    private static boolean youWon;
    public static boolean ghostEatingMode;
    private PacMan pacman;
    private List<Ghost> ghosts;
    private static Direction lastDirection;
    private static Direction currentDirection;
    private List<BotPacMan> botPacMen;
    private static final int GHOST_EATING_MODE_DURATION = 10;
    private static int ghostEatingModeCounter;
    private GhostManager ghostManager;
    private Controller controller;
    private final Map<Class<? extends Ghost>, Image> ghostImages;

    public PacManModel(Controller controller) {
        this.controller = controller;
        this.botPacMen = new ArrayList<>();
        this.ghosts = new ArrayList<>();
        this.ghostManager = new GhostManager(this.ghosts);
        this.ghostImages = new HashMap<>();
        this.ghostImages.put(RedGhost.class, new Image(getClass().getResourceAsStream("/image/redghost.gif")));
        this.ghostImages.put(BlueGhost.class, new Image(getClass().getResourceAsStream("/image/blueghost1.gif")));
        this.ghostImages.put(PinkGhost.class, new Image(getClass().getResourceAsStream("/image/pinkghost.gif")));
        this.ghostImages.put(YellowGhost.class, new Image(getClass().getResourceAsStream("/image/yellowghost.gif")));
        this.initializeLevel(Controller.getLevelFile(0));
    }

    public void initializeLevel(String fileName) {
        rowCount = 0;
        columnCount = 0;
        dotCount = 0;

        File file = new File(fileName);
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        List<String> lines = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            lines.add(line);
            rowCount++;
        }
        if (rowCount > 0) {
            columnCount = lines.get(0).split(" ").length;
        }

        grid = new CellValue[rowCount][columnCount];
        int row = 0;
        int pacmanRow = 0;
        int pacmanColumn = 0;
        for (String line : lines) {
            int column = 0;
            Scanner lineScanner = new Scanner(line);
            while (lineScanner.hasNext()) {
                String value = lineScanner.next();
                CellValue thisValue;
                switch (value) {
                    case "W" -> thisValue = CellValue.WALL;
                    case "S" -> {
                        thisValue = CellValue.SMALLDOT;
                        dotCount++;
                    }
                    case "B" -> {
                        thisValue = CellValue.BIGDOT;
                        dotCount++;
                    }
                    case "1" -> thisValue = CellValue.GHOST1HOME;
                    case "2" -> thisValue = CellValue.GHOST2HOME;
                    case "3" -> thisValue = CellValue.GHOST3HOME;
                    case "4" -> thisValue = CellValue.GHOST4HOME;
                    case "P" -> {
                        thisValue = CellValue.PACMANHOME;
                        pacmanRow = row;
                        pacmanColumn = column;
                    }
                    default -> thisValue = CellValue.EMPTY;
                }
                grid[row][column] = thisValue;
                column++;
            }
            row++;
        }

        Point2D pacmanLocation = new Point2D(pacmanRow, pacmanColumn);
        this.pacman = new PacMan(pacmanLocation, new Point2D(0, 0));
        this.ghosts = new ArrayList<>();
        this.botPacMen = new ArrayList<>();

        currentDirection = Direction.NONE;
        lastDirection = Direction.NONE;

        this.ghostManager = new GhostManager(this.ghosts);  // Ensure GhostManager is properly initialized with the ghosts
    }

    public void initializeGhosts(int initialGhosts) {
        this.ghostManager.getGhosts().clear(); // Очистим привидения перед добавлением новых
        int ghostsAdded = 0;
        for (int i = 1; i <= 4; i++) {
            if (ghostsAdded >= initialGhosts) break;
            for (int row = 0; row < getRowCount(); row++) {
                for (int column = 0; column < getColumnCount(); column++) {
                    CellValue cellValue = grid[row][column];
                    if ((i == 1 && cellValue == CellValue.GHOST1HOME) ||
                            (i == 2 && cellValue == CellValue.GHOST2HOME) ||
                            (i == 3 && cellValue == CellValue.GHOST3HOME) ||
                            (i == 4 && cellValue == CellValue.GHOST4HOME)) {
                        addSpecificGhost(i, new Point2D(row, column));
                        ghostsAdded++;
                        break;
                    }
                }
                if (ghostsAdded >= initialGhosts) break;
            }
        }
    }

    private void addSpecificGhost(int ghostNumber, Point2D location) {
        Class<? extends Ghost> ghostClass;
        switch (ghostNumber) {
            case 1 -> ghostClass = RedGhost.class;
            case 2 -> ghostClass = BlueGhost.class;
            case 3 -> ghostClass = PinkGhost.class;
            case 4 -> ghostClass = YellowGhost.class;
            default -> throw new IllegalArgumentException("Invalid ghost number: " + ghostNumber);
        }
        Ghost newGhost = createGhost(ghostClass, location, new Point2D(-1, 0));
        this.ghostManager.addGhost(newGhost);
        System.out.println("Added new ghost: " + newGhost.getClass().getSimpleName() + " at " + location);
    }

    public void startNewGame(int initialGhosts) {
        gameOver = false;
        youWon = false;
        ghostEatingMode = false;
        dotCount = 0;
        rowCount = 0;
        columnCount = 0;
        score = 0;
        level = 1;
        this.initializeLevel(Controller.getLevelFile(0));

        initializeGhosts(initialGhosts);
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
                int selectedGhostCount = controller.getSelectedGhostCount();
                initializeGhosts(selectedGhostCount);
                controller.autoNextLevel();
            } catch (ArrayIndexOutOfBoundsException e) {
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

        // Движение ботов и проверка на столкновение с привидениями
        for (Iterator<BotPacMan> iterator = botPacMen.iterator(); iterator.hasNext(); ) {
            BotPacMan botPacMan = iterator.next();
            botPacMan.move(grid, ghosts, botPacMen);
            for (Ghost ghost : ghosts) {
                if (botPacMan.getLocation().equals(ghost.getLocation())) {
                    iterator.remove();
                    break;
                }
            }
        }

        this.moveGhosts();

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
            for (Ghost ghost : ghosts) {
                ghost.changeImageToBlue();
            }
        }

        // Обновление положения ботов и проверка на столкновение с едой
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
                for (Ghost ghost : ghosts) {
                    ghost.changeImageToBlue();
                }
            }
        }

        if (ghostEatingMode) {
            for (Ghost ghost : ghosts) {
                if (pacman.getLocation().equals(ghost.getLocation())) {
                    sendGhostHome(ghost);
                    score += 100;
                }
            }
        } else {
            for (Ghost ghost : ghosts) {
                if (pacman.getLocation().equals(ghost.getLocation())) {
                    gameOver = true;
                    pacman.setVelocity(new Point2D(0, 0));
                }
            }
        }

        decrementGhostEatingModeCounter();

        if (this.isLevelComplete()) {
            pacman.setVelocity(new Point2D(0, 0));
            startNextLevel();
        }
    }

    public void moveGhosts() {
        ghostManager.moveGhosts(pacman.getLocation(), botPacMen, ghostEatingMode, grid);
    }

    public void sendGhostHome(Ghost ghost) {
        for (int row = 0; row < this.rowCount; row++) {
            for (int column = 0; column < this.columnCount; column++) {
                if (grid[row][column] == CellValue.GHOST1HOME) {
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

    public void decrementGhostEatingModeCounter() {
        if (ghostEatingModeCounter > 0) {
            ghostEatingModeCounter--;
            if (ghostEatingModeCounter == 0) {
                ghostEatingMode = false;
                for (Ghost ghost : ghosts) {
                    ghost.resetImage(); // Сброс изображения привидения после завершения режима поедания
                }
                controller.updateView(); // Добавим вызов обновления представления
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
        if (botPacMen.isEmpty()) {
            return new Point2D(0, 0);
        }
        return botPacMen.get(0).getLocation();
    }

    public BotPacMan getBotPacMan() {
        if (botPacMen.isEmpty()) {
            return null;
        }
        return botPacMen.get(0);
    }

    public List<BotPacMan> getBots() {
        return botPacMen;
    }

    public void addBots(int count) {
        for (int i = 0; i < count; i++) {
            Point2D botLocation = findBotSpawnLocation();
            BotPacMan bot = new BotPacMan(botLocation, new Point2D(0, 0));
            botPacMen.add(bot);
            System.out.println("добавился бот");
        }
    }

    private Point2D findBotSpawnLocation() {
        for (int row = 0; row < getRowCount(); row++) {
            for (int column = 0; column < getColumnCount(); column++) {
                if (getCellValue(row, column) == CellValue.PACMANHOME) {
                    return new Point2D(row, column);
                }
            }
        }
        return new Point2D(1, 1);
    }

    public void addGhost(Point2D location) {
        Ghost newGhost = createRandomGhost(location, new Point2D(-1, 0));
        this.ghostManager.addGhost(newGhost);
        System.out.println("Added new ghost: " + newGhost.getClass().getSimpleName() + " at " + location);
    }

    private Ghost createGhost(Class<? extends Ghost> ghostClass, Point2D location, Point2D velocity) {
        try {
            Constructor<? extends Ghost> constructor = ghostClass.getConstructor(Point2D.class, Point2D.class, GhostManager.class, Image.class);
            Image defaultImage = ghostImages.get(ghostClass);
            return constructor.newInstance(location, velocity, this.ghostManager, defaultImage);
        } catch (Exception e) {
            e.printStackTrace();
            Image defaultImage = new Image(getClass().getResourceAsStream("/image/redghost.gif"));
            return new RedGhost(location, velocity, this.ghostManager, defaultImage);  // Default to RedGhost in case of error
        }
    }

    private Ghost createRandomGhost(Point2D location, Point2D velocity) {
        Random random = new Random();
        int ghostType = random.nextInt(4);
        Class<? extends Ghost> ghostClass;
        switch (ghostType) {
            case 0 -> ghostClass = RedGhost.class;
            case 1 -> ghostClass = BlueGhost.class;
            case 2 -> ghostClass = PinkGhost.class;
            case 3 -> ghostClass = YellowGhost.class;
            default -> ghostClass = RedGhost.class;
        }
        return createGhost(ghostClass, location, velocity);
    }

    public void setGameOver(boolean gameOver) {
        PacManModel.gameOver = gameOver;
    }

    public List<Ghost> getGhosts() {
        return ghosts;
    }
}
