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

    private java.util.Map<String, Image> cardImages = new java.util.HashMap<>();

    @FXML
    public void initialize() {
        model = new MemoryModel();
        try {
            backImage = new Image(getClass().getResourceAsStream("/frontend/memory_back.jpg"));
            for (String icon : MemoryModel.ICONS) {
    try {
        Image img = new Image(getClass().getResourceAsStream(
            "/frontend/memory_card/" + icon + ".jpg"));
        cardImages.put(icon, img);
    } catch (Exception e) {
        cardImages.put(icon, null);
    }
            }

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

    // Fond carte
    if (card.matched) {
        gc.setFill(Color.web("#7ECFB3"));
    } else {
        gc.setFill(Color.web("#261A22"));
    }
    gc.fillRoundRect(2, 2, CARD_W - 4, CARD_H - 4, 16, 16);
    gc.setStroke(Color.web("#7ECFB3"));
    gc.setLineWidth(2);
    gc.strokeRoundRect(2, 2, CARD_W - 4, CARD_H - 4, 16, 16);

    int padding = 8;
    int nameZoneH = 22;
    int topMargin = 10;
    int imgSize = CARD_W - (padding * 2);  
    int imgHeight = CARD_H - topMargin - nameZoneH - padding;
    int imgX = padding;
    int imgY = topMargin;

    Image img = cardImages.get(card.icon);
    if (img != null && !img.isError()) {
        gc.drawImage(img, imgX, imgY, imgSize, imgHeight);
        gc.setStroke(card.matched ? Color.web("#1C1018") : Color.web("#7ECFB3"));
        gc.setLineWidth(1.5);
        gc.strokeRect(imgX, imgY, imgSize, imgHeight);
    }

    gc.setFill(card.matched ? Color.web("#1C1018") : Color.web("#F2EBE0"));
    gc.setFont(Font.font("Courier New", 9));
    gc.setTextAlign(TextAlignment.CENTER);
    gc.fillText(card.icon.toUpperCase(), CARD_W / 2.0, CARD_H - (nameZoneH / 2.0) + 3);
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
        stage.setScene(new Scene(root));
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
    }
}