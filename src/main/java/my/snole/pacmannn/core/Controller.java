package my.snole.pacmannn.core;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import my.snole.pacmannn.util.Database;
import my.snole.pacmannn.util.GameTimer;
import my.snole.pacmannn.util.PacManView;

import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Контроллер для управления игрой Pac-Man.
 * Реализует интерфейс EventHandler для обработки событий клавиш.
 */
public class Controller implements EventHandler<KeyEvent> {
    private static final double FRAMES_PER_SECOND = 5.0;

    @FXML private Label scoreLabel;
    @FXML private Label levelLabel;
    @FXML private Label gameOverLabel;
    @FXML private BorderPane borderPane;
    @FXML private Label timerLabel;
    @FXML private ComboBox<String> selectLvlCombo;
    @FXML private RadioButton radio2Ghosts;
    @FXML private RadioButton radio3Ghosts;
    @FXML private RadioButton radio4Ghosts;
    @FXML private Button addGhostButton;
    @FXML private  Button resumeButton;
    @FXML private Button startButton;
    private ToggleGroup ghostToggleGroup;
    private Timer timer;
    public Database database;
    private GameTimer gameTimer;
    private PacManView pacManView;
    private PacManModel pacManModel;
    private Button stopButton;
    private boolean paused;
    private boolean gameStarted;
    public static int ghostEatingModeCounter;
    private static final String[] levelFiles = {
            "level1.txt",
            "level2.txt",
            "level3.txt"
    };

