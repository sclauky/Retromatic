package backend.fusion;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.util.Duration;
import javafx.stage.Stage;

public class FusionController {

    @FXML private BorderPane root;
    @FXML private GridPane grid;
    @FXML private Label scoreLabel;
    @FXML private Label statusLabel;
    @FXML private StackPane overlay;
    @FXML private Label gameOverLabel;

    private FusionModel model;

    @FXML
    public void initialize() {
        model = new FusionModel();
        updateView();
        statusLabel.setText("Utilise les flèches pour jouer");
        javafx.application.Platform.runLater(() -> root.requestFocus());
    }

    @FXML
    private void handleKeyPressed(KeyEvent event) {
        if (overlay.isVisible()) return;

        KeyCode code = event.getCode();
        boolean moved = false;

        if (code == KeyCode.LEFT) moved = model.move(FusionModel.Direction.LEFT);
        else if (code == KeyCode.RIGHT) moved = model.move(FusionModel.Direction.RIGHT);
        else if (code == KeyCode.UP) moved = model.move(FusionModel.Direction.UP);
        else if (code == KeyCode.DOWN) moved = model.move(FusionModel.Direction.DOWN);

        if (moved) {
            updateView();
            if (model.hasWon()) statusLabel.setText("2048 ! Tu as gagné 🎉");
            else if (!model.hasMoves()) showGameOver();
            else statusLabel.setText("");
        }
    }

    @FXML
    private void handleNewGame() {
        model.reset();
        updateView();
        overlay.setVisible(false);
        statusLabel.setText("Nouvelle partie !");
        javafx.application.Platform.runLater(() -> root.requestFocus());
    }

    @FXML
    private void handleBack() throws Exception {
        Parent homeRoot = FXMLLoader.load(getClass().getResource("/frontend/home.fxml"));
        Stage stage = (Stage) root.getScene().getWindow();
        stage.setScene(new Scene(homeRoot, 900, 660));
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
    }

    private void updateView() {
        grid.getChildren().clear();
        int[][] g = model.getGrid();
        scoreLabel.setText("Score : " + model.getScore());

        for (int r = 0; r < FusionModel.SIZE; r++) {
            for (int c = 0; c < FusionModel.SIZE; c++) {
                int value = g[r][c];
                StackPane cell = createTile(value);
                grid.add(cell, c, r);

                if (model.wasMerged(r, c)) {
                    animatePop(cell);
                }
            }
        }
    }

    private StackPane createTile(int value) {
        StackPane pane = new StackPane();
        pane.getStyleClass().add("tile");
        if (value != 0) pane.getStyleClass().add("tile-" + value);
        else pane.getStyleClass().add("tile-empty");

        Label label = new Label(value == 0 ? "" : String.valueOf(value));
        label.getStyleClass().add("tile-label");
        pane.setAlignment(Pos.CENTER);
        pane.getChildren().add(label);

        TranslateTransition tt = new TranslateTransition(Duration.millis(90), pane);
        tt.setFromX(0);
        tt.setToX(0);
        tt.play();

        return pane;
    }

    private void showGameOver() {
        overlay.getStyleClass().add("overlay-visible");
        overlay.setVisible(true);
    }

    private void animatePop(StackPane tile) {
        ScaleTransition st = new ScaleTransition(Duration.millis(120), tile);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.18);
        st.setToY(1.18);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }
}