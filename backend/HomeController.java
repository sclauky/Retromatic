package backend;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class HomeController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private void openFusion() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/frontend/fusion.fxml"));
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.setScene(new Scene(root, 900, 660));
        stage.setFullScreen(true);
        stage.setFullScreenExitHint(null);
    }

    @FXML
    private void openHangman() {
        System.out.println("Pendu - à implémenter");
    }

    @FXML
    private void openMemory() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/frontend/memory.fxml"));
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.setScene(new Scene(root, 900, 660));
        stage.setFullScreen(true);
        stage.setFullScreenExitHint(null);
    }

    @FXML
    private void openSudoku() {
        System.out.println("Sudoku - à implémenter");
    }
}