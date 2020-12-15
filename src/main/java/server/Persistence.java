package server;

import mail.Email;
import mail.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Persistence {
    private static final String folderName = System.getProperty("user.dir") + "/" + "saves";

    public static int prepareDir(String name) {
        File dir = new File(folderName + "/" + name);
        if (!dir.exists())
            return -1;
        return 0;
    }

    public static List<String> addressesExist(List<String> addresses) {
        List<String> inexistentAddresses = new ArrayList<>();
        for (String address : addresses)
            if (!addressExists(address))
                inexistentAddresses.add(address);
        if (inexistentAddresses.isEmpty())
            return null;
        return inexistentAddresses;
    }

    public static boolean addressExists(String address) {
        File dir = new File(folderName + "/" + address);
        if (!dir.exists())
            return false;
        return true;
    }

    public static synchronized int saveEmail(String name, Email data) {
        assert data != null;
        if (prepareDir(name) == -1) return -1;
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

    public static Email loadEmail(String name, int id) {
        String filePath = folderName + "/" + name + "/" + id;
        ObjectInputStream in = null;
        if (!new File(filePath).exists())
            return null;
        try {
            in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filePath)));
            Object o = in.readObject();
            if (o != null && o.getClass().equals(Email.class))
                return (Email) o;
            else
                return null;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

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

    public static void deleteEmail(String name, int id) {
        String filePath = folderName + "/" + name + "/" + id;
        if (!new File(filePath).exists())
            return;
        File in = new File(filePath);
        in.delete();
    }

    public static List<Email> loadEmailsFromId(String name, int id) {
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

    public static List<Email> loadAllEmails(String name) {
        return loadEmailsFromId(name, 0);
    }

    private static int calculateNextId(String name) {
        List<Email> emails = new ArrayList<>();
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
