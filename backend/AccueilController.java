package backend;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class AccueilController {

    @FXML
    private Button btnEntrer;

    @FXML
    private void handleEntrer() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/frontend/home.fxml"));
        Stage stage = (Stage) btnEntrer.getScene().getWindow();
        stage.setScene(new Scene(root, 900, 660));
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
    }
}