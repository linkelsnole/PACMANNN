package my.snole.pacmannn.model;

import javafx.beans.property.*;
import javafx.geometry.Point2D;
import my.snole.pacmannn.model.ghost.Ghost;
import my.snole.pacmannn.model.ghost.GhostManager;
import my.snole.pacmannn.model.pacman.BotPacMan;
import my.snole.pacmannn.model.pacman.PacMan;
import my.snole.pacmannn.service.LevelService;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.Scanner;

/**
 * Класс PacManModel - модель игры Pac-Man.
 * Управляет состоянием игры: положение Pac-Man, привидений, ботов Pac-Man; карта.
 */
public class PacManModel {
    // Перечисление для обозначения значений ячеек в сетке
    public enum CellValue {
        EMPTY, SMALLDOT, BIGDOT, WALL, GHOST1HOME, GHOST2HOME, GHOST3HOME, GHOST4HOME, PACMANHOME
    }

    // Перечисление для обозначения направлений движения
    public enum Direction {
        UP, DOWN, LEFT, RIGHT, NONE
    }

    // Свойства для биндинга с представлением
    private final IntegerProperty score = new SimpleIntegerProperty(0);
    private final IntegerProperty level = new SimpleIntegerProperty(1);
    private final BooleanProperty gameOver = new SimpleBooleanProperty(false);
    private final BooleanProperty youWon = new SimpleBooleanProperty(false);
    private final BooleanProperty ghostEatingMode = new SimpleBooleanProperty(false);
    private final IntegerProperty ghostEatingModeCounter = new SimpleIntegerProperty(10);
    private final IntegerProperty dotCount = new SimpleIntegerProperty(0);

    private int rowCount; // количество строк в сетке
    private int columnCount; // количество колонок в сетке
    private CellValue[][] grid; // двумерный массив, представляющий игровую сетку

    private PacMan pacman; // объект PacMan
    private List<Ghost> ghosts; // список привидений
    private List<BotPacMan> botPacMen; // список ботов PacMan
    private GhostManager ghostManager; // менеджер привидений


    private static Direction currentDirection;
    private static Direction lastDirection;

    private final Map<Class<? extends Ghost>, javafx.scene.image.Image> ghostImages; // карта изображений привидений

    // Константы
    private static final int GHOST_EATING_MODE_DURATION = 10; // длительность режима поедания привидений
    private static final String[] levelFiles = {
            "level1.txt",
            "level2.txt",
            "level3.txt"
    };

    /**
     * Конструктор для инициализации модели игры.
     */
    public PacManModel() {
        this.ghosts = new ArrayList<>();
        this.botPacMen = new ArrayList<>();
        this.ghostManager = new GhostManager(this.ghosts);
        this.ghostImages = new HashMap<>();
        loadGhostImages();
        initializeLevel(0); // Начальный уровень
    }

    /**
     * Загрузка изображений привидений.
     */
    private void loadGhostImages() {
        // Предполагается, что изображения находятся в ресурсах
        ghostImages.put(my.snole.pacmannn.model.ghost.RedGhost.class,
                new javafx.scene.image.Image(getClass().getResourceAsStream("/image/redghost.gif")));
        ghostImages.put(my.snole.pacmannn.model.ghost.BlueGhost.class,
                new javafx.scene.image.Image(getClass().getResourceAsStream("/image/blueghost1.gif")));
        ghostImages.put(my.snole.pacmannn.model.ghost.PinkGhost.class,
                new javafx.scene.image.Image(getClass().getResourceAsStream("/image/pinkghost.gif")));
        ghostImages.put(my.snole.pacmannn.model.ghost.YellowGhost.class,
                new javafx.scene.image.Image(getClass().getResourceAsStream("/image/yellowghost.gif")));
    }

