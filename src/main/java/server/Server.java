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
        observableList = FXCollections.observableArrayList();
        logs.setItems(observableList);
        this.addLog(new Date() + " " + "SERVER STARTUP");
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
            Platform.runLater(() -> observableList.add(log));

        }
    }

    @FXML
    public void exitApplication() {
        ObjectOutputStream oos = null;
        PrintWriter printWriter = null;
        try {
            this.addLog(new Date() + " " + "SERVER SHUTDOWN");
            printWriter = new PrintWriter(new FileOutputStream(new File(System.getProperty("user.dir") + "/" + "saves/log.txt"), true));
            observableList.forEach(printWriter::println); //print each string as a line in the file
            printWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
        Platform.exit();
    }

    //TODO: add bug when exiting server
}
