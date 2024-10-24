package my.snole.pacmannn.core;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import my.snole.pacmannn.model.PacManModel;
import my.snole.pacmannn.service.LevelService;
import my.snole.pacmannn.util.Database;
import my.snole.pacmannn.util.GameTimer;
import my.snole.pacmannn.util.PacManView;

import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Контроллер для управления игрой Pac-Man.
 * Обрабатывает пользовательский ввод и взаимодействует с моделью.
 */
public class Controller implements javafx.event.EventHandler<KeyEvent> {
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
    @FXML private Button resumeButton;
    @FXML private Button startButton;
    @FXML private Button stopButton;
    @FXML private Button nextLevelButton;

    private ToggleGroup ghostToggleGroup;
    private Timer timer;
    public Database database;
    private GameTimer gameTimer;
    private PacManModel pacManModel;
    private PacManView pacManView;
    private boolean paused;
    private boolean gameStarted;
    private int ghostEatingModeCounter;

    public Controller() {
        this.paused = false;
        this.gameStarted = false;
        try {
            this.database = new Database();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        // Инициализируем модель
        this.pacManModel = new PacManModel();

        // Инициализируем представление и связываем его с моделью
        this.pacManView = new PacManView();
        this.pacManView.setRowCount(pacManModel.getRowCount());
        this.pacManView.setColumnCount(pacManModel.getColumnCount());
        this.borderPane.setCenter(this.pacManView);
        this.pacManView.update(pacManModel); // Первоначальное обновление

        // Настройка элементов управления
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

        // Подписка на изменения модели для обновления представления
        pacManModel.scoreProperty().addListener((obs, oldVal, newVal) -> {
            scoreLabel.setText(String.format("Score: %d", newVal.intValue()));
        });

        pacManModel.levelProperty().addListener((obs, oldVal, newVal) -> {
            levelLabel.setText(String.format("Level: %d", newVal.intValue()));
        });

        pacManModel.gameOverProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                gameOverLabel.setText("GAME OVER");
                pause();
                gameTimer.stop();
                saveScoreToDB();
            } else {
                gameOverLabel.setText("");
            }
        });

        pacManModel.youWonProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                gameOverLabel.setText("YOU WON!");
                gameTimer.stop();
                saveScoreToDB();
            }
        });

        pacManModel.ghostEatingModeProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                ghostEatingModeCounter = 10;
            }
        });

        // Инициализация таймера
        gameTimer = new GameTimer(timerLabel);
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
                    if (gameStarted && !paused) {
                        pacManModel.step(pacManModel.getCurrentDirection());
                        pacManView.update(pacManModel);
                        if (pacManModel.isGhostEatingMode()) {
                            ghostEatingModeCounter--;
                            if (ghostEatingModeCounter == 0) {
                                pacManModel.ghostEatingModeProperty().set(false);
                            }
                        }
                    }
                });
            }
        };

        long frameTimeInMilliseconds = (long) (1000.0 / FRAMES_PER_SECOND);
        this.timer.schedule(timerTask, 0, frameTimeInMilliseconds);
    }

    /**
     * Обработка нажатий клавиш.
     *
     * @param keyEvent событие клавиши
     */
    @Override
    public void handle(KeyEvent keyEvent) {
        boolean keyRecognized = true;
        KeyCode code = keyEvent.getCode();
        PacManModel.Direction direction = PacManModel.Direction.NONE;
        switch (code) {
            case LEFT -> direction = PacManModel.Direction.LEFT;
            case RIGHT -> direction = PacManModel.Direction.RIGHT;
            case UP -> direction = PacManModel.Direction.UP;
            case DOWN -> direction = PacManModel.Direction.DOWN;
            case G -> restartGame();
            default -> keyRecognized = false;
        }
        if (keyRecognized) {
            keyEvent.consume();
            pacManModel.setCurrentDirection(direction);
        }
    }

    /**
     * Перезапуск игры.
     * Сбрасывает все параметры и начинает новую игру.
     */
    private void restartGame() {
        pause();
        int initialGhosts = getSelectedGhostCount();
        pacManModel.startNewGame(initialGhosts);
        pacManView.setRowCount(pacManModel.getRowCount());
        pacManView.setColumnCount(pacManModel.getColumnCount());
        pacManView.update(pacManModel);
        gameOverLabel.setText("");
        paused = false;
        gameStarted = true;
        ghostEatingModeCounter = 10;
        gameTimer.reset();
        gameTimer.start();
        borderPane.requestFocus();
        selectLvlCombo.setValue("Level 1");
        startTimer();
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

    /**
     * Возвращает ширину игрового поля.
     *
     * @return ширина игрового поля
     */
    public double getBoardWidth() {
        return PacManView.CELL_WIDTH * this.pacManView.getColumnCount();
    }

    /**
     * Возвращает высоту игрового поля.
     *
     * @return высота игрового поля
     */
    public double getBoardHeight() {
        return PacManView.CELL_WIDTH * this.pacManView.getRowCount();
    }

    /**
     * Обработчик кнопки "Start".
     *
     * @param event событие действия
     */
    @FXML
    private void handleStartButtonAction(ActionEvent event) {
        gameTimer.start();
        gameStarted = true;
        borderPane.requestFocus();
        pacManModel.startNewGame(getSelectedGhostCount());
        pacManView.setRowCount(pacManModel.getRowCount());
        pacManView.setColumnCount(pacManModel.getColumnCount());
        pacManView.update(pacManModel);
        startButton.setDisable(true);
    }

    /**
     * Обработчик кнопки "Resume".
     *
     * @param event событие действия
     */
    @FXML
    private void handleResumeButtonAction(ActionEvent event) {
        gameTimer.start();
        gameStarted = true;
        resumeButton.setDisable(true);
        borderPane.requestFocus();
    }

    /**
     * Обработчик кнопки "Stop".
     *
     * @param event событие действия
     */
    @FXML
    private void handleStopButtonAction(ActionEvent event) {
        gameTimer.stop();
        pause();
        gameStarted = false;
        resumeButton.setDisable(false);
        borderPane.requestFocus();
    }

    /**
     * Обработчик кнопки "Add Ghost".
     *
     * @param event событие действия
     */
    @FXML
    private void handleAddGhostButton(ActionEvent event) {
        pacManModel.addGhost();
        pacManView.setRowCount(pacManModel.getRowCount());
        pacManView.setColumnCount(pacManModel.getColumnCount());
        pacManView.update(pacManModel);
        borderPane.requestFocus();
    }

    /**
     * Обработчик выбора уровня.
     *
     * @param event событие действия
     */
    @FXML
    private void handleLevelSelection(ActionEvent event) {
        String selectedLevel = selectLvlCombo.getValue();
        if (selectedLevel != null) {
            int levelIndex = Integer.parseInt(selectedLevel.replace("Level ", "")) - 1;
            pacManModel.createNewLevel(levelIndex);
            pacManView.setRowCount(pacManModel.getRowCount());
            pacManView.setColumnCount(pacManModel.getColumnCount());
            pacManView.update(pacManModel);
            borderPane.requestFocus();
        }
    }

    /**
     * Обработчик кнопки "Next Level".
     *
     * @param event событие действия
     */
    @FXML
    private void handleNextLevelButtonAction(ActionEvent event) {
        pacManModel.startNextLevel();
        pacManView.setRowCount(pacManModel.getRowCount());
        pacManView.setColumnCount(pacManModel.getColumnCount());
        pacManView.update(pacManModel);
        borderPane.requestFocus();
    }

    /**
     * Автоматический переход на следующий уровень.
     */
    public void autoNextLevel() {
        Platform.runLater(() -> handleNextLevelButtonAction(null));
    }

    /**
     * Возвращает количество выбранных привидений.
     *
     * @return количество привидений
     */
    public int getSelectedGhostCount() {
        RadioButton selectedRadioButton = (RadioButton) ghostToggleGroup.getSelectedToggle();
        return Integer.parseInt(selectedRadioButton.getText());
    }

    /**
     * Сохраняет текущий результат (очки) игры в базу данных.
     */
    private void saveScoreToDB() {
        try {
            database.saveScore(pacManModel.getScore(), pacManModel.getLevel(), pacManModel.isYouWon());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddBotsButton() {
        pacManModel.addBots(1);
        gameStarted = true;
        borderPane.requestFocus();
    }
}
