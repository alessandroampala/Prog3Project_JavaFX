package client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import mail.Email;

import java.io.IOException;

public class CustomCell extends ListCell<Email> {

    private FXMLLoader fxmlLoader;
    private final boolean emailReceived;

    @FXML
    private Label titleLabel;
    @FXML
    private Label previewLabel;
    @FXML
    private Label fromLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private GridPane gridPane;

    public CustomCell(boolean emailReceived) {
        this.emailReceived = emailReceived;
    }

    @Override
    protected void updateItem(Email email, boolean empty) {
        super.updateItem(email, empty);

        if (empty || email == null) {
            Platform.runLater(() -> {
                setText(null);
                setGraphic(null);
            });
        } else {
            Platform.runLater(() -> {
                if (fxmlLoader == null) {
                    fxmlLoader = new FXMLLoader(getClass().getResource("customCell.fxml"));
                    fxmlLoader.setController(this);

                    try {
                        fxmlLoader.load();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                titleLabel.setText(email.getObject());
                previewLabel.setText(email.getText());
                if (this.emailReceived)
                    fromLabel.setText(email.getFrom());
                else
                    fromLabel.setText(email.getStringTo());
                dateLabel.setText(email.getDate().toString());

                setText(null);
                setGraphic(gridPane);
            });
        }
    }
}
