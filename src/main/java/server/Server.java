package server;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 9000;
    private static final int MAX_TREAHDS_NUM = 10;

    @FXML
    private TextArea logText;

    @FXML
    public void initialize() {
        new Thread( () -> {
           logText.setText("ciao\n");
            try {
                ServerSocket serverSocket = new ServerSocket(PORT);
                ExecutorService executor = Executors.newFixedThreadPool(MAX_TREAHDS_NUM);

                while (true)
                {
                    Socket s = serverSocket.accept();
                    executor.execute(new RequestHandler(s));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
    }

    //TODO: shutdown thread when closing server
}
