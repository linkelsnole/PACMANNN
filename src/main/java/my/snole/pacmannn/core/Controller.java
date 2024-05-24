package my.snole.pacmannn.core;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import my.snole.pacmannn.util.GameTimer;
import my.snole.pacmannn.util.PacManView;

import java.util.Timer;
import java.util.TimerTask;


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
    private ToggleGroup ghostToggleGroup;
    private Timer timer;
    private GameTimer gameTimer;
    private PacManView pacManView;
    private PacManModel pacManModel;
    private boolean paused;
    private boolean gameStarted;
    public static int ghostEatingModeCounter;
    private static final String[] levelFiles = {
            "level1.txt",
            "level2.txt",
            "level3.txt"
    };

    public Controller() {
        this.paused = false;
        this.gameStarted = false;
    }

    public void initialize() {
        gameTimer = new GameTimer(timerLabel);
        this.pacManModel = new PacManModel();
        this.pacManView = new PacManView();
        this.pacManView.setRowCount(pacManModel.getRowCount());
        this.pacManView.setColumnCount(pacManModel.getColumnCount());
        this.borderPane.setCenter(this.pacManView);
        this.update(PacManModel.Direction.NONE);
        ghostEatingModeCounter = 25;
        startTimer();
        selectLvlCombo.getItems().addAll("Level 1", "Level 2", "Level 3");
        selectLvlCombo.setValue("Level 1");

        // Set up the toggle group for ghost radio buttons
        ghostToggleGroup = new ToggleGroup();
        radio2Ghosts.setToggleGroup(ghostToggleGroup);
        radio3Ghosts.setToggleGroup(ghostToggleGroup);
        radio4Ghosts.setToggleGroup(ghostToggleGroup);
        radio2Ghosts.setSelected(true); // Default selection
    }

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

        long frameTimeInMilliseconds = (long)(1000.0 / FRAMES_PER_SECOND);
        this.timer.schedule(timerTask, 0, frameTimeInMilliseconds);
    }

    private void update(PacManModel.Direction direction) {
        if (!paused && gameStarted) {
            this.pacManModel.step(direction);
            this.pacManView.update(pacManModel);
            this.scoreLabel.setText(String.format("Score: %d", this.pacManModel.getScore()));
            this.levelLabel.setText(String.format("Level: %d", this.pacManModel.getLevel()));
            if (pacManModel.isGameOver()) {
                this.gameOverLabel.setText("GAME OVER");
                pause();
            }
            if (pacManModel.isYouWon()) {
                this.gameOverLabel.setText("YOU WON!");
            }
            if (pacManModel.isGhostEatingMode()) {
                ghostEatingModeCounter--;
            }
            if (ghostEatingModeCounter == 0 && pacManModel.isGhostEatingMode()) {
                pacManModel.setGhostEatingMode(false);
            }
        }
    }

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

    private void restartGame() {
        pause();
        int initialGhosts = getSelectedGhostCount();
        System.out.println("Restarting game with " + initialGhosts + " ghosts.");
        this.pacManModel.startNewGame(initialGhosts);
        this.gameOverLabel.setText("");
        paused = false;
        gameStarted = true;
        ghostEatingModeCounter = 25;
        gameTimer.reset();
        gameTimer.start();
        borderPane.requestFocus();
        startTimer();
        update(PacManModel.Direction.NONE);
    }

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
        ghostEatingModeCounter = 25;
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
    }

    @FXML
    private void handleStopButtonAction() {
        gameTimer.stop();
        gameStarted = false;
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

    @FXML
    private void handleLevelSelection() {
        String selectedLevel = selectLvlCombo.getValue();
        if (selectedLevel != null) {
            int levelIndex = Integer.parseInt(selectedLevel.replace("Level ", "")) - 1;
            pacManModel.initializeLevel(Controller.getLevelFile(levelIndex));
            pacManModel.initializeGhosts(getSelectedGhostCount());
            pacManView.update(pacManModel);
            borderPane.requestFocus();
        }
    }

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
                pacManView.update(pacManModel);
                borderPane.requestFocus();
            } else {
                // Завершаем игру, если нет больше уровней
                gameOverLabel.setText("GAME OVER - No more levels");
                gameTimer.stop();
                pacManModel.setGameOver(true);
                pause();
            }
        }
    }

    public int getSelectedGhostCount() {
        RadioButton selectedRadioButton = (RadioButton) ghostToggleGroup.getSelectedToggle();
        return Integer.parseInt(selectedRadioButton.getText());
    }
}
