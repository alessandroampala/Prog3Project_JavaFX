package client;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import mail.Email;
import mail.User;

import java.io.IOException;

public class CustomCell extends ListCell<Email> {

    private FXMLLoader fxmlLoader;
    private User user;
    private Button deleteBtnReceived, deleteBtnSent;

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
    @FXML
    private CheckBox checkToDelete;

    public CustomCell(User user, Button deleteBtnReceived, Button deleteBtnSent) {
        this.user = user;
        this.deleteBtnReceived = deleteBtnReceived;
        this.deleteBtnSent = deleteBtnSent;
    }

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

            checkToDelete.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    user.addIdsToDelete(email.getId());
                } else {
                    user.removeIdsToDelete(email.getId());
                }
                if (user.getIdsToDelete().size() > 0) {
                    deleteBtnReceived.setDisable(false);
                    deleteBtnSent.setDisable(false);
                } else {
                    deleteBtnReceived.setDisable(true);
                    deleteBtnSent.setDisable(true);
                }
            });

            setText(null);
            setGraphic(anchorPane);
        }

    }
}
