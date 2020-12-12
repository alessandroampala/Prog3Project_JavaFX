package client;

import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSnackbarLayout;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import mail.Email;
import mail.Request;
import mail.User;
import server.RequestHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Client {
    private static final String ADDRESS = "localhost";
    private static final int PORT = 9000;

    private User user;
    private List<Email> emailsSent, emailsReceived;

    @FXML
    private Label username, fromNewMail;
    @FXML
    private VBox newMailContainer, readMailContainer;
    @FXML
    private HBox unselectedMailContainer;
    @FXML
    private TextField to, subject;
    @FXML
    private TextArea message;

    @FXML
    public void initialize() {
        username.setText("gatto@gatto.com");
        fromNewMail.setText("gatto@gatto.com");
        user = new User("gatto@gatto.com");
        emailsSent = new ArrayList<>();
        emailsReceived = new ArrayList<>();
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                loadEmails();
            }
        }, 0, 2, TimeUnit.MINUTES);
    }

    private void loadEmails() {
        new Thread(() -> {
            Socket socket = null;
            try {
                socket = new Socket(ADDRESS, PORT);
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                outStream.writeObject(new Request("Receive emails", user));
                Object obj = in.readObject();
                if (obj != null && obj.getClass().equals(Request.class)) {
                    Request received = (Request) obj;
                    switch (received.getType()) {
                        case "OK":
                            System.out.println("Got emails");
                            obj = received.getData();
                            if (obj != null && obj.getClass().getComponentType().equals(Email.class)) {
                                List<Email> emails = (List<Email>) obj;
                                Collections.sort(emails);
                                for (Email email : emails) {
                                    if (email.getFrom().equals(username.getText()))
                                        emailsReceived.add(email);
                                    else
                                        emailsSent.add(email);
                                }
                                user.setLastId(emails.get(0).getId() + 1);
                                //TODO: add this emails to List
                            }
                            break;
                        default:
                            System.out.println(received.getType());
                            break;
                    }
                }

            } catch (IOException e) {
                System.out.println("Connection Error");
            } catch (ClassNotFoundException e) {
                System.out.println("Class not found");
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
        Email email = new Email(-1, username.getText(), Arrays.asList(emails), subject.getText(), message.getText(), new Date());
        Request send = new Request("Send email", email);
        new Thread(() -> {
            Socket socket = null;
            try {
                socket = new Socket(ADDRESS, PORT);
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                outStream.writeObject(send);
                Object obj = in.readObject();
                if (obj != null && obj.getClass().equals(Request.class)) {
                    Request received = (Request) obj;
                    switch (received.getType()) {
                        case "OK":
                            System.out.println("Email sent");
                            obj = received.getData();
                            if (obj != null && obj.getClass().equals(Integer.class)) {
                                int mailId = (Integer) obj;
                                email.setId(mailId);
                                //TODO: add this email to Inviati List

                            }
                            break;
                        default:
                            System.out.println(received.getType());
                            break;
                    }
                }

            } catch (IOException e) {
                System.out.println("Connection Error");
            } catch (ClassNotFoundException e) {
                System.out.println("Class not found");
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

    /*private void sendRequest(Request send) {
        new Thread(() -> {
            Socket socket = null;
            try {
                socket = new Socket(ADDRESS, PORT);
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                outStream.writeObject(send);
                receivedRequest((Request) in.readObject());
            } catch (IOException e) {
                System.out.println("Connection Error");
            } catch (ClassNotFoundException e) {
                System.out.println("Class not found");
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

    private void receivedRequest(Request received) {
        switch (received.getType()) {
            case "Send email":
                System.out.println("Email sent");
                break;
        }
    }*/

    public void readMail() {
        newMailContainer.setVisible(false);
        readMailContainer.setVisible(true);
        unselectedMailContainer.setVisible(false);
    }
}
