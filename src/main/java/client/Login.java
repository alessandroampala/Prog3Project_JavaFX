package client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import mail.Request;
import mail.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class Login {
    private static final String ADDRESS = "localhost";
    private static final int PORT = 9000;

    private List<User> usersList;
    private ObservableList<String> users = FXCollections.observableArrayList();

    @FXML
    private ComboBox userSelected;
    @FXML
    private Button loginBtn;

    @FXML
    // Initializes the login controller
    public void initialize() {
        loadUsers();
    }

    // Requests to the server all the users
    private void loadUsers() {
        new Thread(() -> {
            Socket socket = null;
            try {
                socket = new Socket(ADDRESS, PORT);
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                outStream.writeObject(new Request("Load users", null));
                Object obj = in.readObject();
                if (obj != null && obj.getClass().equals(Request.class)) {
                    Request received = (Request) obj;
                    if ("OK".equals(received.getType())) {
                        System.out.println("Got users");
                        obj = received.getData();
                        if (obj != null) {
                            usersList = (List<User>) obj;
                            for (User user : usersList)
                                users.add(user.getMail());
                            userSelected.setItems(users);
                        }
                    } else {
                        System.out.println(received.getType());
                        Toast.show(received.getType(), true);
                    }
                }
            } catch (IOException e) {
                System.out.println("Connection Error");
                Toast.show("Connection Error", true);
            } catch (ClassNotFoundException e) {
                System.out.println("Class not found");
                Toast.show("Class not found", true);
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        System.out.println("Error during socket disconnection");
                        Toast.show("Error during socket disconnection", true);
                    }
                }
            }
        }).start();
    }

    @FXML
    // Loads the client fxml and its default styles, initializes the client controller
    public void login() throws Exception {
        if (userSelected.getSelectionModel().getSelectedItem().equals("- Seleziona una mail -"))
            return;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("client.fxml"));
        AnchorPane pane = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Mail");
        stage.setMinWidth(900);
        stage.setMinHeight(500);
        stage.setWidth(900);
        stage.setHeight(500);
        stage.setScene(new Scene(pane));
        Client client = loader.getController();
        for (User user : usersList) {
            if (user.getMail().equals(userSelected.getSelectionModel().getSelectedItem()))
                client.initializeClient(user);
        }
        stage.show();

        ((Stage) loginBtn.getScene().getWindow()).close();
        stage.setOnHiding(e -> client.exitApplication());
    }
}