    /**
     * Метод для инициализации уровня игры.
     *
     * @param levelIndex индекс уровня
     */
    public void initializeLevel(int levelIndex) {
        if (levelIndex < 0 || levelIndex >= levelFiles.length) {
            throw new IllegalArgumentException("Invalid level index: " + levelIndex);
        }

        rowCount = 0;
        columnCount = 0;
        dotCount.set(0);

        String fileName = LevelService.getLevelFile(levelIndex);
        File file = new File(fileName);
        Scanner scanner;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Level file not found: " + fileName, e);
        }

        List<String> lines = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            lines.add(line);
            rowCount++;
        }
        if (rowCount > 0) {
            columnCount = lines.get(0).split(" ").length; // определяем количество колонок по первой строке
        }

        grid = new CellValue[rowCount][columnCount]; // инициализация сетки уровня
        int row = 0; // начальная строка
        int pacmanRow = 0; // строка для PacMan
        int pacmanColumn = 0; // колонка для PacMan
        for (String line : lines) {
            int column = 0;
            Scanner lineScanner = new Scanner(line);
            while (lineScanner.hasNext()) {
                String value = lineScanner.next(); // считываем значение ячейки
                CellValue thisValue; // переменная для хранения значения ячейки
                switch (value) {
                    case "W" -> thisValue = CellValue.WALL; // стена
                    case "S" -> {
                        thisValue = CellValue.SMALLDOT; // маленькая точка
                        dotCount.set(dotCount.get() + 1);
                    }
                    case "B" -> {
                        thisValue = CellValue.BIGDOT; // большая точка
                        dotCount.set(dotCount.get() + 1);
                    }
                    case "1" -> thisValue = CellValue.GHOST1HOME;
                    case "2" -> thisValue = CellValue.GHOST2HOME;
                    case "3" -> thisValue = CellValue.GHOST3HOME;
                    case "4" -> thisValue = CellValue.GHOST4HOME;
                    case "P" -> {
                        thisValue = CellValue.PACMANHOME; // дом PacMan
                        pacmanRow = row;
                        pacmanColumn = column;
                    }
                    default -> thisValue = CellValue.EMPTY;
                }
                grid[row][column] = thisValue; // заполняем сетку значениями ячеек
                column++; // переходим к следующей колонке
            }
            row++; // переходим к следующей строке
        }

        Point2D pacmanLocation = new Point2D(pacmanRow, pacmanColumn); // начальная позиция PacMan
        this.pacman = new PacMan(pacmanLocation, new Point2D(0, 0));
        this.ghosts.clear();
        this.botPacMen.clear();

        ghostManager = new GhostManager(this.ghosts); // инициализация менеджера привидений
    }

    /**
     * Метод для инициализации привидений на уровне.
     *
     * @param initialGhosts количество привидений для инициализации
     */
    public void initializeGhosts(int initialGhosts) {
        ghostManager.getGhosts().clear();
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

    /**
     * Добавление конкретного привидения на указанную позицию.
     *
     * @param ghostNumber номер привидения
     * @param location     позиция привидения
     */
    private void addSpecificGhost(int ghostNumber, Point2D location) {
        Class<? extends Ghost> ghostClass;
        switch (ghostNumber) {
            case 1 -> ghostClass = my.snole.pacmannn.model.ghost.RedGhost.class;
            case 2 -> ghostClass = my.snole.pacmannn.model.ghost.BlueGhost.class;
            case 3 -> ghostClass = my.snole.pacmannn.model.ghost.PinkGhost.class;
            case 4 -> ghostClass = my.snole.pacmannn.model.ghost.YellowGhost.class;
            default -> throw new IllegalArgumentException("Invalid ghost number: " + ghostNumber);
        }
        Ghost newGhost = createGhost(ghostClass, location, new Point2D(-1, 0));
        ghostManager.addGhost(newGhost);
        System.out.println("Добавлено привидение: " + newGhost.getClass().getSimpleName() + " в " + location);
    }

    /**
     * Начало новой игры.
     *
     * @param initialGhosts количество начальных привидений
     */
    public void startNewGame(int initialGhosts) {
        score.set(0);
        level.set(1);
        gameOver.set(false);
        youWon.set(false);
        ghostEatingMode.set(false);
        ghostEatingModeCounter.set(GHOST_EATING_MODE_DURATION);
        dotCount.set(0);
        initializeLevel(0);
        initializeGhosts(initialGhosts);
    }

    /**
     * Переход на следующий уровень.
     */
    public void startNextLevel() {
        if (isLevelComplete()) {
            if (level.get() < levelFiles.length) {
                level.set(level.get() + 1);
                initializeLevel(level.get() - 1);
                initializeGhosts(getInitialGhostCount());
            } else {
                youWon.set(true);
                gameOver.set(true);
                System.out.println("Все уровни пройдены! Вы победили!");
            }
        }
    }

    /**
     * Выполняет один шаг игры.
     *
     * @param direction направление движения Pac-Man
     */
    public void step(Direction direction) {
        if (!gameOver.get() && !youWon.get()) {
            pacman.move(direction, grid);
            moveGhosts();

            // Проверка столкновений и обновление состояния
            if (checkPacManCollisions()) {
                return;
            }

            CellValue pacmanLocationCellValue = grid[(int) pacman.getLocation().getX()][(int) pacman.getLocation().getY()];
            if (pacmanLocationCellValue == CellValue.SMALLDOT) {
                grid[(int) pacman.getLocation().getX()][(int) pacman.getLocation().getY()] = CellValue.EMPTY;
                dotCount.set(dotCount.get() - 1);
                score.set(score.get() + 10);
            }
            if (pacmanLocationCellValue == CellValue.BIGDOT) {
                grid[(int) pacman.getLocation().getX()][(int) pacman.getLocation().getY()] = CellValue.EMPTY;
                dotCount.set(dotCount.get() - 1);
                score.set(score.get() + 50);
                ghostEatingMode.set(true);
                ghostEatingModeCounter.set(GHOST_EATING_MODE_DURATION);
                for (Ghost ghost : ghosts) {
                    ghost.changeImageToBlue();
                    ghost.reverseDirection();
                }
            }

            // Обновление положения ботов и проверка на столкновение с едой
            for (Iterator<BotPacMan> iterator = botPacMen.iterator(); iterator.hasNext(); ) {
                BotPacMan botPacMan = iterator.next();
                botPacMan.move(grid, ghosts, botPacMen);
                for (Ghost ghost : ghosts) {
                    if (botPacMan.getLocation().equals(ghost.getLocation())) {
                        iterator.remove();
                        System.out.println("Бот Pac-Man был съеден привидением.");
                        break;
                    }
                }
            }

            // Повторная проверка столкновений после движения ботов
            checkPacManCollisions();

            decrementGhostEatingModeCounter();

            if (isLevelComplete()) {
                startNextLevel();
            }
        }
    }

    /**
     * Проверяет столкновения Pac-Man с привидениями.
     *
     * @return true, если произошло столкновение и игра окончена
     */
    private boolean checkPacManCollisions() {
        if (ghostEatingMode.get()) {
            for (Ghost ghost : ghosts) {
                if (pacman.getLocation().equals(ghost.getLocation())) {
                    sendGhostHome(ghost);
                    score.set(score.get() + 100);
                }
            }
        } else {
            for (Ghost ghost : ghosts) {
                if (pacman.getLocation().equals(ghost.getLocation())) {
                    gameOver.set(true);
                    pacman.setVelocity(new Point2D(0, 0));
                    System.out.println("Pac-Man столкнулся с привидением. Игра окончена.");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Перемещение привидений.
     */
    private void moveGhosts() {
        ghostManager.moveGhosts(pacman.getLocation(), botPacMen, ghostEatingMode.get(), grid);
    }

    /**
     * Отправляет привидение на его начальную позицию.
     *
     * @param ghost привидение
     */
    private void sendGhostHome(Ghost ghost) {
        for (int row = 0; row < this.rowCount; row++) {
            for (int column = 0; column < this.columnCount; column++) {
                if (grid[row][column] == CellValue.GHOST1HOME ||
                        grid[row][column] == CellValue.GHOST2HOME ||
                        grid[row][column] == CellValue.GHOST3HOME ||
                        grid[row][column] == CellValue.GHOST4HOME) {
                    ghost.setLocation(new Point2D(row, column));
                    ghost.setVelocity(new Point2D(-1, 0));
                    return;
                }
            }
        }
    }

    /**
     * Уменьшает счетчик режима поедания привидений.
     */
    private void decrementGhostEatingModeCounter() {
        if (ghostEatingModeCounter.get() > 0) {
            ghostEatingModeCounter.set(ghostEatingModeCounter.get() - 1);
            if (ghostEatingModeCounter.get() == 0) {
                ghostEatingMode.set(false);
                for (Ghost ghost : ghosts) {
                    ghost.resetImage(); // Сброс изображения привидения после завершения режима поедания
                }
                System.out.println("Режим поедания привидений завершен.");
            }
        }
    }

    /**
     * Проверяет, завершен ли уровень.
     *
     * @return true, если все точки собраны
     */
    public boolean isLevelComplete() {
        return dotCount.get() == 0;
    }

    /**
     * Добавляет ботов Pac-Man.
     *
     * @param count количество ботов
     */
    public void addBots(int count) {
        for (int i = 0; i < count; i++) {
            Point2D botLocation = findBotSpawnLocation();
            BotPacMan bot = new BotPacMan(botLocation, new Point2D(0, 0));
            botPacMen.add(bot);
            System.out.println("Добавился бот Pac-Man.");
        }
    }

    /**
     * Находит позицию для появления бота.
     *
     * @return позиция для появления бота
     */
    private Point2D findBotSpawnLocation() {
        for (int row = 0; row < getRowCount(); row++) {
            for (int column = 0; column < getColumnCount(); column++) {
                if (getCellValue(row, column) == CellValue.PACMANHOME) {
                    return new Point2D(row, column);
                }
            }
        }
        return new Point2D(1, 1); // Стандартная позиция, если дом не найден
    }

    /**
     * Добавляет привидение на случайную позицию.
     */
    public void addGhost() {
        Point2D location = findGhostSpawnLocation();
        Ghost newGhost = createRandomGhost(location, new Point2D(-1, 0));
        ghostManager.addGhost(newGhost);
        System.out.println("Добавлено привидение: " + newGhost.getClass().getSimpleName() + " в " + location);
    }

    /**
     * Создает привидение указанного класса.
     *
     * @param ghostClass класс привидения
     * @param location   позиция привидения
     * @param velocity   скорость привидения
     * @return объект привидения
     */
    private Ghost createGhost(Class<? extends Ghost> ghostClass, Point2D location, Point2D velocity) {
        try {
            Constructor<? extends Ghost> constructor = ghostClass.getConstructor(Point2D.class, Point2D.class, GhostManager.class, javafx.scene.image.Image.class);
            javafx.scene.image.Image defaultImage = ghostImages.get(ghostClass);
            return constructor.newInstance(location, velocity, this.ghostManager, defaultImage);
        } catch (Exception e) {
            e.printStackTrace();
            javafx.scene.image.Image defaultImage = new javafx.scene.image.Image(getClass().getResourceAsStream("/image/redghost.gif"));
            return new my.snole.pacmannn.model.ghost.RedGhost(location, velocity, this.ghostManager, defaultImage); // Возвращает красное привидение по умолчанию
        }
    }

    /**
     * Создает привидение случайного типа.
     *
     * @param location позиция привидения
     * @param velocity скорость привидения
     * @return объект привидения
     */
    private Ghost createRandomGhost(Point2D location, Point2D velocity) {
        Random random = new Random();
        int ghostType = random.nextInt(4);
        Class<? extends Ghost> ghostClass;
        switch (ghostType) {
            case 0 -> ghostClass = my.snole.pacmannn.model.ghost.RedGhost.class;
            case 1 -> ghostClass = my.snole.pacmannn.model.ghost.BlueGhost.class;
            case 2 -> ghostClass = my.snole.pacmannn.model.ghost.PinkGhost.class;
            case 3 -> ghostClass = my.snole.pacmannn.model.ghost.YellowGhost.class;
            default -> ghostClass = my.snole.pacmannn.model.ghost.RedGhost.class;
        }
        return createGhost(ghostClass, location, velocity);
    }

    /**
     * Находит позицию для появления привидения.
     *
     * @return позиция для появления привидения
     */
    private Point2D findGhostSpawnLocation() {
        List<Point2D> spawnLocations = new ArrayList<>();
        for (int row = 0; row < rowCount; row++) {
            for (int column = 0; column < columnCount; column++) {
                if (grid[row][column] == CellValue.GHOST1HOME ||
                        grid[row][column] == CellValue.GHOST2HOME ||
                        grid[row][column] == CellValue.GHOST3HOME ||
                        grid[row][column] == CellValue.GHOST4HOME) {
                    spawnLocations.add(new Point2D(row, column));
                }
            }
        }
        return spawnLocations.isEmpty() ? new Point2D(0, 0) : spawnLocations.get(new Random().nextInt(spawnLocations.size()));
    }

    /**
     * Получает количество привидений для начальной инициализации.
     *
     * @return количество привидений
     */
    private int getInitialGhostCount() {
        // Можно изменить эту логику в зависимости от требований
        return 2;
    }

    // Геттеры и сеттеры для свойств

    public IntegerProperty scoreProperty() {
        return score;
    }

    public int getScore() {
        return score.get();
    }

    public IntegerProperty levelProperty() {
        return level;
    }

    public int getLevel() {
        return level.get();
    }

    public BooleanProperty gameOverProperty() {
        return gameOver;
    }

    public boolean isGameOver() {
        return gameOver.get();
    }

    public BooleanProperty youWonProperty() {
        return youWon;
    }

    public boolean isYouWon() {
        return youWon.get();
    }

    public BooleanProperty ghostEatingModeProperty() {
        return ghostEatingMode;
    }

    public boolean isGhostEatingMode() {
        return ghostEatingMode.get();
    }

    public static Direction getLastDirection() {
        return lastDirection;
    }

    public static void setLastDirection(Direction direction) {
        lastDirection = direction;
    }

    public IntegerProperty ghostEatingModeCounterProperty() {
        return ghostEatingModeCounter;
    }
    public static void setCurrentDirection(Direction currentDirection) {
        PacManModel.currentDirection = currentDirection;
    }

    public static Direction getCurrentDirection() {
        return currentDirection;
    }

    public int getGhostEatingModeCounter() {
        return ghostEatingModeCounter.get();
    }

    public IntegerProperty dotCountProperty() {
        return dotCount;
    }

    public int getDotCount() {
        return dotCount.get();
    }

    public PacMan getPacman() {
        return pacman;
    }

    public List<Ghost> getGhosts() {
        return ghosts;
    }

    public List<BotPacMan> getBotPacMen() {
        return botPacMen;
    }

    public CellValue getCellValue(int row, int column) {
        if (row < 0 || row >= rowCount || column < 0 || column >= columnCount) {
            throw new IndexOutOfBoundsException("Invalid cell position: (" + row + ", " + column + ")");
        }
        return grid[row][column];
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    /**
     * Создает новый уровень игры.
     *
     * @param levelIndex индекс уровня
     */
    public void createNewLevel(int levelIndex) {
        initializeLevel(levelIndex);
        initializeGhosts(getInitialGhostCount());
    }

    /**
     * Добавляет бота Pac-Man.
     */
    public void addBotPacMan() {
        addBots(1);
    }
}
