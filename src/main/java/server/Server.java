package server;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 9000;
    private static final int MAX_THREADS_NUM = 10;

    private ObservableList<String> observableList;
    private ExecutorService executor = null;

    @FXML
    private ListView<String> logs;

    @FXML
    /*
     * Initialize the controller for the server:
     * Creates the server socket
     * Waits connections
     */
    public void initialize() {
        observableList = FXCollections.observableArrayList();
        logs.setItems(observableList);
        this.addLog(new Date() + " " + "SERVER STARTUP");

        Thread thread = new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(PORT);
                executor = Executors.newFixedThreadPool(MAX_THREADS_NUM);

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

    //Adds log to the observable list
    void addLog(String log) {
        synchronized (observableList) {
            Platform.runLater(() -> observableList.add(log));
        }
    }

    @FXML
    /*
     * Executes the following tasks during application exit
     * Shutdowns the executor if exists
     * close the window
     */
    public void exitApplication() {
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
        if (executor != null)
            executor.shutdownNow();
        Platform.exit();
    }
}
