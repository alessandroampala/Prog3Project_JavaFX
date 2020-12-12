package server;

import mail.Email;
import mail.Request;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

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
                    case "Send email":
                        Object obj = request.getData();
                        if(obj != null && obj.getClass().equals(Email.class))
                        {
                            Email email = (Email) obj;
                            List<String> addresses = email.getTo();
                            if(Persistence.addressExists(email.getFrom()) && Persistence.addressesExist(addresses))
                            {
                                int mailId = Persistence.saveEmail(email.getFrom(), email);
                                for(String address : addresses)
                                    Persistence.saveEmail(address, email);
                                outStream.writeObject(new Request("OK", mailId)); //send back mailId
                            }
                            else
                            {
                                outStream.writeObject(new Request("ERRORE negli indirizzi di destinazione", null)); //send back mailId
                            }
                        }
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

