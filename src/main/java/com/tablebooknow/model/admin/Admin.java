package com.tablebooknow.model.admin;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents an admin user in the system with elevated privileges.
 */
public class Admin implements Serializable {
    private String id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String role;

    /**
     * Default constructor that generates a unique ID for a new admin.
     */
    public Admin() {
        this.id = UUID.randomUUID().toString();
        this.role = "admin"; // Default role
    }

    /**
     * Constructor with all fields.
     */
    public Admin(String id, String username, String password, String email, String fullName, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.role = role != null ? role : "admin";
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Converts the Admin object to a CSV format string for file storage.
     * Format: id,username,password,email,fullName,role
     */
    public String toCsvString() {
        return String.format("%s,%s,%s,%s,%s,%s",
                id,
                username,
                password,
                email != null ? email : "",
                fullName != null ? fullName : "",
                role != null ? role : "admin");
    }

    /**
     * Creates an Admin object from a CSV format string.
     */
    public static Admin fromCsvString(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length < 6) {
            throw new IllegalArgumentException("Invalid CSV format for Admin");
        }

        return new Admin(
                parts[0],
                parts[1],
                parts[2],
                parts[3],
                parts[4],
                parts[5]
        );
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}