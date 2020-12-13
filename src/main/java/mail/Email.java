package mail;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Email implements Serializable, Comparable<Email> {
    // Useful for having the same serialVersionUID both on client and server
    private static final long serialVersionUID = 5748049580470856805L;

    private int id;
    private final String from;
    private final List<String> to;
    private final String object;
    private final String text;
    private final Date date;

    public Email(int id, String from, List<String> to, String object, String text, Date date) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.object = object;
        this.text = text;
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    public List<String> getTo() {
        return to;
    }

    public String getObject() {
        return object;
    }

    public String getText() {
        return text;
    }

    public Date getDate() {
        return date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int compareTo(Email o) {
        return o.getId() - this.id;
    }
}
