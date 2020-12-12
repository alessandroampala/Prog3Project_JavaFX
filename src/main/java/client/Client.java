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
import mail.Email;
import mail.Request;
import server.RequestHandler;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class Client {
    private static final String ADDRESS = "localhost";
    private static final int PORT = 9000;

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
        Request send = new Request("Send email", new Email(-1, username.getText(), Arrays.asList(emails), subject.getText(), message.getText(), new Date()));
        sendRequest(send);
    }

    private void sendRequest(Request send) {
        new Thread(() -> {
            Socket socket = null;
            try {
                socket = new Socket(ADDRESS, PORT);

                Scanner in = new Scanner(socket.getInputStream());

                ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                outStream.writeObject(send);

                while (in.hasNext()) {
                    System.out.println(in.nextLine());
                }
            } catch (IOException e) {
                System.out.println("Connection Error");
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        System.out.println("Error during socket disconnection");
                    }
                }
            }
        }).start();
    }

    public void readMail() {
        newMailContainer.setVisible(false);
        readMailContainer.setVisible(true);
        unselectedMailContainer.setVisible(false);
    }
}
