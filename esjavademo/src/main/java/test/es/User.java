package test.es;

import java.util.Date;

public class User {

    private String username;

    private Date postDate;

    private String message;

    public User(String username, Date postDate, String message) {
        this.username = username;
        this.postDate = postDate;
        this.message = message;
    }

    public User() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(Date postDate) {
        this.postDate = postDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
