package backend.sudoku;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class SudokuController {

    @FXML private GridPane sudokuGrid;
    @FXML private Label statusLabel;

    private SudokuModel model;
    private Label[][] cells = new Label[9][9];
    private StackPane[][] cellPanes = new StackPane[9][9];
    private Rectangle[][] cellBackgrounds = new Rectangle[9][9];
    private int selectedRow = -1;
    private int selectedCol = -1;

    @FXML
    public void initialize() {
        model = new SudokuModel();
        SudokuDatabaseHelper.initDatabase();
        int[][][] grid = SudokuDatabaseHelper.getRandomGrid();
        if (grid != null) {
            model.loadGrid(grid[0], grid[1]);
        }
        buildGrid();
    }

    private void buildGrid() {
        sudokuGrid.getChildren().clear();

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                final int r = row;
                final int c = col;

                StackPane cell = new StackPane();
                cell.setPrefSize(60, 60);

                Rectangle bg = new Rectangle(60, 60);
                bg.setFill(Color.web("#1a1a2e"));

                String style = "-fx-border-color: #3a3a5c; -fx-border-width: ";
                style += (row % 3 == 0 ? "3" : "1") + " ";
                style += (col % 3 == 2 ? "3" : "1") + " ";
                style += (row % 3 == 2 ? "3" : "1") + " ";
                style += (col % 3 == 0 ? "3" : "1") + ";";

                Label label = new Label();
                label.setAlignment(Pos.CENTER);
                label.getStyleClass().add("cell-label");

                int val = model.getUserGrid()[row][col];
                if (val != 0) {
                    label.setText(String.valueOf(val));
                }

                if (model.isPreFilled(row, col)) {
                    label.getStyleClass().add("cell-prefilled");
                } else {
                    label.setTextFill(Color.web("#F2EBE0"));
                }

                cell.setStyle(style);
                cell.getChildren().addAll(bg, label);
                cells[row][col] = label;
                cellPanes[row][col] = cell;
                cellBackgrounds[row][col] = bg;

                cell.setOnMouseClicked(e -> selectCell(r, c));
                sudokuGrid.add(cell, col, row);

                updateCellVisual(row, col, false);
            }
        }

        sudokuGrid.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(e -> {
                    String key = e.getText();
                    if (key.matches("[1-9]") && selectedRow >= 0) {
                        enterNumber(Integer.parseInt(key));
                    } else if (e.getCode().toString().equals("BACK_SPACE") && selectedRow >= 0) {
                        enterNumber(0);
                    }
                });
            }
        });
    }

    private void selectCell(int row, int col) {
        if (model.isPreFilled(row, col)) return;

        // Désélectionner l'ancienne cellule
        if (selectedRow >= 0) {
            updateCellVisual(selectedRow, selectedCol, false);
        }

        selectedRow = row;
        selectedCol = col;

        // Illuminer la cellule sélectionnée
        updateCellVisual(row, col, true);
    }

    private void enterNumber(int number) {
        if (selectedRow < 0 || model.isPreFilled(selectedRow, selectedCol)) return;

        model.setNumber(selectedRow, selectedCol, number);
        Label cell = cells[selectedRow][selectedCol];

        if (number == 0) {
            cell.setText("");
            cell.setTextFill(Color.web("#F2EBE0"));
        } else {
            cell.setText(String.valueOf(number));
            if (model.isCorrect(selectedRow, selectedCol)) {
                cell.setTextFill(Color.web("#7ECFB3")); // vert = correct
            } else {
                cell.setTextFill(Color.web("#E8472A")); // rouge = erreur
            }
        }

        if (model.isComplete()) {
            statusLabel.setText("🎉 BRAVO ! Sudoku complété !");
            statusLabel.setStyle("-fx-text-fill: #7ECFB3;");
        }
    }

    private void updateCellVisual(int row, int col, boolean selected) {
        StackPane pane = cellPanes[row][col];
        Rectangle bg = cellBackgrounds[row][col];

        if (pane == null || bg == null) {
            return;
        }

        if (selected) {
            pane.setStyle(getCellBorderStyle(row, col, "#7ECFB3", 4));
            bg.setFill(Color.web("#2a3559"));

            DropShadow glow = new DropShadow();
            glow.setBlurType(BlurType.GAUSSIAN);
            glow.setColor(Color.web("#7ECFB3"));
            glow.setRadius(16);
            glow.setSpread(0.45);
            pane.setEffect(glow);
        } else {
            pane.setStyle(getCellBorderStyle(row, col, "#3a3a5c", 3));
            bg.setFill(Color.web("#1a1a2e"));
            pane.setEffect(null);
        }
    }

    private String getCellBorderStyle(int row, int col, String color, int thickBorderSize) {
        return "-fx-border-color: " + color + "; -fx-border-width: "
            + (row % 3 == 0 ? thickBorderSize : 1) + " "
            + (col % 3 == 2 ? thickBorderSize : 1) + " "
            + (row % 3 == 2 ? thickBorderSize : 1) + " "
            + (col % 3 == 0 ? thickBorderSize : 1) + ";";
    }

    @FXML
    private void handleRestart() {
        SudokuDatabaseHelper.initDatabase();
        int[][][] grid = SudokuDatabaseHelper.getRandomGrid();
        if (grid != null) {
            model.loadGrid(grid[0], grid[1]);
        }
        selectedRow = -1;
        selectedCol = -1;
        statusLabel.setText("");
        statusLabel.setStyle("");
        buildGrid();
    }

    @FXML
    private void handleBack() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/frontend/home.fxml"));
        Stage stage = (Stage) sudokuGrid.getScene().getWindow();
        stage.setScene(new Scene(root, 900, 660));
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
    }
}