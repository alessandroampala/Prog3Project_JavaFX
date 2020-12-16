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
    private boolean isSent;

    // Constructor
    public Email(int id, String from, List<String> to, String object, String text, Date date, boolean isSent) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.object = object;
        this.text = text;
        this.date = date;
        this.isSent = isSent;
    }

    /*
     * Sets variable based on the type
     * To send -> true
     * Received -> false
     */
    public void setIsSent(boolean isSent) {
        this.isSent = isSent;
    }

    // Returns isSent value
    public boolean getIsSent() {
        return isSent;
    }

    // Returns the "From" parameter
    public String getFrom() {
        return from;
    }

    // Returns the "To" parameter
    public List<String> getTo() {
        return to;
    }

    // Returns the "Subject" parameter
    public String getObject() {
        return object;
    }

    // Returns the "Message" parameter
    public String getText() {
        return text;
    }

    // Returns the date
    public Date getDate() {
        return date;
    }

    // Returns the id of the email
    public int getId() {
        return id;
    }

    // Sets the id of the email
    public void setId(int id) {
        this.id = id;
    }

    @Override
    // Compare method used to sort list in descending order, based on id
    public int compareTo(Email o) {
        return o.getId() - this.id;
    }

    public String getStringTo() {
        StringBuilder toReturn = new StringBuilder();
        for (String s : this.to) {
            toReturn.append(s);
            toReturn.append(", ");
        }
        if (toReturn.length() > 2) {
            toReturn.deleteCharAt(toReturn.length() - 1);
            toReturn.deleteCharAt(toReturn.length() - 1);
        }
        return toReturn.toString();
    }
}
