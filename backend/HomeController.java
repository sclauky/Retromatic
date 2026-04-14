package backend;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class HomeController {

    @FXML
    private BorderPane rootPane;

    private Timeline standbyFlicker;

    @FXML
    public void initialize() {
        // Mimics a subtle CRT standby brightness drift.
        standbyFlicker = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(rootPane.opacityProperty(), 1.0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(260), new KeyValue(rootPane.opacityProperty(), 0.988, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(680), new KeyValue(rootPane.opacityProperty(), 0.996, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(1120), new KeyValue(rootPane.opacityProperty(), 0.985, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(1680), new KeyValue(rootPane.opacityProperty(), 1.0, Interpolator.EASE_BOTH))
        );
        standbyFlicker.setCycleCount(Animation.INDEFINITE);
        standbyFlicker.play();
    }

    @FXML
    private void openFusion() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/frontend/fusion.fxml"));
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setFullScreen(true);
        stage.setFullScreenExitHint(null);
    }

    @FXML
    private void openHangman() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/frontend/hangman.fxml"));
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
    }

    @FXML
    private void openMemory() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/frontend/memory.fxml"));
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setFullScreen(true);
        stage.setFullScreenExitHint(null);
    }

    @FXML
private void openSudoku() throws Exception {
    Parent root = FXMLLoader.load(getClass().getResource("/frontend/sudoku.fxml"));
    Stage stage = (Stage) rootPane.getScene().getWindow();
    stage.setScene(new Scene(root, 900, 660));
    stage.setFullScreen(true);
    stage.setFullScreenExitHint("");
    stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
}
}