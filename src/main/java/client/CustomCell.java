package client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import mail.Email;

import java.io.IOException;

public class CustomCell extends ListCell<Email> {

    private FXMLLoader fxmlLoader;

    @FXML
    private Label titleLabel;
    @FXML
    private Label previewLabel;
    @FXML
    private Label fromLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private AnchorPane anchorPane;

    @Override
    protected void updateItem(Email email, boolean empty) {
        super.updateItem(email, empty);

        if (empty || email == null) {
            setText(null);
            setGraphic(null);
        } else {
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
            fromLabel.setText(email.getFrom());
            dateLabel.setText(email.getDate().toString());

            setText(null);
            setGraphic(anchorPane);
        }

    }
}
