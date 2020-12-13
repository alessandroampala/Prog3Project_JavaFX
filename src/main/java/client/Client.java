package client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mail.Email;
import mail.Request;
import mail.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Client {
    private static final String ADDRESS = "localhost";
    private static final int PORT = 9000;

    private User user;
    private List<Email> emailsSent, emailsReceived;
    private ScheduledExecutorService scheduledExecutor = null;

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
    private Button reply, replyToAll;
    @FXML
    private Label fromMail, toMail, subjectMail, messageMail;

    @FXML
    public void exitApplication() {
        if (scheduledExecutor != null)
            scheduledExecutor.shutdownNow();
        Platform.exit();
    }

    public void initializeClient(User user) {
        this.user = user;
        username.setText(user.getMail());
        fromNewMail.setText(user.getMail());
        emailsSent = new ArrayList<>();
        emailsReceived = new ArrayList<>();
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleAtFixedRate(new Thread(this::loadEmails), 0, 2, TimeUnit.MINUTES);
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
                    if ("OK".equals(received.getType())) {
                        System.out.println("Got emails");
                        obj = received.getData();
                        if (obj != null) {
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
                    } else {
                        System.out.println(received.getType());
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

    private void deleteEmail() {
        new Thread(() -> {
            Socket socket = null;
            try {
                socket = new Socket(ADDRESS, PORT);
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                outStream.writeObject(new Request("Delete emails", user));
                Object obj = in.readObject();
                if (obj != null && obj.getClass().equals(Request.class)) {
                    System.out.println("Emails deleted");
                    user.clearIdsToDelete();
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
    public void replyActionEvent(ActionEvent event) {
        Object source = event.getSource();

        String fromMailText = fromMail.getText();
        String toMailText = toMail.getText();
        String subjectMailText = subjectMail.getText();
        String messageMailText = messageMail.getText();

        if (source.equals(reply)) {
            to.setText(fromMailText);
        } else if (source.equals(replyToAll)) {
            toMailText.replace(username.getText(), fromMailText);
            to.setText(toMailText);
        } else {
            to.setText("");
        }
        subject.setText(subjectMailText);
        message.setText(messageMailText);
        newMail();
    }

    @FXML
    public void sendMail() {
        String[] emails = to.getText().toLowerCase().split(",");
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
                    if ("OK".equals(received.getType())) {
                        System.out.println("Email sent");
                        obj = received.getData();
                        if (obj != null && obj.getClass().equals(Integer.class)) {
                            int mailId = (Integer) obj;
                            email.setId(mailId);
                            //TODO: add this email to Inviati List

                        }
                    } else {
                        System.out.println(received.getType());
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

    public void readMail() {
        newMailContainer.setVisible(false);
        readMailContainer.setVisible(true);
        unselectedMailContainer.setVisible(false);
    }
}
