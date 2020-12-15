package client;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Client {
    private static final String ADDRESS = "localhost";
    private static final int PORT = 9000;

    private User user;
    private final ObservableList<Email> emailsSent = FXCollections.observableArrayList();
    private final ObservableList<Email> emailsReceived = FXCollections.observableArrayList();
    private ScheduledExecutorService scheduledExecutor = null;

    @FXML
    private Label username, fromNewMail, fromMail, toMail, subjectMail, messageMail;
    @FXML
    private VBox newMailContainer, readMailContainer;
    @FXML
    private HBox unselectedMailContainer;
    @FXML
    private TextField to, subject;
    @FXML
    private TextArea message;
    @FXML
    private Button reply, replyToAll, inoltra, deleteReceived, deleteSent;
    @FXML
    private ListView<Email> listViewReceived, listViewSent;
    @FXML
    private TabPane tabPane;

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
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleAtFixedRate(this::loadEmails, 0, 2, TimeUnit.MINUTES);
        listViewReceived.setItems(emailsReceived);
        listViewSent.setItems(emailsSent);
        listViewReceived.setCellFactory(emailListView -> new CustomCell(user, deleteReceived, deleteSent, true));
        listViewSent.setCellFactory(emailListView -> new CustomCell(user, deleteReceived, deleteSent, false));

        // Display selected mail on list selection
        ChangeListener<Email> selectedChangeListener = new ChangeListener<Email>() {
            @Override
            public void changed(ObservableValue<? extends Email> observable, Email oldValue, Email newValue) {
                if (newValue != null) {
                    subjectMail.setText(newValue.getObject());
                    messageMail.setText(newValue.getText());
                    fromMail.setText(newValue.getFrom());
                    toMail.setText(newValue.getStringTo());
                    readMail();
                }
            }
        };
        listViewReceived.getSelectionModel().selectedItemProperty().addListener(selectedChangeListener);
        listViewSent.getSelectionModel().selectedItemProperty().addListener(selectedChangeListener);

        // Clear list selection when changing tab
        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                if (readMailContainer.isVisible())
                    unselectedMail();
                if (tabPane.getSelectionModel().getSelectedIndex() == 1) {
                    reply.setVisible(false);
                    replyToAll.setVisible(false);
                } else {
                    reply.setVisible(true);
                    replyToAll.setVisible(true);
                }
                clearListsSelection();
            }
        });

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
                                if (email.getIsSent())
                                    emailsSent.add(email);
                                else
                                    emailsReceived.add(email);
                            }
                            user.setLastId(emails.get(0).getId() + 1);
                        }
                    } else {
                        System.out.println(received.getType());
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    new Toast(received.getType(), true).start();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }

            } catch (IOException e) {
                System.out.println("Connection Error");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new Toast("Connection Error", true).start();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                });
            } catch (ClassNotFoundException e) {
                System.out.println("Class not found");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new Toast("Class not found", true).start();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                });
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        System.out.println("Error during socket disconnection");
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    new Toast("Error during socket disconnection", true).start();
                                } catch (Exception e2) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }
        }).start();
    }

    @FXML
    private void deleteEmails() {
        new Thread(() -> {
            Socket socket = null;
            try {
                socket = new Socket(ADDRESS, PORT);
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                outStream.writeObject(new Request("Delete emails", user));
                Object obj = in.readObject();
                if (obj != null && obj.getClass().equals(Request.class)) {
                    if (((Request) obj).getType().equals("OK")) {
                        System.out.println("Emails deleted");
                        //Remove mails from list
                        Platform.runLater(() -> {
                            user.getIdsToDelete().forEach(id -> {
                                emailsSent.removeIf(email -> id == email.getId());
                                emailsReceived.removeIf(email -> id == email.getId());
                            });

                            // Recalculate next id
                            int newLastId = 0;
                            if(!emailsSent.isEmpty() && !emailsReceived.isEmpty())
                                newLastId = Math.max(
                                        Collections.max(emailsSent, (e1, e2) -> e1.getId() - e2.getId()).getId(),
                                        Collections.max(emailsReceived, (e1, e2) -> e1.getId() - e2.getId()).getId()) + 1;
                            else if(!emailsSent.isEmpty())
                                newLastId = Collections.max(emailsSent, (e1, e2) -> e1.getId() - e2.getId()).getId() + 1;
                            else  if(!emailsReceived.isEmpty())
                                newLastId = Collections.max(emailsReceived, (e1, e2) -> e1.getId() - e2.getId()).getId() + 1;
                            user.setLastId(newLastId);

                            user.clearIdsToDelete();
                            clearListsSelection();
                            deleteSent.setDisable(true);
                            deleteReceived.setDisable(true);
                        });

                    } else {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    new Toast(((Request) obj).getType(), true).start();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            } catch (IOException e) {
                System.out.println("Connection Error");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new Toast("Connection Error", true).start();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                });
            } catch (ClassNotFoundException e) {
                System.out.println("Class not found");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new Toast("Class not found", true).start();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                });
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        System.out.println("Error during socket disconnection");
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    new Toast("Error during socket disconnection", true).start();
                                } catch (Exception e2) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }
        }).start();
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
            toMailText = toMailText.replace(user.getMail(), fromMailText);
            to.setText(toMailText);
        } else {
            to.setText("");
        }
        subject.setText(subjectMailText);
        message.setText(messageMailText);
        newMail(true);
    }

    @FXML
    public void sendMail() {
        // TODO:Check duplicates and avoid to send email to yourself
        String[] emails = to.getText().toLowerCase().split(",");
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+.[a-zA-Z0-9-.]+$");

        if (emails.length == 0) {
            System.out.println("email");
            try {
                new Toast("Please insert an email in the field", true).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        for (String email : emails)
            if (email.trim().isBlank() || !pattern.matcher(email.trim()).matches()) {
                System.out.println("email");
                try {
                    new Toast("Please correct emails format", true).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

        if (subject.getText().isBlank()) {
            System.out.println("subject");
            try {
                new Toast("Please insert a subject", true).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        } else if (message.getText().isBlank()) {
            System.out.println("message");
            try {
                new Toast("Please insert a message", true).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        // Remove spaces from all emails
        for (int i = 0; i < emails.length; i++)
            emails[i] = emails[i].trim();

        Email email = new Email(-1, username.getText(), Arrays.asList(emails), subject.getText(), message.getText(), new Date(), true);
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
                            user.setLastId(Math.max(user.getLastId(), email.getId() + 1));
                            //Add this email to Inviati List (run on FX thread)
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        new Toast("Email sent", false).start();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if (Arrays.asList(emails).contains(fromNewMail.getText()))
                                        //TODO:add to received
                                        System.out.println("");
                                    emailsSent.add(email);
                                    Collections.sort(emailsSent);
                                }
                            });
                            unselectedMail();
                        }
                    } else {
                        System.out.println(received.getType());
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    new Toast(received.getType(), true).start();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }

            } catch (IOException e) {
                System.out.println("Connection Error");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new Toast("Connection Error", true).start();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                });
            } catch (ClassNotFoundException e) {
                System.out.println("Class not found");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new Toast("Class not found", true).start();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                });
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        System.out.println("Error during socket disconnection");
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    new Toast("Error during socket disconnection", true).start();
                                } catch (Exception e2) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }
        }).start();
    }

    @FXML
    public void newMail() {
        clearListsSelection();
        clearFields();
        newMail(true);
    }

    public void newMail(boolean replay) {
        if (replay) {
            newMailContainer.setVisible(true);
            readMailContainer.setVisible(false);
            unselectedMailContainer.setVisible(false);
        }
    }

    public void readMail() {
        newMailContainer.setVisible(false);
        readMailContainer.setVisible(true);
        unselectedMailContainer.setVisible(false);
    }

    public void unselectedMail() {
        newMailContainer.setVisible(false);
        readMailContainer.setVisible(false);
        unselectedMailContainer.setVisible(true);
    }

    private void clearFields() {
        to.setText("");
        subject.setText("");
        message.setText("");
    }

    private void clearListsSelection() {
        listViewReceived.getSelectionModel().clearSelection();
        listViewSent.getSelectionModel().clearSelection();
    }

}
