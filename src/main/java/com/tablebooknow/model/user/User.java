package com.tablebooknow.model.user;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents a user in the hotel table reservation system.
 */
public class User implements Serializable {
    private String id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private boolean isAdmin;

    /**
     * Default constructor that generates a unique ID for a new user.
     */
    public User() {
        this.id = UUID.randomUUID().toString();
        this.isAdmin = false; // By default, a new user is not an admin
    }

    /**
     * Constructor with all fields.
     */
    public User(String id, String username, String password, String email, String phone, boolean isAdmin) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.isAdmin = isAdmin;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    /**
     * Converts the User object to a CSV format string for file storage.
     * Format: id,username,password,email,phone,isAdmin
     */
    public String toCsvString() {
        return String.format("%s,%s,%s,%s,%s,%b",
                id,
                username,
                password,
                email != null ? email : "",
                phone != null ? phone : "",
                isAdmin);
    }

    /**
     * Creates a User object from a CSV format string.
     */
    public static User fromCsvString(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length < 6) {
            throw new IllegalArgumentException("Invalid CSV format for User");
        }

        return new User(
                parts[0],
                parts[1],
                parts[2],
                parts[3],
                parts[4],
                Boolean.parseBoolean(parts[5])
        );
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }
}