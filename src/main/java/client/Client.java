package client;

import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSnackbarLayout;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class Client {

    @FXML
    private Label username;
    @FXML
    private VBox newMailContainer, readMailContainer;
    @FXML
    private HBox unselectedMailContainer;
    @FXML
    private JFXTextField to, subject;
    @FXML
    private TextArea message;

    private JFXSnackbar jfxSnackbar;

    @FXML
    public void initialize() {
    }

    @FXML
    public void newMail() {
        newMailContainer.setVisible(true);
        readMailContainer.setVisible(false);
        unselectedMailContainer.setVisible(false);
    }

    @FXML
    public void sendMail() {
        String[] emails = to.getText().split(",");
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+.[a-zA-Z0-9-.]+$");

        if (emails.length == 0) {
            System.out.println("email");
            return;
        }

        for (String email : emails)
            if (email.trim().isBlank() || !pattern.matcher(email.trim()).matches()) {
                System.out.println("email");
                return;
            }

        if (subject.getText().isBlank()) {
            System.out.println("subject");
            return;
        } else if (message.getText().isBlank()) {
            System.out.println("message");
            return;
        }
        System.out.println("SENT");
    }

    public void readMail() {
        newMailContainer.setVisible(false);
        readMailContainer.setVisible(true);
        unselectedMailContainer.setVisible(false);
    }
}
