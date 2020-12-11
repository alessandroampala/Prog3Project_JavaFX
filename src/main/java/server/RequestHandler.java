package server;

import mail.Request;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RequestHandler implements Runnable{

    private Socket socket;

    public RequestHandler(Socket socket)
    {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

            Object received = inStream.readObject();
            if(received != null && received.getClass().equals(Request.class))
            {
                Request request = (Request) received;
                switch (request.getType())
                {
                    //HANDLE REQUEST

                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