    /**
     * Конструктор контроллера.
     * Инициализирует базу данных
     */
    public Controller() {
        this.paused = false;
        this.gameStarted = false;
        try {
            this.database = new Database();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Инициализация контроллера
     * Настройка таймера игры и элементов управления
     */
    public void initialize() {
        gameTimer = new GameTimer(timerLabel);
        this.pacManModel = new PacManModel(this);
        this.pacManView = new PacManView();
        this.pacManView.setRowCount(pacManModel.getRowCount());
        this.pacManView.setColumnCount(pacManModel.getColumnCount());
        this.borderPane.setCenter(this.pacManView);
        this.update(PacManModel.Direction.NONE);
        ghostEatingModeCounter = 10;
        startTimer();
        selectLvlCombo.getItems().addAll("Level 1", "Level 2", "Level 3");
        selectLvlCombo.setValue("Level 1");

        ghostToggleGroup = new ToggleGroup();
        radio2Ghosts.setToggleGroup(ghostToggleGroup);
        radio3Ghosts.setToggleGroup(ghostToggleGroup);
        radio4Ghosts.setToggleGroup(ghostToggleGroup);
        radio2Ghosts.setSelected(true);
        resumeButton.setDisable(true);
    }

    /**
     * Запуск таймера для обновления игры.
     * Таймер обновляется каждые 200 миллисекунд.
     */
    private void startTimer() {
        this.timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            public void run() {
                Platform.runLater(() -> {
                    if (gameStarted) {
                        update(pacManModel.getCurrentDirection());
                    }
                });
            }
        };

        // Вычисляем время кадра в миллисекундах исходя из заданной частоты кадров.
        // Запускаем таймер с нулевой задержкой и интервалом
        long frameTimeInMilliseconds = (long)(1000.0 / FRAMES_PER_SECOND);
        this.timer.schedule(timerTask, 0, frameTimeInMilliseconds);
    }

    /**
     * Обновление состояния игры.
     * @param direction направление движения Pac-Man
     */
    private void update(PacManModel.Direction direction) {
        if (!paused && gameStarted) {
            this.pacManModel.step(direction);
            this.pacManView.update(pacManModel);
            this.scoreLabel.setText(String.format("Score: %d", this.pacManModel.getScore()));
            this.levelLabel.setText(String.format("Level: %d", this.pacManModel.getLevel()));

            checkGameState(); // Проверка состояния игры после обновления
            
            checkGhostEatingMode();
        }
    }

    /**
     * Обработка нажатий клавиш.
     * @param keyEvent событие клавиши
     */
    @Override
    public void handle(KeyEvent keyEvent) {
        boolean keyRecognized = true;
        KeyCode code = keyEvent.getCode();
        PacManModel.Direction direction = PacManModel.Direction.NONE;
        if (code == KeyCode.LEFT) {
            direction = PacManModel.Direction.LEFT;
        } else if (code == KeyCode.RIGHT) {
            direction = PacManModel.Direction.RIGHT;
        } else if (code == KeyCode.UP) {
            direction = PacManModel.Direction.UP;
        } else if (code == KeyCode.DOWN) {
            direction = PacManModel.Direction.DOWN;
        } else if (code == KeyCode.G) {
            restartGame();
        } else {
            keyRecognized = false;
        }
        if (keyRecognized) {
            keyEvent.consume();
            pacManModel.setCurrentDirection(direction);
        }
    }

    /**
     * Перезапуск игры.
     * Сбрасывает все параметры и начинает новую игру
     */
    private void restartGame() {
        pause();
        int initialGhosts = getSelectedGhostCount();
        System.out.println("Restarting game with " + initialGhosts + " ghosts.");
        this.pacManModel.startNewGame(initialGhosts);
        this.gameOverLabel.setText("");
        paused = false;
        gameStarted = true;
        ghostEatingModeCounter = 10;
        gameTimer.reset();
        gameTimer.start();
        borderPane.requestFocus();
        selectLvlCombo.setValue("Level 1");
        startTimer();
        update(PacManModel.Direction.NONE);
    }

    /**
     * Пауза игры.
     */
    public void pause() {
        if (this.timer != null) {
            this.timer.cancel();
        }
        this.paused = true;
    }

    public double getBoardWidth() {
        return PacManView.CELL_WIDTH * this.pacManView.getColumnCount();
    }

    public double getBoardHeight() {
        return PacManView.CELL_WIDTH * this.pacManView.getRowCount();
    }

    public static void setGhostEatingModeCounter() {
        ghostEatingModeCounter = 10;
    }

    public static int getGhostEatingModeCounter() {
        return ghostEatingModeCounter;
    }

    public static String getLevelFile(int x) {
        return levelFiles[x];
    }

    public boolean getPaused() {
        return paused;
    }

    @FXML
    private void handleStartButtonAction() {
        gameTimer.start();
        gameStarted = true;
        borderPane.requestFocus();
        pacManModel.startNewGame(getSelectedGhostCount());
        startButton.setDisable(true);
    }

    @FXML
    private void handleResumeButtonAction() {
        gameTimer.start();
        gameStarted = true;
        resumeButton.setDisable(true);
        borderPane.requestFocus();
    }

    @FXML
    private void handleStopButtonAction() {
        gameTimer.stop();
        gameStarted = false;
        resumeButton.setDisable(false);
        borderPane.requestFocus();
    }

    @FXML
    private void handleAddBotsButton() {
        pacManModel.addBots(1);
        gameStarted = true;
        borderPane.requestFocus();
    }

    @FXML
    private void handleAddGhostButton(ActionEvent event) {
         pacManModel.addGhost();
        borderPane.requestFocus();
    }

    /**
     * Обработчик выбора уровня.
     * Инициализирует выбранный уровень
     */
    @FXML
    private void handleLevelSelection() {
        String selectedLevel = selectLvlCombo.getValue();
        if (selectedLevel != null) {
            int levelIndex = Integer.parseInt(selectedLevel.replace("Level ", "")) - 1;
            pacManModel.initializeLevel(Controller.getLevelFile(levelIndex));
            pacManModel.initializeGhosts(getSelectedGhostCount());
            pacManModel.setLevel(levelIndex + 1);
            pacManView.update(pacManModel);
            borderPane.requestFocus();
        }
    }

    /**
     * Обработчик кнопки перехода на следующий уровень
     */
    @FXML
    private void handleNextLevelButtonAction() {
        String selectedLevel = selectLvlCombo.getValue();
        if (selectedLevel != null) {
            int levelIndex = Integer.parseInt(selectedLevel.replace("Level ", "")) - 1;
            if (levelIndex < levelFiles.length - 1) {
                levelIndex++;
                selectLvlCombo.setValue("Level " + (levelIndex + 1));
                pacManModel.initializeLevel(Controller.getLevelFile(levelIndex));
                pacManModel.initializeGhosts(getSelectedGhostCount());
                int selectedGhostCount = getSelectedGhostCount();  // Логика остается в контроллере
                pacManModel.startNextLevel(selectedGhostCount);
                pacManView.update(pacManModel);
                pacManModel.setLevel(levelIndex + 1);
                borderPane.requestFocus();
            } else {
                // Завершаем игру, если нет больше уровней
                pacManModel.setLevel(levelIndex + 1);
                gameOverLabel.setText("YOU WON - No more levels");
                pacManModel.setGameOver(true);
                PacManModel.setYouWon(true);
                saveScoreToDB();
                gameTimer.stop();
                pause();
                borderPane.requestFocus();
            }
        }
    }

    /**
     * Автоматический переход на следующий уровень
     */
    public void autoNextLevel() {
        Platform.runLater(() -> handleNextLevelButtonAction());
    }

    /**
     * Возвращает количество выбранных привидений
     * @return количество выбранных привидений
     */
    public int getSelectedGhostCount() {
        RadioButton selectedRadioButton = (RadioButton) ghostToggleGroup.getSelectedToggle();
        return Integer.parseInt(selectedRadioButton.getText());
    }

    /**
     * Обновляет представление (view) игры
     */
    public void updateView() {
        pacManView.update(pacManModel);
    }

    /**
     * Сохраняет текущий результат(очки) игры в базу данных
     */
    private void saveScoreToDB () {
        try {
            database.saveScore(pacManModel.getScore(), pacManModel.getLevel(), PacManModel.isYouWon());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void checkGameState() {
        if (pacManModel.isLevelComplete()) {
            handleNextLevelButtonAction();  // Переход на следующий уровень
        } else if (pacManModel.isGameOver()) {
            displayGameOver();
        } else if (pacManModel.isYouWon()) {
            displayVictory();
        }
    }

    public void checkGhostEatingMode() {
        pacManModel.decrementGhostEatingModeCounter();
        updateView();  // Обновляем view вручную после проверки режима поедания
    }

    private void displayGameOver() {
        gameOverLabel.setText("GAME OVER");
        gameTimer.stop();  // Останавливаем таймер
    }

    private void displayVictory() {
        gameOverLabel.setText("YOU WON!");
        gameTimer.stop();  // Останавливаем таймер
    }
}
