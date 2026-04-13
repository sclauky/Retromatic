package backend.memory;

import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public class MemoryController {

    @FXML private GridPane cardGrid;
    @FXML private Label movesLabel;
    @FXML private Label pairsLabel;
    @FXML private Label statusLabel;

    private MemoryModel model;
    private Image backImage;
    private boolean locked = false;

    private static final int CARD_W = 110;
    private static final int CARD_H = 140;
    private static final int COLS = 4;

    @FXML
    public void initialize() {
        model = new MemoryModel();
        try {
            backImage = new Image(getClass().getResourceAsStream("/frontend/memory_back.jpg"));
        } catch (Exception e) {
            backImage = null;
        }
        buildGrid();
        updateStats();
    }

    private void buildGrid() {
        cardGrid.getChildren().clear();
        List<MemoryModel.Card> cards = model.getCards();
        for (int i = 0; i < cards.size(); i++) {
            MemoryModel.Card card = cards.get(i);
            StackPane cell = createCardCell(card);
            cardGrid.add(cell, i % COLS, i / COLS);
        }
    }

    private StackPane createCardCell(MemoryModel.Card card) {
        Canvas canvas = new Canvas(CARD_W, CARD_H);
        drawBack(canvas);

        StackPane cell = new StackPane(canvas);
        cell.setAlignment(Pos.CENTER);
        cell.setStyle("-fx-cursor: hand;");

        cell.setOnMouseClicked(e -> {
            if (locked || !model.canFlip(card)) return;
            animateFlip(canvas, card, true, () -> {
                model.flip(card);
                updateStats();
                if (model.getFirstFlipped() != null && model.getSecondFlipped() != null) {
                    locked = true;
                    PauseTransition pause = new PauseTransition(Duration.millis(900));
                    pause.setOnFinished(ev -> {
                        boolean match = model.checkMatch();
                        if (!match) {
                            flipBackTwo();
                        } else {
                            locked = false;
                            updateStats();
                            if (model.isGameOver()) {
                                statusLabel.setText("BRAVO ! " + model.getMoves() + " coups");
                            }
                        }
                    });
                    pause.play();
                }
            });
        });

        return cell;
    }

    private void flipBackTwo() {
        List<MemoryModel.Card> cards = model.getCards();
        MemoryModel.Card c1 = model.getFirstFlipped();
        MemoryModel.Card c2 = model.getSecondFlipped();
        model.unflipTwo();

        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i) == c1 || cards.get(i) == c2) {
                StackPane cell = (StackPane) cardGrid.getChildren().get(i);
                Canvas canvas = (Canvas) cell.getChildren().get(0);
                animateFlip(canvas, cards.get(i), false, null);
            }
        }
        locked = false;
        updateStats();
    }

    private void animateFlip(Canvas canvas, MemoryModel.Card card, boolean toFront, Runnable onMidpoint) {
        ScaleTransition shrink = new ScaleTransition(Duration.millis(150), canvas);
        shrink.setFromX(1.0);
        shrink.setToX(0.0);

        ScaleTransition grow = new ScaleTransition(Duration.millis(150), canvas);
        grow.setFromX(0.0);
        grow.setToX(1.0);

        shrink.setOnFinished(e -> {
            if (onMidpoint != null) onMidpoint.run();
            if (toFront) {
                drawFront(canvas, card);
            } else {
                drawBack(canvas);
            }
        });

        SequentialTransition flip = new SequentialTransition(shrink, grow);
        flip.play();
    }

private void drawBack(Canvas canvas) {
    GraphicsContext gc = canvas.getGraphicsContext2D();
    gc.clearRect(0, 0, CARD_W, CARD_H);

    if (backImage != null) {
        gc.drawImage(backImage, 0, 0, CARD_W, CARD_H);
    } else {
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(0, 0, CARD_W, CARD_H);
    }
}

