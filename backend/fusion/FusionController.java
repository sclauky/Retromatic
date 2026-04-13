package backend.fusion;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

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

    private void updateView() {
        grid.getChildren().clear();
        int[][] g = model.getGrid();
        scoreLabel.setText("Score : " + model.getScore());

        for (int r = 0; r < FusionModel.SIZE; r++) {
            for (int c = 0; c < FusionModel.SIZE; c++) {
                int value = g[r][c];
                StackPane cell = createTile(value);
                grid.add(cell, c, r);
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
        return pane;
    }

    private void showGameOver() {
        gameOverLabel.setText("GAME OVER");
        overlay.setVisible(true);
    }
}