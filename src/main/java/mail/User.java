package mail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private final String mail;
    private int lastId = 0;
    private List<Integer> idsToDelete = new ArrayList<>();

    // Constructor
    public User(String mail) {
        this.mail = mail;
    }

    // Returns the user mail
    public String getMail() {
        return mail;
    }

    // Returns the last id of the loaded emails
    public int getLastId() {
        return lastId;
    }

    // Sets the last id of the loaded emails
    public void setLastId(int lastId) {
        this.lastId = lastId;
    }

    // Returns the ids of the emails to delete
    public List<Integer> getIdsToDelete() {
        synchronized (idsToDelete) {
            return idsToDelete;
        }
    }

    // Adds ids to the emails to delete
    public void addIdsToDelete(int id) {
        synchronized (idsToDelete) {
            idsToDelete.add(id);
        }
    }

    // Removes all ids from the list
    public void clearIdsToDelete() {
        synchronized (idsToDelete) {
            idsToDelete.clear();
        }
    }
}