private void drawFront(Canvas canvas, MemoryModel.Card card) {
    GraphicsContext gc = canvas.getGraphicsContext2D();
    gc.clearRect(0, 0, CARD_W, CARD_H);

    if (card.matched) {
        gc.setFill(Color.web("#7ECFB3"));
    } else {
        gc.setFill(Color.web("#261A22"));
    }
    gc.fillRoundRect(2, 2, CARD_W - 4, CARD_H - 4, 16, 16);
    gc.setStroke(Color.web("#7ECFB3"));
    gc.setLineWidth(2);
    gc.strokeRoundRect(2, 2, CARD_W - 4, CARD_H - 4, 16, 16);

    drawIcon(gc, card.icon, CARD_W / 2.0, CARD_H / 2.0 - 10);

    gc.setFill(card.matched ? Color.web("#1C1018") : Color.web("#7ECFB3"));
    gc.setFont(Font.font("Courier New", 10));
    gc.setTextAlign(TextAlignment.CENTER);
    gc.fillText(card.icon, CARD_W / 2.0, CARD_H - 14);

}

    private void drawIcon(GraphicsContext gc, String icon, double cx, double cy) {
        gc.setStroke(Color.web("#1C1018"));
        gc.setFill(Color.web("#1C1018"));
        gc.setLineWidth(2);

        switch (icon) {
            case "ROCKET" -> {
                gc.setFill(Color.web("#F2EBE0"));
                gc.fillOval(cx - 14, cy - 30, 28, 45);
                gc.strokeOval(cx - 14, cy - 30, 28, 45);
                gc.setFill(Color.web("#E8472A"));
                gc.fillPolygon(new double[]{cx, cx - 14, cx + 14}, new double[]{cy - 30, cy - 10, cy - 10}, 3);
                gc.setFill(Color.web("#F5C94E"));
                gc.fillPolygon(new double[]{cx - 14, cx - 22, cx - 14}, new double[]{cy + 5, cy + 18, cy + 18}, 3);
                gc.fillPolygon(new double[]{cx + 14, cx + 22, cx + 14}, new double[]{cy + 5, cy + 18, cy + 18}, 3);
                gc.setFill(Color.web("#7ECFB3"));
                gc.fillOval(cx - 6, cy - 8, 12, 12);
            }
            case "PLANET" -> {
                gc.setFill(Color.web("#7ECFB3"));
                gc.fillOval(cx - 20, cy - 20, 40, 40);
                gc.strokeOval(cx - 20, cy - 20, 40, 40);
                gc.setStroke(Color.web("#F5C94E"));
                gc.setLineWidth(3);
                gc.strokeOval(cx - 30, cy - 10, 60, 20);
                gc.setStroke(Color.web("#1C1018"));
                gc.setLineWidth(2);
                gc.strokeOval(cx - 30, cy - 10, 60, 20);
            }
            case "STAR" -> {
                double[] xs = new double[10];
                double[] ys = new double[10];
                for (int i = 0; i < 10; i++) {
                    double angle = Math.PI / 2 + i * Math.PI / 5;
                    double r = (i % 2 == 0) ? 24 : 10;
                    xs[i] = cx + r * Math.cos(angle);
                    ys[i] = cy - r * Math.sin(angle);
                }
                gc.setFill(Color.web("#F5C94E"));
                gc.fillPolygon(xs, ys, 10);
                gc.strokePolygon(xs, ys, 10);
            }
            case "SATELLITE" -> {
                gc.setFill(Color.web("#F2EBE0"));
                gc.fillRect(cx - 8, cy - 8, 16, 16);
                gc.strokeRect(cx - 8, cy - 8, 16, 16);
                gc.setFill(Color.web("#7ECFB3"));
                gc.fillRect(cx - 28, cy - 5, 18, 10);
                gc.strokeRect(cx - 28, cy - 5, 18, 10);
                gc.fillRect(cx + 10, cy - 5, 18, 10);
                gc.strokeRect(cx + 10, cy - 5, 18, 10);
                gc.strokeLine(cx - 10, cy, cx - 28, cy);
                gc.strokeLine(cx + 10, cy, cx + 28, cy);
            }
            case "MOON" -> {
                gc.setFill(Color.web("#F5C94E"));
                gc.fillOval(cx - 20, cy - 22, 40, 44);
                gc.setFill(Color.web("#e29c4c"));
                gc.fillOval(cx - 6, cy - 22, 36, 44);
            }
            case "COMET" -> {
                gc.setFill(Color.web("#F2EBE0"));
                gc.fillOval(cx + 6, cy - 8, 18, 18);
                gc.strokeOval(cx + 6, cy - 8, 18, 18);
                gc.setStroke(Color.web("#7ECFB3"));
                gc.setLineWidth(2.5);
                gc.strokeLine(cx + 6, cy, cx - 24, cy - 16);
                gc.setLineWidth(1.5);
                gc.strokeLine(cx + 8, cy + 4, cx - 18, cy + 10);
                gc.setLineWidth(1);
                gc.strokeLine(cx + 10, cy - 4, cx - 12, cy - 18);
            }
            case "ASTRONAUT" -> {
                gc.setFill(Color.web("#F2EBE0"));
                gc.fillOval(cx - 14, cy - 28, 28, 28);
                gc.strokeOval(cx - 14, cy - 28, 28, 28);
                gc.setFill(Color.web("#7ECFB3"));
                gc.fillOval(cx - 8, cy - 22, 16, 16);
                gc.setFill(Color.web("#F2EBE0"));
                gc.fillRoundRect(cx - 16, cy + 2, 32, 26, 10, 10);
                gc.strokeRoundRect(cx - 16, cy + 2, 32, 26, 10, 10);
                gc.strokeLine(cx - 16, cy + 14, cx - 24, cy + 20);
                gc.strokeLine(cx + 16, cy + 14, cx + 24, cy + 20);
            }
            case "UFO" -> {
                gc.setFill(Color.web("#7ECFB3"));
                gc.fillOval(cx - 28, cy, 56, 20);
                gc.strokeOval(cx - 28, cy, 56, 20);
                gc.setFill(Color.web("#F2EBE0"));
                gc.fillOval(cx - 16, cy - 18, 32, 26);
                gc.strokeOval(cx - 16, cy - 18, 32, 26);
                gc.setFill(Color.web("#F5C94E"));
                gc.fillOval(cx - 5, cy + 22, 10, 10);
                gc.fillOval(cx - 18, cy + 18, 8, 8);
                gc.fillOval(cx + 10, cy + 18, 8, 8);
            }
        }
    }

    private void updateStats() {
        movesLabel.setText("COUPS : " + model.getMoves());
        pairsLabel.setText("PAIRES : " + model.getMatchedPairs() + " / " + MemoryModel.ICONS.length);
    }

    @FXML
    private void handleRestart() {
        model.reset();
        locked = false;
        statusLabel.setText("");
        buildGrid();
        updateStats();
    }

    @FXML
    private void handleBack() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/frontend/home.fxml"));
        Stage stage = (Stage) cardGrid.getScene().getWindow();
        stage.setScene(new Scene(root, 900, 660));
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
    }
}