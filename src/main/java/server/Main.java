package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    // Loads the fxml file and sets the default property
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("server.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Mail");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        Server server = loader.getController();
        primaryStage.setOnHiding(e -> server.exitApplication());
    }

    // Runs the GUI
    public static void main(String[] args) {
        launch(args);
    }
}
