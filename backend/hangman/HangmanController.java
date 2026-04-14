package backend.hangman;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class HangmanController {

    @FXML private Canvas hangmanCanvas;
    @FXML private Label wordLabel;
    @FXML private Label triesLabel;
    @FXML private Label statusLabel;
    @FXML private FlowPane letterButtons;
    @FXML private VBox wrongLettersBox;

    private HangmanModel model;

    @FXML
    public void initialize() {
        model = new HangmanModel();
        createLetterButtons();
        updateDisplay();
        drawHangman();
    }

    // Créer les boutons A-Z
    private void createLetterButtons() {
        letterButtons.getChildren().clear();
        letterButtons.setHgap(8);
        letterButtons.setVgap(8);
        letterButtons.setAlignment(Pos.CENTER);

        for (char c = 'A'; c <= 'Z'; c++) {
            final char letter = c;
            Button btn = new Button(String.valueOf(c));
            btn.getStyleClass().add("letter-button");
            btn.setMinSize(45, 45);
            btn.setMaxSize(45, 45);
            
            btn.setOnAction(e -> handleLetterClick(letter, btn));
            letterButtons.getChildren().add(btn);
        }
    }

    // Gérer le clic sur une lettre
    private void handleLetterClick(char letter, Button btn) {
        if (model.isGameOver()) return;

        btn.setDisable(true);
        boolean correct = model.guessLetter(letter);

        if (correct) {
            btn.getStyleClass().add("letter-correct");
        } else {
            btn.getStyleClass().add("letter-wrong");
        }

        updateDisplay();
        drawHangman();

        if (model.isGameOver()) {
            disableAllButtons();
            if (model.hasWon()) {
                statusLabel.setText("🎉 BRAVO ! Mot trouvé : " + model.getSecretWord());
                statusLabel.setStyle("-fx-text-fill: #7ECFB3;");
            } else {
                statusLabel.setText("💀 PERDU ! Le mot était : " + model.getSecretWord());
                statusLabel.setStyle("-fx-text-fill: #E8472A;");
            }
        }
    }

    // Désactiver tous les boutons
    private void disableAllButtons() {
        for (var node : letterButtons.getChildren()) {
            if (node instanceof Button) {
                ((Button) node).setDisable(true);
            }
        }
    }

    // Mettre à jour l'affichage
    private void updateDisplay() {
        wordLabel.setText(model.getDisplayWord());
        triesLabel.setText("ESSAIS RESTANTS : " + model.getRemainingTries() + " / 6");

        // Afficher les lettres incorrectes
        StringBuilder wrong = new StringBuilder("Lettres incorrectes : ");
        for (char c : model.getWrongLetters()) {
            wrong.append(c).append(" ");
        }
        wrongLettersBox.getChildren().clear();
        Label wrongLabel = new Label(wrong.toString());
        wrongLabel.setStyle("-fx-text-fill: #E8472A; -fx-font-size: 14px;");
        wrongLettersBox.getChildren().add(wrongLabel);
    }

    // Dessiner le bonhomme pendu sur le Canvas
    private void drawHangman() {
        GraphicsContext gc = hangmanCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, hangmanCanvas.getWidth(), hangmanCanvas.getHeight());

        gc.setStroke(Color.web("#F2EBE0"));
        gc.setLineWidth(4);

        int errors = model.getErrorCount();

        // Base (potence)
        if (errors >= 1) {
            gc.strokeLine(50, 350, 200, 350); // Base
        }
        if (errors >= 2) {
            gc.strokeLine(100, 350, 100, 50); // Poteau vertical
        }
        if (errors >= 3) {
            gc.strokeLine(100, 50, 250, 50); // Poteau horizontal
            gc.strokeLine(250, 50, 250, 100); // Corde
        }

        // Bonhomme (style spatial/astronaute)
        gc.setFill(Color.web("#F2EBE0"));
        gc.setStroke(Color.web("#7ECFB3"));

        if (errors >= 4) {
            // Casque d'astronaute (tête)
            gc.setFill(Color.web("#F2EBE0"));
            gc.fillOval(220, 100, 60, 60);
            gc.strokeOval(220, 100, 60, 60);
            
            // Visière
            gc.setFill(Color.web("#7ECFB3"));
            gc.fillOval(230, 115, 40, 30);
        }
        if (errors >= 5) {
            // Corps (combinaison spatiale)
            gc.setFill(Color.web("#F2EBE0"));
            gc.fillRoundRect(230, 165, 40, 80, 10, 10);
            gc.strokeRoundRect(230, 165, 40, 80, 10, 10);
        }
        if (errors >= 6) {
            // Bras et jambes
            gc.setStroke(Color.web("#F2EBE0"));
            gc.setLineWidth(3);
            
            // Bras gauche
            gc.strokeLine(230, 180, 200, 200);
            // Bras droit
            gc.strokeLine(270, 180, 300, 200);
            // Jambe gauche
            gc.strokeLine(240, 245, 220, 290);
            // Jambe droite
            gc.strokeLine(260, 245, 280, 290);
        }
    }

    @FXML
    private void handleRestart() {
        model.reset();
        statusLabel.setText("");
        statusLabel.setStyle("");
        createLetterButtons();
        updateDisplay();
        drawHangman();
    }

    @FXML
    private void handleBack() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/frontend/home.fxml"));
        Stage stage = (Stage) hangmanCanvas.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
    }
}