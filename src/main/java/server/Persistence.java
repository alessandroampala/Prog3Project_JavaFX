package server;

import mail.Email;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Persistence {
    private static final String folderName = System.getProperty("user.dir") + "/" + "saves";

    private static void prepareDir(String name) {
        File dir = new File(folderName + "/" + name);
        if (!dir.exists())
            dir.mkdirs();
    }

    public static void saveEmail(String name, Email data) {
        assert data != null;
        prepareDir(name);
        FileOutputStream fout = null;
        try {
            String filePath = folderName + "/" + name + "/" + data.getId();
            new File(filePath).createNewFile(); // if file doesn't exists yet, create it
            fout = new FileOutputStream(filePath);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Email loadEmail(String name, int id) {
        String filePath = folderName + "/" + name + "/" + id;
        ObjectInputStream in = null;
        if (!new File(filePath).exists())
            return null;
        try {
            in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filePath)));
            Object o = in.readObject();
            Email email;
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
}
