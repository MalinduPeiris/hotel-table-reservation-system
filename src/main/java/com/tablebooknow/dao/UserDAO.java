package com.tablebooknow.dao;

import com.tablebooknow.model.user.User;
import com.tablebooknow.util.FileHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User entities to handle file-based storage operations.
 */
public class UserDAO {

    private static final String FILE_PATH = getDataFilePath("users.txt");

    /**
     * Gets the path to a data file, using the application's data directory.
     */
    private static String getDataFilePath(String fileName) {
        String dataPath = System.getProperty("app.datapath");

        // Fallback to user.dir/data if app.datapath is not set
        if (dataPath == null) {
            dataPath = System.getProperty("user.dir") + File.separator + "data";
        }

        return dataPath + File.separator + fileName;
    }

    /**
     * Creates a new user by appending to the users file.
     * @param user The user to create
     * @return The created user with assigned ID
     */
    public User create(User user) throws IOException {
        // Make sure the file exists
        FileHandler.ensureFileExists(FILE_PATH);

        // Log the file path for debugging
        System.out.println("Creating user in file: " + FILE_PATH);

        // Append user to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(user.toCsvString());
            writer.newLine();
        }

        return user;
    }

    /**
     * Finds a user by their ID.
     * @param id The ID to search for
     * @return The user or null if not found
     */
    public User findById(String id) throws IOException {
        if (!FileHandler.fileExists(FILE_PATH)) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                User user = User.fromCsvString(line);
                if (user.getId().equals(id)) {
                    return user;
                }
            }
        }

        return null;
    }

    /**
     * Finds a user by their username.
     * @param username The username to search for
     * @return The user or null if not found
     */
    public User findByUsername(String username) throws IOException {
        if (!FileHandler.fileExists(FILE_PATH)) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                User user = User.fromCsvString(line);
                if (user.getUsername().equalsIgnoreCase(username)) {
                    return user;
                }
            }
        }

        return null;
    }

    /**
     * Finds a user by their email.
     * @param email The email to search for
     * @return The user or null if not found
     */
    public User findByEmail(String email) throws IOException {
        if (!FileHandler.fileExists(FILE_PATH)) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                User user = User.fromCsvString(line);
                if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(email)) {
                    return user;
                }
            }
        }

        return null;
    }

    /**
     * Updates a user's information.
     * @param user The user to update
     * @return true if successful, false otherwise
     */
    public boolean update(User user) throws IOException {
        if (!FileHandler.fileExists(FILE_PATH)) {
            return false;
        }

        List<User> users = findAll();
        boolean found = false;

        // Replace the user in the list
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(user.getId())) {
                users.set(i, user);
                found = true;
                break;
            }
        }

        if (!found) {
            return false;
        }

        // Write all users back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (User u : users) {
                writer.write(u.toCsvString());
                writer.newLine();
            }
        }

        return true;
    }

    /**
     * Deletes a user by their ID.
     * @param id The ID of the user to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(String id) throws IOException {
        if (!FileHandler.fileExists(FILE_PATH)) {
            return false;
        }

        List<User> users = findAll();
        boolean found = false;

        // Remove the user from the list
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(id)) {
                users.remove(i);
                found = true;
                break;
            }
        }

        if (!found) {
            return false;
        }

        // Write all users back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (User u : users) {
                writer.write(u.toCsvString());
                writer.newLine();
            }
        }

        return true;
    }

    /**
     * Gets all users from the file.
     * @return List of all users
     */
    public List<User> findAll() throws IOException {
        List<User> users = new ArrayList<>();

        if (!FileHandler.fileExists(FILE_PATH)) {
            return users;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    users.add(User.fromCsvString(line));
                }
            }
        }

        return users;
    }
}