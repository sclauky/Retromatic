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
    private void openFusion() {
        System.out.println("Fusion (2048) - à implémenter");
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
    }

    @FXML
    private void openSudoku() {
        System.out.println("Sudoku - à implémenter");
    }
}