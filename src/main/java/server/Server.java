package server;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import mail.Email;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 9000;
    private static final int MAX_TREAHDS_NUM = 10;

    private ObservableList<String> observableList;

    @FXML
    private ListView<String> logs;

    @FXML
    public void initialize() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(System.getProperty("user.dir") + "/" + "saves/log")));
        Object o = ois.readObject();
        ois.close();
        if (o != null)
            observableList = FXCollections.observableArrayList((ArrayList<String>) o);
        else
            observableList = FXCollections.observableArrayList();
        logs.setItems(observableList);
        new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                synchronized (observableList) {
                    logs.setItems(observableList);
                }
            }
        };
        Thread thread = new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(PORT);
                ExecutorService executor = Executors.newFixedThreadPool(MAX_TREAHDS_NUM);

                while (true) {
                    Socket s = serverSocket.accept();
                    executor.execute(new RequestHandler(s, this));
                }
            } catch (IOException e) {
                e.printStackTrace();
                addLog(new Date() + " " + e.toString());
            }

        });
        thread.setDaemon(true);
        thread.start();
    }

    void addLog(String log) {
        synchronized (observableList) {
            observableList.add(log);
        }
    }

    @FXML
    public void exitApplication() {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(System.getProperty("user.dir") + "/" + "saves/log", false));
            oos.writeObject(new ArrayList<>(observableList));
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Platform.exit();
    }
}
