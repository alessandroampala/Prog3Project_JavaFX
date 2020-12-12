package mail;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String mail;
    private int lastId = 0;
    private List<Integer> idsToDelete = new ArrayList<>();

    public User(String mail) {
        this.mail = mail;
    }

    public String getMail() {
        return mail;
    }

    public int getLastId() {
        return lastId;
    }

    public void setLastId(int lastId) {
        this.lastId = lastId;
    }

    public List<Integer> getIdsToDelete() {
        return idsToDelete;
    }

    public void addIdsToDelete(int id) {
        idsToDelete.add(id);
    }

    public void clearIdsToDelete() {
        idsToDelete.clear();
    }
}
