package server;

import mail.Email;
import mail.Request;
import mail.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class RequestHandler implements Runnable {

    private Socket socket;

    public RequestHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

            Object received = inStream.readObject();
            Object obj;
            if (received != null && received.getClass().equals(Request.class)) {
                Request request = (Request) received;
                switch (request.getType()) {
                    //HANDLE REQUEST
                    case "Send email":
                        obj = request.getData();
                        if (obj != null && obj.getClass().equals(Email.class)) {
                            Email email = (Email) obj;
                            List<String> addresses = email.getTo();
                            if (Persistence.addressExists(email.getFrom()) && Persistence.addressesExist(addresses)) {
                                int mailId = Persistence.saveEmail(email.getFrom(), email);
                                for (String address : addresses)
                                    Persistence.saveEmail(address, email);
                                outStream.writeObject(new Request("OK", mailId)); //send back mailId
                            } else {
                                outStream.writeObject(new Request("ERRORE negli indirizzi di destinazione", null)); //send back mailId
                            }
                        }
                        break;
                    case "Receive emails":
                        obj = request.getData();
                        if (obj != null && obj.getClass().equals(User.class)) {
                            List<Email> emails = Persistence.loadEmailsFromId(((User) obj).getMail(), ((User) obj).getLastId());
                            if (!emails.isEmpty()) {
                                outStream.writeObject(new Request("OK", emails));
                            } else {
                                outStream.writeObject(new Request("Nessuna email trovata", null));
                            }
                        }
                        break;
                    case "Delete emails":
                        obj = request.getData();
                        if (obj != null && obj.getClass().equals(User.class)) {
                            for (int id : ((User) obj).getIdsToDelete()) {
                                Persistence.deleteEmail(((User) obj).getMail(), id);
                            }
                            outStream.writeObject(new Request("OK", null));
                        }
                        break;
                    case "Load users":
                        List<User> users = Persistence.loadUsers();
                        if (!users.isEmpty())
                            outStream.writeObject(new Request("OK", users));
                        else
                            outStream.writeObject(new Request("Non ci sono utenti", null));
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

