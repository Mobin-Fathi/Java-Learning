package model;

import java.util.Objects;

/**
 * این کلاس نشان‌دهنده اطلاعات کاربر در سیستم است.
 */
public class User {
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = (username != null) ? username.trim() : "";
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username != null) {
            this.username = username.trim();
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return username + "," + password;
    }
}
