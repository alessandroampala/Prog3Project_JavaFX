package mail;

import java.io.Serializable;

public class Request implements Serializable {
    private final String type;
    private final Object data;

    // Constructor
    public Request(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    // Returns the request type
    public String getType() {
        return type;
    }

    // Returns the request data
    public Object getData() {
        return data;
    }
}
