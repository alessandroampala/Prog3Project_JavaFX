package mail;

public class User {
    private String mail;
    private int lastId = 0;

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
}
