package server;

import mail.Email;
import mail.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Persistence {
    private static final String folderName = System.getProperty("user.dir") + "/" + "saves";

    // Checks if a list of users exist
    public static List<String> addressesExist(List<String> addresses) {
        List<String> inexistentAddresses = new ArrayList<>();
        for (String address : addresses)
            if (!addressExists(address))
                inexistentAddresses.add(address);
        if (inexistentAddresses.isEmpty())
            return null;
        return inexistentAddresses;
    }

    // Checks if a user exists
    public static boolean addressExists(String address) {
        File dir = new File(folderName + "/" + address);
        return dir.exists();
    }

    // Saves emails in the correct folders
    public static synchronized int saveEmail(String name, Email data) {
        assert data != null;
        if (!addressExists(name)) return -1;
        FileOutputStream fout = null;
        int id = -1;
        try {
            id = calculateNextId(name);
            data.setId(id);
            String filePath = folderName + "/" + name + "/" + id;
            new File(filePath).createNewFile(); // if file doesn't exists yet, create it
            fout = new FileOutputStream(filePath);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return id;
    }

    // Loads users checking existing folders
    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        if (!new File(folderName).exists()) {
            return null;
        }
        File[] files = new File(folderName).listFiles();
        for (File file : files) {
            users.add(new User(file.getName()));
        }
        users.removeIf(user -> user.getMail().equals("log.txt")); // Removes log entry if exists
        return users;
    }

    // Deletes email from a folder
    public static synchronized void deleteEmail(String name, int id) {
        String filePath = folderName + "/" + name + "/" + id;
        if (!new File(filePath).exists())
            return;
        File in = new File(filePath);
        in.delete();
    }

    // Loads emails from the given point
    public static synchronized List<Email> loadEmailsFromId(String name, int id) {
        List<Email> emails = new ArrayList<>();
        File dir = new File(folderName + "/" + name);
        File[] files = dir.listFiles();
        ObjectInputStream in = null;
        if (files != null)
            for (File f : files) {
                if (Integer.parseInt(f.getName()) >= id)
                    try {
                        in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f.getPath())));
                        Object o = in.readObject();
                        if (o != null && o.getClass().equals(Email.class)) {
                            emails.add((Email) o);
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (in != null)
                                in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            }
        return emails;
    }

    // Calculates the next id for a new email
    private static int calculateNextId(String name) {
        File dir = new File(folderName + "/" + name);
        File[] files = dir.listFiles();

        int max = -1;
        if (files != null)
            for (File f : files)
                if (Integer.parseInt(f.getName()) > max)
                    max = Integer.parseInt(f.getName());
        return max + 1;
    }
}
