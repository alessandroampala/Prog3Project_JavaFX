package client;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Toast {
    @FXML
    private TextArea message;
    @FXML
    private Label success, error;

    private final String messageText;
    private final boolean errorFlag;

    // Constructor
    public Toast(String message, boolean errorFlag) {
        messageText = message;
        this.errorFlag = errorFlag;
    }

    // Loads the toast fxml and sets the default styles
    public void start() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("toast.fxml"));
        loader.setController(this);
        AnchorPane pane = loader.load();
        Stage popup = new Stage();
        popup.setX(10);
        popup.setY(10);
        popup.setMaxHeight(120);
        popup.setMaxWidth(300);
        popup.setMinHeight(120);
        popup.setMinWidth(300);
        popup.setScene(new Scene(pane));
        popup.setAlwaysOnTop(true);
        popup.initStyle(StageStyle.TRANSPARENT);
        if (errorFlag) {
            success.setVisible(false);
            success.setManaged(false);
        } else {
            error.setVisible(false);
            error.setManaged(false);
        }
        message.setWrapText(true);
        message.setText(messageText);
        popup.show();

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> popup.close());
        delay.play();
    }

    // Shows the toast
    public static void show(String content, boolean error) {
        Platform.runLater(() -> {
            try {
                new Toast(content, error).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
