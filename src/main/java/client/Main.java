package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Login");
        primaryStage.setMinWidth(300);
        primaryStage.setMinHeight(200);
        primaryStage.setWidth(300);
        primaryStage.setHeight(200);
        primaryStage.setMaxWidth(300);
        primaryStage.setMaxHeight(200);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
