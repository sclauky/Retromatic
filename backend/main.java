package backend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        Parent root = FXMLLoader.load(getClass().getResource("/frontend/accueil.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("RETROMATIC");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}